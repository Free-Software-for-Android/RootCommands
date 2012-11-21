package org.sufficientlysecure.rootcommands;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.util.Log;

public class RootCommandsService extends IRootCommandsService.Stub {
    private static final String TAG = "RootCommandsService";
    private TestWorkerThread mWorker;
    private RootCommandsWorkerHandler mHandler;
    private Context mContext;

    public RootCommandsService() {
        super();
        mWorker = new TestWorkerThread("RootCommandsService");
        mWorker.start();
        Log.i(TAG, "Spawned worker thread for RootCommandsService");
    }

    public void setValue(int val) {
        Log.i(TAG, "setValue " + val);
        Message msg = Message.obtain();
        msg.what = RootCommandsWorkerHandler.MESSAGE_SET;
        msg.arg1 = val;
        mHandler.sendMessage(msg);
    }

    private class TestWorkerThread extends Thread {
        public TestWorkerThread(String name) {
            super(name);
        }

        public void run() {
            Looper.prepare();

            try {
                // Context mContext = ActivityManagerService.self().mContext
                Class<?> activityManagerServiceCls = Class
                        .forName("com.android.server.am.ActivityManagerService");

                // activityManagerServiceCls.se

                for (Field f : activityManagerServiceCls.getDeclaredFields()) {
                    Log.d(TAG, "f " + f.getName().toString());
                }

                Method methodSelf = activityManagerServiceCls.getDeclaredMethod("self",
                        (Class<?>[]) null);

                Object objMSelf = methodSelf.invoke(null, (Object[]) null);

                // Field fieldSelf = activityManagerServiceCls.getDeclaredField("mSelf");
                // fieldSelf.setAccessible(true);

                // activityManagerServiceCls.getC
                // Object objMSelf = fieldSelf.get(activityManagerServiceCls);

                // activityManagerServiceCls.
                // Object objMSelf = fieldSelf.get(null);
                Log.d(TAG, "objMSelf " + objMSelf.getClass().getName().toString());
                for (Field f : objMSelf.getClass().getDeclaredFields()) {
                    Log.d(TAG, "f " + f.getName().toString());
                }

                Field fieldMContext = activityManagerServiceCls.getDeclaredField("mContext");
                fieldMContext.setAccessible(true);
                mContext = (Context) fieldMContext.get(objMSelf);

            } catch (Exception e) {
                Log.e(TAG, "Exception while trying to get context!", e);
            }

            mHandler = new RootCommandsWorkerHandler();
            Looper.loop();
        }
    }

    private class RootCommandsWorkerHandler extends Handler {
        private static final int MESSAGE_SET = 0;

        @TargetApi(8)
        @Override
        public void handleMessage(Message msg) {
            try {
                if (msg.what == MESSAGE_SET) {
                    Log.i(TAG, "set message received: " + msg.arg1);

                    // reboot
                    PowerManager pm = (PowerManager) mContext
                            .getSystemService(Context.POWER_SERVICE);
                    pm.reboot("recovery");
                    pm.reboot(null);
                }
            } catch (Exception e) {
                // Log, don't crash!
                Log.e(TAG, "Exception in TestWorkerHandler.handleMessage:", e);
            }
        }
    }
}