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

import org.rootcommands.util.Constants;
import org.rootcommands.util.Log;

public class SimpleCommand extends Command {
    private StringBuilder sb = new StringBuilder();
    private int exitCode = -1;

    public SimpleCommand(int id, String... command) {
        super(id, command);
    }

    @Override
    public void output(int id, String line) {
        sb.append(line).append('\n');
        Log.d(Constants.TAG, "ID: " + id + ", Output: " + line);
    }

    @Override
    public void afterExecution(int id, int exitCode) {
        this.exitCode = exitCode;
        Log.d(Constants.TAG, "ID: " + id + ", ExitCode: " + exitCode);
    }

    public String getOutput() {
        return sb.toString();
    }

    public int getExitCode() {
        return exitCode;
    }
}