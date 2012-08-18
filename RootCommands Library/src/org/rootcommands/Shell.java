/*
 * Copyright (C) 2012 Dominik Schürmann <dominik@dominikschuermann.de>
 * Copyright (c) 2012 Stephen Erickson, Chris Ravenscroft, Adam Shanks, Jeremy Lakeman (RootTools)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.rootcommands;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.rootcommands.util.Constants;
import org.rootcommands.util.Log;
import org.rootcommands.util.RootAccessDeniedException;
import org.rootcommands.util.Utils;

public class Shell {
    private final Process proc;
    private final BufferedReader stdOut;
    private final BufferedReader stdErr;
    private final DataOutputStream outputStream;
    private final List<Command> commands = new ArrayList<Command>();
    private boolean close = false;

    private static final String LD_LIBRARY_PATH = System.getenv("LD_LIBRARY_PATH");
    private static final String token = "F*D^W@#FGF";

    /**
     * Start root shell
     * 
     * @param customEnv
     * @param baseDirectory
     * @return
     * @throws IOException
     */
    public static Shell startRootShell(ArrayList<String> customEnv, String baseDirectory)
            throws IOException, RootAccessDeniedException {
        Log.d(Constants.TAG, "Starting Root Shell!");

        // On some versions of Android (ICS) LD_LIBRARY_PATH is unset when using su
        // We need to pass LD_LIBRARY_PATH over su for some commands to work correctly.
        if (customEnv == null) {
            customEnv = new ArrayList<String>();
        }
        customEnv.add("LD_LIBRARY_PATH=" + LD_LIBRARY_PATH);

        Shell shell = new Shell(Utils.getSuPath(), customEnv, baseDirectory);

        return shell;
    }

    /**
     * Start root shell without custom environment and base directory
     * 
     * @return
     * @throws IOException
     */
    public static Shell startRootShell() throws IOException, RootAccessDeniedException {
        return startRootShell(null, null);
    }

    /**
     * Start default sh shell
     * 
     * @param customEnv
     * @param baseDirectory
     * @return
     * @throws IOException
     */
    public static Shell startShell(ArrayList<String> customEnv, String baseDirectory)
            throws IOException {
        Log.d(Constants.TAG, "Starting Shell!");
        Shell shell = new Shell("sh", customEnv, baseDirectory);
        return shell;
    }

    /**
     * Start default sh shell without custom environment and base directory
     * 
     * @return
     * @throws IOException
     */
    public static Shell startShell() throws IOException {
        return startShell(null, null);
    }

    /**
     * Start custom shell defined by shellPath
     * 
     * @param shellPath
     * @param customEnv
     * @param baseDirectory
     * @return
     * @throws IOException
     */
    public static Shell startCustomShell(String shellPath, ArrayList<String> customEnv,
            String baseDirectory) throws IOException {
        Log.d(Constants.TAG, "Starting Custom Shell!");
        Shell shell = new Shell(shellPath, customEnv, baseDirectory);

        return shell;
    }

    /**
     * Start custom shell without custom environment and base directory
     * 
     * @param shellPath
     * @return
     * @throws IOException
     */
    public static Shell startCustomShell(String shellPath) throws IOException {
        return startCustomShell(shellPath, null, null);
    }

    private Shell(String shell, ArrayList<String> customEnv, String baseDirectory)
            throws IOException, RootAccessDeniedException {

        Log.d(Constants.TAG, "Starting shell: " + shell);

        // proc = new ProcessBuilder(shell).redirectErrorStream(true).start();

        // TODO: ERROR STREAM!!!
        // open shell!
        proc = Utils.runWithEnv(shell, customEnv, baseDirectory);

        stdOut = new BufferedReader(new InputStreamReader(proc.getInputStream()));
        stdErr = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
        outputStream = new DataOutputStream(proc.getOutputStream());

        outputStream.write("echo Started\n".getBytes());
        outputStream.flush();

        while (true) {
            String line = stdOut.readLine();
            if (line == null)
                throw new RootAccessDeniedException(
                        "stdout line is null! This is probably no shell!");
            if ("".equals(line))
                continue;
            if ("Started".equals(line))
                break;

            destroyShellProcess();
            throw new IOException("Unable to start shell, unexpected output \"" + line + "\"");
        }

        new Thread(input, "Shell Input").start();
        new Thread(output, "Shell Output").start();
    }

    private Runnable input = new Runnable() {
        public void run() {
            try {
                writeCommands();
            } catch (IOException e) {
                Log.e(Constants.TAG, "IO Exception", e);
            }
        }
    };

    /**
     * Destroy shell process considering that the process could already be terminated
     */
    private void destroyShellProcess() {
        try {
            // Yes, this really is the way to check if the process is
            // still running.
            proc.exitValue();
        } catch (IllegalThreadStateException e) {
            // Only call destroy() if the process is still running;
            // Calling it for a terminated process will not crash, but
            // (starting with at least ICS/4.0) spam the log with INFO
            // messages ala "Failed to destroy process" and "kill
            // failed: ESRCH (No such process)".
            proc.destroy();
        }

        Log.d(Constants.TAG, "Shell destroyed");
    }

    private void writeCommands() throws IOException {
        try {
            int write = 0;
            while (true) {
                DataOutputStream out;
                synchronized (commands) {
                    while (!close && write >= commands.size()) {
                        commands.wait();
                    }
                    out = this.outputStream;
                }
                if (write < commands.size()) {
                    Command next = commands.get(write);
                    next.writeCommand(out);
                    String line = "\necho " + token + " " + write + " $?\n";
                    out.write(line.getBytes());
                    out.flush();
                    write++;
                } else if (close) {
                    out.write("\nexit 0\n".getBytes());
                    out.flush();
                    out.close();
                    Log.d(Constants.TAG, "Closing shell");
                    return;
                }
            }
        } catch (InterruptedException e) {
            Log.e(Constants.TAG, "interrupted while writing command", e);
        }
    }

    private Runnable output = new Runnable() {
        public void run() {
            try {
                readOutput();
            } catch (IOException e) {
                Log.e(Constants.TAG, "IOException", e);
            } catch (InterruptedException e) {
                Log.e(Constants.TAG, "InterruptedException", e);
            }
        }
    };

    /*
     * 
     * TODO: implement check for broken toolbox, busybox, throw new exception then
     * 
     * 
     * Sadly sometimes toolbox is broken. This can be recognized by lines such as
     * "Stderr: ls: /system/bin/toolbox: Value too large for defined data type". We try to detect
     * broken versions here. It the same problem as some busybox versions have (see
     * https://code.google.com/p/busybox-android/issues/detail?id=1). It is giving
     * "Value too large for defined data type" on certain file operations (e.g. ls and chown) in
     * certain directories (e.g. /data/data)
     * 
     * Known roms with broken toolbox:
     * 
     * - stock rom Android 4 of Galaxy Note
     */
    private void readOutput() throws IOException, InterruptedException {
        Command command = null;
        int read = 0;
        while (true) {
            String line = stdOut.readLine();

            // terminate on EOF
            if (line == null)
                break;

            // Log.v("Shell", "Out; \"" + line + "\"");
            if (command == null) {
                if (read >= commands.size()) {
                    if (close)
                        break;
                    continue;
                }
                command = commands.get(read);
            }

            int pos = line.indexOf(token);
            if (pos > 0)
                command.output(command.id, line.substring(0, pos));
            if (pos >= 0) {
                line = line.substring(pos);
                String fields[] = line.split(" ");
                int id = Integer.parseInt(fields[1]);
                if (id == read) {
                    command.setExitCode(Integer.parseInt(fields[2]));
                    read++;
                    command = null;
                    continue;
                }
            }
            command.output(command.id, line);
        }
        Log.d(Constants.TAG, "Read all output");
        proc.waitFor();
        destroyShellProcess();

        while (read < commands.size()) {
            if (command == null)
                command = commands.get(read);
            command.terminated("Unexpected Termination.");
            command = null;
            read++;
        }
    }

    /**
     * Add command to shell queue
     * 
     * @param command
     * @return
     * @throws IOException
     */
    public Command add(Command command) throws IOException {
        if (close)
            throw new IOException("Unable to add commands to a closed shell");
        synchronized (commands) {
            commands.add(command);
            // set shell on the command object, to now where it is running on
            command.setShell(this);
            commands.notifyAll();
        }

        return command;
    }

    /**
     * Close shell
     * 
     * @throws IOException
     */
    public void close() throws IOException {
        synchronized (commands) {
            this.close = true;
            commands.notifyAll();
        }
    }

    /**
     * Returns number of queued commands
     * 
     * @return
     */
    public int getCommandsSize() {
        return commands.size();
    }
}