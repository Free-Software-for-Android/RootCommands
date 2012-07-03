/*
 * Copyright (C) 2012 Dominik Schürmann <dominik@dominikschuermann.de>
 * Copyright (c) 2012 Michael Elsdörfer (Android Autostarts)
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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.rootcommands.util.Constants;
import org.rootcommands.util.Log;
import org.rootcommands.util.ShellResult;
import org.rootcommands.util.Utils;

/**
 * 
 * ShellExecutor can open su or sh shells on Android to be used for shell commands
 * 
 * 
 * Comment from Autostarts-App:
 * 
 * Running an app through root isn't even that straightforward as it would seem. Here's some issues
 * we've run into so far, and which we try to workaround here:
 * 
 * 1) The Superuser Whitelist application most rooted devices use identifies the command based on
 * arguments. If we were to just call su -c "pm xyz", the user would need to confirm every single
 * call; "Allows allow" would be useless.
 * 
 * 2) Rarely, a devices seems to have a su-executable that uses a different argument syntax (`su -c
 * "command args"` vs. `su -c command args`).
 * 
 * 3) Some ROMs have their "su" in a non-standard location, like Archos in /data/bin, and it's not
 * on the path either (this is because they don't have write-access to /system yet).
 * 
 * 4) Some custom ROMs contain what seems to be a kernel bug, in which su/sh?, when run outside of
 * the system shell, cannot access certain paths. The error is "pm: not found". This happens even
 * though the file does exists, and runs just fine from the shell.
 * 
 * The common approach chosen by most root apps seems to be to run "su" and pipe commands into it.
 * This will solve (1) and (2). (3) we solve by checking multiple locations for su. We'll still have
 * to see about (4).
 */
public class ShellExecutor {
    private boolean asRoot;
    private int sleepTime;
    private String[] env;
    private String baseDirectory;
    private Integer timeout;

    public ShellExecutor(boolean asRoot, int sleepTime, String[] env, String baseDirectory,
            Integer timeout) {
        super();
        this.asRoot = asRoot;
        this.sleepTime = sleepTime;
        this.env = env;
        this.baseDirectory = baseDirectory;
        this.timeout = timeout;
    }

    /**
     * Asynchronous stream reader class to pipe output into result objects
     */
    protected class StreamSucker extends Thread {
        InputStream is;
        boolean stderr;
        ShellResult shellResult;

        StreamSucker(InputStream is, boolean stderr, ShellResult shellResult) {
            this.is = is;
            this.stderr = stderr;
            this.shellResult = shellResult;
        }

        public void run() {
            try {
                InputStreamReader isr = new InputStreamReader(is);
                BufferedReader br = new BufferedReader(isr);

                String line = null;
                while ((line = br.readLine()) != null) {
                    // process every line of stdout and stderr in corresponding result object
                    if (shellResult != null) {
                        if (stderr) {
                            Log.d(Constants.TAG, "Stderr: " + line);

                            shellResult.processError(line);
                        } else {
                            Log.d(Constants.TAG, "Stdout: " + line);

                            shellResult.process(line);
                        }
                    }
                }
            } catch (IOException ioe) {
                Log.e(Constants.TAG, "IO Exception in StreamSucker", ioe);
            }
        }
    }

    Process process = null;
    DataOutputStream os = null;

    /**
     * Starts shell in a process based on parameters from ShellExecutor constructor
     * 
     * Code partly from https://github.com/miracle2k/android-autostarts, use under Apache License
     * was agreed by Michael Elsdörfer
     */
    public void openShell() {
        if (process != null || os != null) {
            Log.e(Constants.TAG, "Shell is already open!");
            return;
        }

        try {
            // create new shell process with given environment and baseDirectory
            if (asRoot) {
                process = Utils.runWithEnv(Utils.getSuPath(), env, baseDirectory);
            } else {
                process = Utils.runWithEnv("sh", env, baseDirectory);
            }

            // OutputStream where commands are written to
            os = new DataOutputStream(process.getOutputStream());
        } catch (IOException e) {
            Log.e(Constants.TAG, "Problem while opening shell", e);
        }
    }

