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

package org.rootcommands.demo;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import org.rootcommands.SimpleCommand;
import org.rootcommands.Shell;
import org.rootcommands.Toolbox;
import org.rootcommands.util.Constants;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

public class BaseActivity extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
    }

    public void toolboxTestOnClick(View view) {
        // ShellExecutor exec = new ShellExecutor(true, 100, null, null, 25000);
        // exec.openShell();

        // Toolbox toolbox = new Toolbox(exec);
        //
        // Log.d(Constants.TAG, "access?:" + toolbox.isRootAccessGiven());
        //
        // Log.d(Constants.TAG, "access?:" + toolbox.isRootAccessGiven());

        // Log.d(Constants.TAG, "kill blank_webserver?:" + toolbox.killProcess("blank_webserver"));

        // exec.closeShell();

        // RootTools.debugMode = true;

        SimpleCommand command0 = new SimpleCommand("echo this is a command",
                "echo this is another command");

        SimpleCommand command1 = new SimpleCommand("toolbox ls");

        SimpleCommand command2 = new SimpleCommand("ls -la");

        SimpleCommand command3 = new SimpleCommand("echo Value too large for defined data type");

        Shell shell = null;
        try {
            shell = Shell.startCustomShell("su");

            Toolbox tb = new Toolbox(shell);

            if (tb.isRootAccessGiven()) {
                Log.d(Constants.TAG, "joooooo!");
            } else {
                Log.d(Constants.TAG, "nope!");
            }

            if (tb.killAll("blank_webserver")) {
                Log.d(Constants.TAG, "killed!");
            } else {
                Log.d(Constants.TAG, "nope!");
            }

            // shell.add(command3).waitForFinish();

            shell.close();

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        // try {
        // shell = Shell.startRootShell();
        //
        // shell.add(command0);
        // shell.add(command1);
        // shell.add(command2);
        //
        // } catch (IOException e) {
        // // TODO Auto-generated catch block
        // e.printStackTrace();
        // }
        //
        // try {
        // command0.waitForFinish();
        // command1.waitForFinish();
        // command2.waitForFinish();
        //
        // } catch (InterruptedException e) {
        // // TODO Auto-generated catch block
        // e.printStackTrace();
        // } catch (TimeoutException e) {
        // // TODO Auto-generated catch block
        // e.printStackTrace();
        // }

    }

    public void otherTestOnClick(View view) {

    }

    public void binariesTestOnClick(View view) {

    }
}