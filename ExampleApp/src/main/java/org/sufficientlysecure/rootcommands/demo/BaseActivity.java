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

package org.sufficientlysecure.rootcommands.demo;

import org.sufficientlysecure.rootcommands.RootCommands;
import org.sufficientlysecure.rootcommands.Shell;
import org.sufficientlysecure.rootcommands.Toolbox;
import org.sufficientlysecure.rootcommands.command.Command;
import org.sufficientlysecure.rootcommands.command.SimpleExecutableCommand;
import org.sufficientlysecure.rootcommands.command.SimpleCommand;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

public class BaseActivity extends Activity {
    public static final String TAG = "Demo";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        // enable debug logging
        RootCommands.DEBUG = true;
    }

    private class MyCommand extends Command {
        private static final String LINE = "hosts";
        boolean found = false;

        public MyCommand() {
            super("ls -la /system/etc/");
        }

        public boolean isFound() {
            return found;
        }

        @Override
        public void output(int id, String line) {
            if (line.contains(LINE)) {
                Log.d(TAG, "Found it!");
                found = true;
            }
        }

        @Override
        public void afterExecution(int id, int exitCode) {
        }

    }

    public void commandsTestOnClick(View view) {
        try {
            // start root shell
            Shell shell = Shell.startRootShell();

            // simple commands
            SimpleCommand command0 = new SimpleCommand("echo this is a command",
                    "echo this is another command");
            SimpleCommand command1 = new SimpleCommand("toolbox ls");
            SimpleCommand command2 = new SimpleCommand("ls -la /system/etc/hosts");

            shell.add(command0).waitForFinish();
            shell.add(command1).waitForFinish();
            shell.add(command2).waitForFinish();

            Log.d(TAG, "Output of command2: " + command2.getOutput());
            Log.d(TAG, "Exit code of command2: " + command2.getExitCode());

            // custom command classes:
            MyCommand myCommand = new MyCommand();
            shell.add(myCommand).waitForFinish();

            Log.d(TAG, "myCommand.isFound(): " + myCommand.isFound());

            // close root shell
            shell.close();
        } catch (Exception e) {
            Log.e(TAG, "Exception!", e);
        }
    }

    public void toolboxTestOnClick(View view) {
        try {
            Shell shell = Shell.startRootShell();

            Toolbox tb = new Toolbox(shell);

            if (tb.isRootAccessGiven()) {
                Log.d(TAG, "Root access given!");
            } else {
                Log.d(TAG, "No root access!");
            }

            Log.d(TAG, tb.getFilePermissions("/system/etc/hosts"));

            shell.close();
        } catch (Exception e) {
            Log.e(TAG, "Exception!", e);
        }
    }

    public void binariesTestOnClick(View view) {
        try {
            SimpleExecutableCommand binaryCommand = new SimpleExecutableCommand(this, "hello_world", "");

            // started as normal shell without root, but you can also start your binaries on a root
            // shell if you need more privileges!
            Shell shell = Shell.startShell();

            shell.add(binaryCommand).waitForFinish();

            Toolbox tb = new Toolbox(shell);
            if (tb.killAllExecutable("hello_world")) {
                Log.d(TAG, "Hello World daemon killed!");
            } else {
                Log.d(TAG, "Killing failed!");
            }

            shell.close();
        } catch (Exception e) {
            Log.e(TAG, "Exception!", e);
        }
    }

}