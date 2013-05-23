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

package org.sufficientlysecure.rootcommands;

import java.lang.reflect.Method;

import android.os.IBinder;
import android.util.Log;

public final class RootCommandsRun {

    public static final String TAG = "RootCommandsRun";

    public static void main(String[] args) {
        new RootCommandsRun().run(args);
    }

    public void run(String[] args) {
        Log.i(TAG, "RootCommandsRun script started!");

        Log.i(TAG,
                "Trying to add RootCommandsService as system service with android.os.ServiceManager.addService()");

        try {
            // ServiceManager.addService("RootCommandsService", new RootCommandsService(context));
            Class<?> serviceManagerCls = Class.forName("android.os.ServiceManager");
            Method methodAddService = serviceManagerCls.getDeclaredMethod("addService",
                    String.class, IBinder.class);
            methodAddService.invoke(null, new String("RootCommandsService"),
                    new RootCommandsService());
        } catch (Throwable e) {
            Log.e(TAG, "Failure starting RootCommandsService", e);
        }
    }
}
