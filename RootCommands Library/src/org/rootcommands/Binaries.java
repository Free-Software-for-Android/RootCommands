/*
 * Copyright (C) 2012 Dominik Schürmann <dominik@dominikschuermann.de>
 * Copyright (c) 2012 Jack Palevich (Android-Terminal-Emulator)
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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.rootcommands.util.Constants;
import org.rootcommands.util.Log;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.preference.PreferenceManager;

public class Binaries {
    private Context context;
    private Shell shell;
    private SharedPreferences prefs;

    private static final String BINARY_SUBDIR = "bin";

    /**
     * This class provides ways to deploy and use your own binaries
     * 
     * You need a root shell on Android < 2.3 to set the executable permission! On Android >= 2.3
     * shell can be null, as this can be done using official API now!
     * 
     * 
     * TODO:
     * 
     * Problems with resources
     * http://ponystyle.com/blog/2010/03/26/dealing-with-asset-compression-in-android-apps/
     * 
     * Android < 2.3 can't uncompress files over 1MB from res/ and assets/ when they are compressed!
     * 
     * Workaround by using .png for example!
     * 
     * Check if this really is also is true for res/raw and not only for assets/
     * 
     * 
     * @param context
     * @param shell
     *            where to execute commands on
     */
    public Binaries(Context context, Shell shell) {
        super();
        this.context = context;
        this.shell = shell;

        // get shared preferences
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
    }

    /**
     * Install binary with versioning. This means the binary is redeployed everytime the application
     * itself is updated to a new version.
     * 
     * Binaries have to be included as busybox-x86 or busybox-arm in the assets folder of your
     * android project
     * 
     * @param binaryName
     */
    public void installBinary(String binaryName) {
        File binary = new File(context.getFilesDir().getPath() + File.separator + BINARY_SUBDIR
                + File.separator + binaryName);

        // if there is no binary, redeploy!
        if (!binary.exists()) {
            deployBinary(binaryName);
        } else {
            // redeploy if the package is newer than the binary deployed
            if (getBinaryVersion(binaryName) <= getPackageVersion()) {
                deployBinary(binaryName);
            }
        }
    }

    /**
     * Deploy binary to private directory files/bin/ of your app. This is done without any version
     * check!
     * 
     * On Android >= 2.3, deployed binaries can be set as executeable without root, before 2.3 you
     * need root to set executeable permission!
     * 
     * @param binaryName
     */
    @SuppressLint("NewApi")
    public void deployBinary(String binaryName) {
        String arch = getArch();

        InputStream src = null;
        try {
            src = context.getAssets().open(binaryName + "-" + arch);
        } catch (Exception e) {
            Log.e(Constants.TAG, "Does the binary exists for this arch?", e);
        }

        FileOutputStream dst = null;
        File binary = null;
        try {
            File dir = context.getDir(BINARY_SUBDIR, Context.MODE_PRIVATE);
            binary = new File(dir, binaryName);

            dst = new FileOutputStream(binary);
        } catch (FileNotFoundException e) {
            Log.e(Constants.TAG, "Output file problem!", e);
        }

        // deploy it!
        copyStream(src, dst);

        // set executable
        // only on API 9 or higher without root!
        if (Build.VERSION.SDK_INT >= 9) {
            binary.setExecutable(true);
        } else {
            Toolbox tb = new Toolbox(shell);
            try {
                tb.setFilePermissions(binary.getAbsolutePath(), "755");
            } catch (Exception e) {
                Log.e(Constants.TAG, "Setting executeable permissions with root failed!", e);
            }
        }

        // after successfull deploy, save current version of binary to shared preferences
        setBinaryVersion(binaryName, getPackageVersion());
    }

    /**
     * Get version code of application itself
     * 
     * @return -1 if unsuccessfull
     */
    private int getPackageVersion() {
        try {
            PackageInfo pinfo = context.getPackageManager().getPackageInfo(
                    context.getPackageName(), 0);

            return pinfo.versionCode;
        } catch (NameNotFoundException e) {
            Log.e(Constants.TAG, "Could not get package version!");
            return -1;
        }
    }

    /**
     * Save version as sharedpreference for binary
     * 
     * @param binaryName
     * @param version
     */
    private void setBinaryVersion(String binaryName, int version) {
        // mark this version as installed
        Editor e = prefs.edit();
        e.putInt("rootcommands_" + binaryName + "_version", version);
        e.commit();
    }

    /**
     * Get current version of binary from shared preferences
     * 
     * @param binaryName
     * @return
     */
    private int getBinaryVersion(String binaryName) {
        return prefs.getInt("rootcommands_" + binaryName + "_version", 0);
    }

    /**
     * Returns architecture of device
     * 
     * @return
     */
    private String getArch() {
        /* Returns the value of uname -m */
        String machine = System.getProperty("os.arch");
        Log.d(Constants.TAG, "os.arch is " + machine);

        /* Convert machine name to our arch identifier */
        if (machine.matches("armv[0-9]+(tej?)?l")) {
            return "arm";
        } else if (machine.matches("i[3456]86")) {
            return "x86";
        } else if (machine.equals("OS_ARCH")) {
            /*
             * This is what API < 5 devices seem to return. Presumably all of these are ARM devices.
             */
            return "arm";
        } else {
            /*
             * Result is correct for mips, and this is probably the best thing to do for an unknown
             * arch
             */
            return machine;
        }
    }

    /**
     * Copy a stream from input to output
     * 
     * @param dst
     * @param src
     * @throws IOException
     */
    private void copyStream(InputStream src, OutputStream dst) {
        byte[] buffer = new byte[4096];
        int bytesRead = 0;
        try {
            while ((bytesRead = src.read(buffer)) >= 0) {
                dst.write(buffer, 0, bytesRead);
            }
            dst.close();
        } catch (IOException e) {
            Log.e(Constants.TAG, "Problem while copying!", e);
        }
    }
}
