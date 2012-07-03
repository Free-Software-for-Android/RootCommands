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

import org.rootcommands.ShellExecutor;
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

    public void testOnClick(View view) {
        ShellExecutor exec = new ShellExecutor(true, 100, null, null, 25000);
        exec.openShell();

        Toolbox toolbox = new Toolbox(exec);

        Log.d(Constants.TAG, "access?:" + toolbox.isAccessGiven());

        Log.d(Constants.TAG, "kill blank_webserver?:" + toolbox.killProcess("blank_webserver"));

        exec.closeShell();

    }
}