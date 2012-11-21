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
        try {
            Log.i(TAG, "RootCommandsRun script started!");

            Log.i(TAG, "Trying to add system service!");
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
