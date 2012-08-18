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

import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.TimeoutException;

import org.rootcommands.util.Constants;
import org.rootcommands.util.Log;

public abstract class Command {
    final String command[];
    boolean finished = false;
    int exitCode;
    int id;
    int timeout = Constants.defaultTimeout;

    Shell shell = null;

    public Command(int id, String... command) {
        this.command = command;
        this.id = id;
    }

    public Command(int id, int timeout, String... command) {
        this.command = command;
        this.id = id;
        this.timeout = timeout;
    }

    public String getCommand() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < command.length; i++) {
            sb.append(command[i]);
            sb.append('\n');
        }
        Log.d(Constants.TAG, "Sending command(s): " + sb.toString());
        return sb.toString();
    }

    public void writeCommand(OutputStream out) throws IOException {
        out.write(getCommand().getBytes());
    }

    public abstract void output(int id, String line);

    public abstract void afterExecution(int id, int exitCode);

    public void commandFinished(int id) {
        Log.d(Constants.TAG, "Command " + id + " finished.");
    }

    public void setExitCode(int code) {
        synchronized (this) {
            exitCode = code;
            finished = true;
            commandFinished(id);
            this.notifyAll();
        }
    }

    public void setShell(Shell shell) {
        this.shell = shell;
    }

    /**
     * Closes all shells
     * 
     * @param reason
     */
    public void terminate(String reason) {
        try {
            shell.close();
            Log.d(Constants.TAG, "Terminating all shells.");
            terminated(reason);
        } catch (IOException e) {
        }
    }

    public void terminated(String reason) {
        setExitCode(-1);
        Log.d(Constants.TAG, "Command " + id + " did not finish, because of " + reason);
    }

    /**
     * Waits for this command to finish and forwards exitCode into afterExecution method
     * 
     * @param timeout
     * @throws InterruptedException
     * @throws TimeoutException
     */
    public void waitForFinish() throws InterruptedException, TimeoutException {
        synchronized (this) {
            while (!finished) {
                this.wait(timeout);

                if (!finished) {
                    finished = true;
                    terminate("Timeout");
                    throw new TimeoutException("Timeout has occurred.");
                }
            }

            afterExecution(id, exitCode);
        }
    }

}