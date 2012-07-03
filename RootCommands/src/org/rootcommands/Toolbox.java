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
import org.rootcommands.util.ShellResult;

/**
 * All methods in this class are working with Androids toolbox. Toolbox is similar to busybox, but
 * normally shipped on every Android OS. You can find toolbox commands on
 * https://github.com/CyanogenMod/android_system_core/tree/ics/toolbox
 * 
 * This means that these commands are designed to work on every Android OS, with a _working_ toolbox
 * binary on it. They don't require busybox!
 * 
 * Sadly sometimes toolbox is broken. This can be recognized by lines such as
 * "Stderr: ls: /system/bin/toolbox: Value too large for defined data type". We try to detect broken
 * versions here. It the same problem as some busybox versions have (see
 * https://code.google.com/p/busybox-android/issues/detail?id=1). It is giving
 * "Value too large for defined data type" on certain file operations (e.g. ls and chown) in certain
 * directories (e.g. /data/data)
 * 
 * Known roms with broken toolbox:
 * 
 * - stock rom Android 4 of Galaxy Note
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

    private ShellExecutor executor;

    /**
     * All methods in this class are working with Androids toolbox. Toolbox is similar to busybox,
     * but normally shipped on every Android OS.
     * 
     * @param executor
     *            where to execute commands on
     */
    public Toolbox(ShellExecutor executor) {
        super();
        this.executor = executor;
    }

    /**
     * Checks if user accepted root access
     * 
     * (commands: id)
     * 
     * @return true if user accepted root access
     */
    public boolean isAccessGiven() {
        ShellResult result = new ShellResult() {

            @Override
            public void processError(String line) {
                Log.d(Constants.TAG, "process error: " + line);
                setError(1);
                notifyThread();
            }

            @Override
            public void process(String line) {
                Log.d(Constants.TAG, "process: " + line);

                // check for uid=0 what means the current user id is root
                if (line.contains("uid=0")) {
                    setData(true);
                } else {
                    setData(false);
                }

                // wakeup, we have an answer
                notifyThread();
            }

            @Override
            public void onFailure(Exception ex) {
                Log.d(Constants.TAG, "failure: " + ex.getMessage());
                setError(1);
                notifyThread();
            }

        };
        executor.runCommand("id", result);

        // block thread until we have parsed the output via result object
        result.waitThread();

        if (result.getData() != null && result.getData().equals(true)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * This method can be used to kill a running process
     * 
     * @param processName
     *            name of process to kill
     * @return <code>true</code> if process was found and killed successfully
     */
    public boolean killProcess(final String processName) {
        // RootTools.log(InternalVariables.TAG, "Killing process " + processName);
        Log.d(Constants.TAG, "Killing process " + processName);

        boolean processKilled = false;
        // try {
        ShellResult result = new ShellResult() {
            @Override
            public void process(String line) {
                if (line.contains(processName)) {
                    Matcher psMatcher = PS_PATTERN.matcher(line);

                    try {
                        if (psMatcher.find()) {
                            String pid = psMatcher.group(1);
                            // concatenate to existing pids, to use later in kill
                            if (getData() != null) {
                                setData(getData() + " " + pid);
                            } else {
                                setData(pid);
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

                // notifyThread();
                // TODO: Big problem when to know that there is no entry with this id?
                // how to know that this is the end of output???
                // thus when to notify?
            }

            @Override
            public void onFailure(Exception ex) {
                Log.d(Constants.TAG, "failure: " + ex.getMessage());
                setError(1);
                notifyThread();
            }

            @Override
            public void processError(String line) {
                Log.d(Constants.TAG, "process error: " + line);
                setError(1);
                notifyThread();
            }

        };

        executor.runCommand("ps", result);

        // block thread until we have parsed the output via result object
        result.waitThread();

        // sendShell(new String[] { "ps" }, 1, result, -1);

        if (result.getError() == 0) {
            // get all pids in one string, created in process method
            String pids = (String) result.getData();

            // kill processes
            if (pids != null) {
                // example: kill -9 1234 1222 5343
                executor.runCommand("kill -9 " + pids, null);
                processKilled = true;
            }
        }

        return processKilled;
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
    public void copyFile(String source, String destination, boolean remountAsRw,
            boolean preservePermissions) throws FileNotFoundException {
        /*
         * dd can only copy files, but we can not check if the source is a file without invoking
         * shell commands, because from Java we probably have no read access, thus we only check if
         * they are ending with trailing slashes
         */
        if (source.endsWith("/") || destination.endsWith("/")) {
            throw new FileNotFoundException("dd can only copy files!");
        }

        executor.runCommand("dd if=" + source + " of=" + destination, null);
        // alternative: "cat " + source + " > " + destination
    }
}