    /**
     * Executes command with result object to parse output
     * 
     * @param command
     *            to be run on the opened shell
     * @param result
     *            optional to parse stdout and stderr
     */
    public void runCommand(String command, ShellResult result) {
        if (os == null) {
            Log.e(Constants.TAG, "Shell is not open, you need to open it first!");
            return;
        }

        Log.d(Constants.TAG, "runCommand with: " + command);

        try {
            // create async stream suckers to get stdout and stderr while executing commands
            StreamSucker stdoutSucker = new StreamSucker(process.getInputStream(), false, result);
            StreamSucker stderrSucker = new StreamSucker(process.getErrorStream(), true, result);
            stdoutSucker.start();
            stderrSucker.start();

            // execute command and sleep afterwards
            os.writeBytes(command + "\n");
            os.flush();

            Thread.sleep(sleepTime);

        } catch (Exception e) {
            Log.e(Constants.TAG, "Problem while executing command!", e);

            // process error in result object
            if (result != null) {
                result.onFailure(e);
            }
        }

        Log.d(Constants.TAG, "After runCommand...");
    }

    /**
     * Closes shell
     * 
     * Code partly from https://github.com/miracle2k/android-autostarts, use under Apache License
     * was agreed by Michael Elsdörfer
     */
    public boolean closeShell() {
        if (os == null) {
            Log.e(Constants.TAG, "Shell is not open, you need to open it first!");
            return false;
        }

        try {
            os.writeBytes("exit\n");
            os.flush();

            // Handle a requested timeout, or just use waitFor() otherwise.
            if (timeout != null) {
                long finish = System.currentTimeMillis() + timeout;
                while (true) {
                    Thread.sleep(300);
                    if (!isProcessAlive(process))
                        break;
                    // TODO: We could use a callback to let the caller
                    // check the success-condition (like the state properly
                    // being changed), and then end early, rather than
                    // waiting for the timeout to occur. However, this
                    // is made more complicated by us not really wanting
                    // to kill a process early that would never have hung,
                    // but which might not actually be completely finished yet
                    // when the callback would register success.
                    // Also, now that the timeout is only used as a last-resort
                    // mechanism anyway, with most cases of a hanging process
                    // being avoided by switching on ADB Debugging, improving
                    // the timeout handling isn't that important anymore.
                    if (System.currentTimeMillis() > finish) {
                        // Usually, this can't be considered a success.
                        // However, in terms of the bug we're trying to
                        // work around here (the call hanging if adb
                        // debugging is disabled), the command would
                        // have successfully run, but just doesn't
                        // return. We report success, just in case, and
                        // the caller will have to check whether the
                        // command actually did do it's job.
                        // TODO: It might be more "correct" to return false
                        // here, or indicate the timeout in some other way,
                        // and let the caller ignore those values on their
                        // own violation.
                        Log.w(Constants.TAG, "Process doesn't seem "
                                + "to stop on it's own, assuming it's hanging");
                        // Note: 'finally' will call destroy(), but you
                        // might still see zombies.
                        return true;
                    }
                }
            } else {
                process.waitFor();
            }

            Log.d(Constants.TAG, "Process returned with " + process.exitValue());
            // Log.d(Constants.TAG,
            // "Process stdout was: " + Utils.readStream(process.getInputStream())
            // + "; stderr: " + Utils.readStream(process.getErrorStream()));

            // In order to consider this a success, we require to
            // things: a) a proper exit value, and ...
            if (process.exitValue() != 0) {
                return false;
            }

            return true;

        } catch (FileNotFoundException e) {
            Log.e(Constants.TAG, "Failed to run command", e);
            return false;
        } catch (IOException e) {
            Log.e(Constants.TAG, "Failed to run command", e);
            return false;
        } catch (InterruptedException e) {
            Log.e(Constants.TAG, "Failed to run command", e);
            return false;
        } finally {
            if (os != null)
                try {
                    os.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            if (process != null) {
                try {
                    // Yes, this really is the way to check if the process is
                    // still running.
                    process.exitValue();
                } catch (IllegalThreadStateException e) {
                    // Only call destroy() if the process is still running;
                    // Calling it for a terminated process will not crash, but
                    // (starting with at least ICS/4.0) spam the log with INFO
                    // messages ala "Failed to destroy process" and "kill
                    // failed: ESRCH (No such process)".
                    process.destroy();
                }
            }
        }
    }

    /**
     * Check whether a process is still alive. We use this as a naive way to implement timeouts.
     */
    public static boolean isProcessAlive(Process p) {
        try {
            p.exitValue();
            return false;
        } catch (IllegalThreadStateException e) {
            return true;
        }
    }

}
