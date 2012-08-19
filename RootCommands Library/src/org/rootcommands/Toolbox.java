/*
 * Copyright (C) 2012 Dominik Schürmann <dominik@dominikschuermann.de>
 * Copyright (c) 2012 Stephen Erickson, Chris Ravenscroft, Adam Shanks (RootTools)
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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.rootcommands.util.BrokenBusyboxException;
import org.rootcommands.util.Constants;
import org.rootcommands.util.Log;

/**
 * All methods in this class are working with Androids toolbox. Toolbox is similar to busybox, but
 * normally shipped on every Android OS. You can find toolbox commands on
 * https://github.com/CyanogenMod/android_system_core/tree/ics/toolbox
 * 
 * This means that these commands are designed to work on every Android OS, with a _working_ toolbox
 * binary on it. They don't require busybox!
 * 
 */
public class Toolbox {
    // regex to get pid out of ps line, example:
    // root 2611 0.0 0.0 19408 2104 pts/2 S 13:41 0:00 bash
    protected static final String PS_REGEX = "^\\S+\\s+([0-9]+).*$";
    protected static Pattern PS_PATTERN;
    static {
        PS_PATTERN = Pattern.compile(PS_REGEX);
    }

    private Shell shell;

    /**
     * All methods in this class are working with Androids toolbox. Toolbox is similar to busybox,
     * but normally shipped on every Android OS.
     * 
     * @param shell
     *            where to execute commands on
     */
    public Toolbox(Shell shell) {
        super();
        this.shell = shell;
    }

