/*
 * Copyright (C) 2012 Dominik Schürmann <dominik@dominikschuermann.de>
 * Copyright (c) 2012 Stephen Erickson, Chris Ravenscroft, Dominik Schuermann, Adam Shanks (RootTools)
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
     */
    public boolean isRootAccessGiven() {
        boolean accessGiven = false;

        try {
            SimpleCommand idCommand = new SimpleCommand(0, "id");
            shell.add(idCommand).waitForFinish();

            if (idCommand.getOutput().contains("uid=0")) {
                accessGiven = true;
            }
        } catch (Exception e) {
            Log.e(Constants.TAG, "Command failed!", e);
        }

        return accessGiven;
    }

    /**
     * This command class gets all pids to a given process name
     */
    private class PsCommand extends Command {
        private String processName;
        private String pids = null;

        public PsCommand(int id, String processName) {
            super(id, "ps");
            this.processName = processName;
        }

        public String getPids() {
            return pids;
        }

        @Override
        public void output(int id, String line) {
            if (line.contains(processName)) {
                Matcher psMatcher = PS_PATTERN.matcher(line);

                try {
                    if (psMatcher.find()) {
                        String pid = psMatcher.group(1);
                        // concatenate to existing pids, to use later in kill
                        if (pids != null) {
                            pids += " " + pid;
                        } else {
                            pids = pid;
                        }
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
            Log.d(Constants.TAG, "ID: " + id + ", ExitCode: " + exitCode);
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
     */
    public boolean killAll(final String processName) {
        Log.d(Constants.TAG, "Killing process " + processName);

        try {
            PsCommand commandPs = new PsCommand(0, processName);
            shell.add(commandPs).waitForFinish();

            // kill processes
            if (commandPs.getPids() != null) {
                // example: kill -9 1234 1222 5343
                SimpleCommand killCommand = new SimpleCommand(1, "kill -9 " + commandPs.getPids());
                shell.add(killCommand).waitForFinish();

                if (killCommand.getExitCode() == 0) {
                    return true;
                } else {
                    return false;
                }
            } else {
                return false;
            }
        } catch (Exception e) {
            Log.e(Constants.TAG, "Command failed!", e);
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
     * @throws FileNotFoundException
     */
    public boolean copyFile(String source, String destination, boolean remountAsRw,
            boolean preservePermissions) throws FileNotFoundException {
        /*
         * dd can only copy files, but we can not check if the source is a file without invoking
         * shell commands, because from Java we probably have no read access, thus we only check if
         * they are ending with trailing slashes
         */
        if (source.endsWith("/") || destination.endsWith("/")) {
            throw new FileNotFoundException("dd can only copy files!");
        }

        boolean commandSuccess = false;
        try {
            SimpleCommand ddCommand = new SimpleCommand(0, "dd if=" + source + " of=" + destination);
            shell.add(ddCommand).waitForFinish();

            if (ddCommand.getExitCode() == 0) {
                commandSuccess = true;
            }
        } catch (Exception e) {
            Log.e(Constants.TAG, "Command failed!", e);
        }

        // try cat if dd fails
        if (commandSuccess == false) {
            try {
                SimpleCommand catCommand = new SimpleCommand(0, "cat " + source + " > "
                        + destination);
                shell.add(catCommand).waitForFinish();

                if (catCommand.getExitCode() == 0) {
                    commandSuccess = true;
                }
            } catch (Exception e) {
                Log.e(Constants.TAG, "Command failed!", e);
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
     */
    public void reboot(int action) {
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

            try {
                SimpleCommand rebootCommand = new SimpleCommand(0, command);
                shell.add(rebootCommand).waitForFinish();

                if (rebootCommand.getExitCode() == -1) {
                    Log.e(Constants.TAG, "Reboot failed!");
                }
            } catch (Exception e) {
                Log.e(Constants.TAG, "Command failed!", e);
            }
        }

    }
}
