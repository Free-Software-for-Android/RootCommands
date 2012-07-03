/*
 * Copyright (C) 2012 Dominik Schürmann <dominik@dominikschuermann.de>
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

import org.rootcommands.util.ShellResult;

/**
 * Class to provide direct execution of a command on the shell with or without result object
 * 
 */
public class DirectShell {
    private ShellExecutor executor;

    /**
     * Direct execution of commands
     * 
     * @param executor
     *            where to execute commands on
     */
    public DirectShell(ShellExecutor executor) {
        super();
        this.executor = executor;
    }

    public void executeCommand(String command, ShellResult result) {

    }

    public void executeCommand(String command) {

    }
}