    /**
     * Checks if user accepted root access
     * 
     * (commands: id)
     * 
     * @return true if user has given root access
     * @throws IOException
     * @throws TimeoutException
     * @throws BrokenBusyboxException
     */
    public boolean isRootAccessGiven() throws BrokenBusyboxException, TimeoutException, IOException {
        SimpleCommand idCommand = new SimpleCommand("id");
        shell.add(idCommand).waitForFinish();

        if (idCommand.getOutput().contains("uid=0")) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * This command class gets all pids to a given process name
     */
    private class PsCommand extends Command {
        private String processName;
        private ArrayList<String> pids;

        public PsCommand(String processName) {
            super("ps");
            this.processName = processName;
            pids = new ArrayList<String>();
        }

        public ArrayList<String> getPids() {
            return pids;
        }

        public String getPidsString() {
            StringBuilder sb = new StringBuilder();
            for (String s : pids) {
                sb.append(s);
                sb.append(" ");
            }

            return sb.toString();
        }

        @Override
        public void output(int id, String line) {
            if (line.contains(processName)) {
                Matcher psMatcher = PS_PATTERN.matcher(line);

                try {
                    if (psMatcher.find()) {
                        String pid = psMatcher.group(1);
                        // add to pids list
                        pids.add(pid);
                        Log.d(Constants.TAG, "Found pid: " + pid);
                    } else {
                        Log.d(Constants.TAG, "Matching in ps command failed!");
                    }
                } catch (Exception e) {
                    Log.d(Constants.TAG, "Error with regex!");
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void afterExecution(int id, int exitCode) {
        }

    }

    /**
     * This method can be used to kill a running process
     * 
     * (commands: ps, kill)
     * 
     * @param processName
     *            name of process to kill
     * @return <code>true</code> if process was found and killed successfully
     * @throws IOException
     * @throws TimeoutException
     * @throws BrokenBusyboxException
     */
    public boolean killAll(String processName) throws BrokenBusyboxException, TimeoutException,
            IOException {
        Log.d(Constants.TAG, "Killing process " + processName);

        PsCommand commandPs = new PsCommand(processName);
        shell.add(commandPs).waitForFinish();

        // kill processes
        if (!commandPs.getPids().isEmpty()) {
            // example: kill -9 1234 1222 5343
            SimpleCommand killCommand = new SimpleCommand("kill -9 " + commandPs.getPidsString());
            shell.add(killCommand).waitForFinish();

            if (killCommand.getExitCode() == 0) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    /**
     * This method can be used to to check if a process is running
     * 
     * @param processName
     *            name of process to check
     * @return <code>true</code> if process was found
     * @throws IOException
     * @throws BrokenBusyboxException
     * @throws TimeoutException
     *             (Could not determine if the process is running)
     */
    boolean isProcessRunning(final String processName) throws BrokenBusyboxException,
            TimeoutException, IOException {
        PsCommand commandPs = new PsCommand(processName);
        shell.add(commandPs).waitForFinish();

        // if pids are available process is running!
        if (!commandPs.getPids().isEmpty()) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Copys file
     * 
     * (commands: dd)
     * 
     * @param source
     * @param destination
     * @param remountAsRw
     * @param preservePermissions
     * @throws IOException
     * @throws BrokenBusyboxException
     * @throws TimeoutException
     */
    public boolean copyFile(String source, String destination, boolean remountAsRw,
            boolean preservePermissions) throws BrokenBusyboxException, IOException,
            TimeoutException {

        // TODO: implement remount and preservePerm
        /*
         * dd can only copy files, but we can not check if the source is a file without invoking
         * shell commands, because from Java we probably have no read access, thus we only check if
         * they are ending with trailing slashes
         */
        if (source.endsWith("/") || destination.endsWith("/")) {
            throw new FileNotFoundException("dd can only copy files!");
        }

        boolean commandSuccess = false;

        SimpleCommand ddCommand = new SimpleCommand("dd if=" + source + " of=" + destination);
        shell.add(ddCommand).waitForFinish();

        if (ddCommand.getExitCode() == 0) {
            commandSuccess = true;
        } else {
            // try cat if dd fails
            SimpleCommand catCommand = new SimpleCommand("cat " + source + " > " + destination);
            shell.add(catCommand).waitForFinish();

            if (catCommand.getExitCode() == 0) {
                commandSuccess = true;
            }
        }

        return commandSuccess;
    }

    public static final int REBOOT_HOTREBOOT = 1;
    public static final int REBOOT_REBOOT = 2;
    public static final int REBOOT_SHUTDOWN = 3;
    public static final int REBOOT_RECOVERY = 4;

    /**
     * Shutdown or reboot device. Possible actions are REBOOT_HOTREBOOT, REBOOT_REBOOT,
     * REBOOT_SHUTDOWN, REBOOT_RECOVERY
     * 
     * @param action
     * @throws IOException
     * @throws TimeoutException
     * @throws BrokenBusyboxException
     */
    public void reboot(int action) throws BrokenBusyboxException, TimeoutException, IOException {
        if (action == REBOOT_HOTREBOOT) {
            killAll("system_server");
            // or: killAll("zygote");
        } else {
            String command;
            switch (action) {
            case REBOOT_REBOOT:
                command = "reboot";
                break;
            case REBOOT_SHUTDOWN:
                command = "reboot -p";
                break;
            case REBOOT_RECOVERY:
                command = "reboot recovery";
                break;
            default:
                command = "reboot";
                break;
            }

            SimpleCommand rebootCommand = new SimpleCommand(command);
            shell.add(rebootCommand).waitForFinish();

            if (rebootCommand.getExitCode() == -1) {
                Log.e(Constants.TAG, "Reboot failed!");
            }
        }
    }

    /**
     * This command checks if a file exists
     */
    private class FileExistsCommand extends Command {
        private String file;
        private boolean fileExists = false;

        public FileExistsCommand(String file) {
            super("ls " + file);
            this.file = file;
        }

        public boolean isFileExists() {
            return fileExists;
        }

        @Override
        public void output(int id, String line) {
            if (line.trim().equals(file)) {
                fileExists = true;
            }
        }

        @Override
        public void afterExecution(int id, int exitCode) {
        }

    }

    /**
     * Use this to check whether or not a file exists on the filesystem.
     * 
     * @param file
     *            String that represent the file, including the full path to the file and its name.
     * 
     * @return a boolean that will indicate whether or not the file exists.
     * @throws IOException
     * @throws TimeoutException
     * @throws BrokenBusyboxException
     * 
     */
    public boolean fileExists(final String file) throws BrokenBusyboxException, TimeoutException,
            IOException {
        FileExistsCommand fileExistsCommand = new FileExistsCommand(file);
        shell.add(fileExistsCommand).waitForFinish();

        if (fileExistsCommand.isFileExists()) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * This will take a path, which can contain the file name as well, and attempt to remount the
     * underlying partition.
     * <p/>
     * For example, passing in the following string:
     * "/system/bin/some/directory/that/really/would/never/exist" will result in /system ultimately
     * being remounted. However, keep in mind that the longer the path you supply, the more work
     * this has to do, and the slower it will run.
     * 
     * @param file
     *            file path
     * @param mountType
     *            mount type: pass in RO (Read only) or RW (Read Write)
     * @return a <code>boolean</code> which indicates whether or not the partition has been
     *         remounted as specified.
     */
    public boolean remount(String file, String mountType) {
        // Recieved a request, get an instance of Remounter
        Remounter remounter = new Remounter();
        // send the request.
        return (remounter.remount(file, mountType));
    }

    /**
     * This will tell you how the specified mount is mounted. rw, ro, etc...
     * <p/>
     * 
     * @param The
     *            mount you want to check
     * 
     * @return <code>String</code> What the mount is mounted as.
     * @throws Exception
     *             if we cannot determine how the mount is mounted.
     */
    static String getMountedAs(String path) throws Exception {
        ArrayList<Mount> mounts = Remounter.getMounts();
        if (mounts != null) {
            for (Mount mount : mounts) {
                if (path.contains(mount.getMountPoint().getAbsolutePath())) {
                    Log.d(Constants.TAG, (String) mount.getFlags().toArray()[0]);
                    return (String) mount.getFlags().toArray()[0];
                }
            }

            throw new Exception();
        } else {
            throw new Exception();
        }
    }

}
