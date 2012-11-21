# RootCommands

This is a library to simplify the execution of root commands. It is a Java wrapper around native executables and can be used to execute specific root commands.

# Add the lib to your project

Put the newest jar from the Downloads section (https://github.com/dschuermann/root-commands/downloads) into the ``libs`` folder of your Android project.

## Alternative way

* New -> Android Project -> Create project from existing source, choose ``RootCommands Library``
* Add ``RootCommands Library`` as Android Library (Properties of your project -> Android -> Library -> add ``RootCommands Library`` as android library)

# Examples

To see RootCommands in action, compile the ``RootCommands Demo`` project and observe the logcat output.

## Debug Mode

You can enable debug logging in RootCommands by the following line:
```java
RootCommands.DEBUG = true;
```

## Root Access check

This tries to find the su binary, opens a root shell and checks for root uid.

```java
if (RootCommands.rootAccessGiven()) {
    // your code
}
```

## Simple Commands

You can instantiate SimpleCommands with the shell commands you want to execute. This is a very basic approach of executing something on a shell.

```java
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

// close root shell
shell.close();
```

## Define your own commands

For more complex commands you can extend the Command class to parse the output while the shell executes the command.

```java
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
```

```java
// start root shell
Shell shell = Shell.startRootShell();

// custom command classes:
MyCommand myCommand = new MyCommand();
shell.add(myCommand).waitForFinish();

Log.d(TAG, "myCommand.isFound(): " + myCommand.isFound());

// close root shell
shell.close();
```

## Toolbox

Toolbox is similar to busybox, but normally shipped on every Android OS. You can find toolbox commands on https://github.com/CyanogenMod/android_system_core/tree/ics/toolbox . This means that these commands are designed to work on every Android OS, with a _working_ toolbox binary on it. They don't require busybox!

The Toolbox class is based on this toolbox executeable and provides some nice commands as java methods like:

* isRootAccessGiven()
* killAll(String processName)
* isProcessRunning(String processName)
* getFilePermissions(String file)
* setFilePermissions(String file, String permissions)
* getSymlink(String file)
* copyFile(String source, String destination, boolean remountAsRw, boolean preservePermissions)
* reboot(int action)
* withWritePermissions(String file, WithPermissions withWritePermission)
* setSystemClock(long millis)
* remount(String file, String mountType)
* ...

```java
Shell shell = Shell.startRootShell();

Toolbox tb = new Toolbox(shell);

if (tb.isRootAccessGiven()) {
    Log.d(TAG, "Root access given!");
} else {
    Log.d(TAG, "No root access!");
}

Log.d(TAG, tb.getFilePermissions("/system/etc/hosts"));

shell.close();
```

## Binaries

Android APKs are normally not designed to include native binaries. But they are designed to include native libraries for different architectures, which are deployed when the app is installed on the device. Androids mechanism will deploy the proper native library based on the architecture of the device.
This method only deploys files that are named like ``lib*.so``, which are included from the libs folder of your project.

We are missusing Androids library method to deploy our native binaries, by renaming them after compilation, so that they are included in the apk and deployed based on the architecture.

Note: Permission and owner of deployed files: ``-rwxr-xr-x system   system      38092 2012-09-24 19:51 libhello_world_bin.so``

1. Put the sources of the native binaries into the libs folder as seen in https://github.com/dschuermann/root-commands/tree/master/RootCommands%20Demo/jni
2. Write your own Android.mk and Application.mk
3. To automate the renaming process use a custom ruleset for Ant: https://github.com/dschuermann/root-commands/blob/master/RootCommands%20Demo/custom_rules.xml . This will rename the files from ``*`` to ``lib*_bin.so``.
4. Build ``local.properties`` with sdk.dir and ndk.dir
5. Execute ``ant release`` to compile Java code, compile the native executables, rename them, and generate an apk.

Now that your binaries are bundled, you can use our ``SimpleBinaryCommand`` like in the following example:

```java
SimpleBinaryCommand binaryCommand = new SimpleBinaryCommand(this, "hello_world", "");

// started as normal shell without root, but you can also start your binaries on a root
// shell if you need more privileges!
Shell shell = Shell.startShell();

shell.add(binaryCommand).waitForFinish();

Toolbox tb = new Toolbox(shell);
if (tb.killAllBinary("hello_world")) {
    Log.d(TAG, "Hello World daemon killed!");
} else {
    Log.d(TAG, "Killing failed!");
}

shell.close();
```

# Contribute

Fork RootCommands and do a Pull Request. I will merge your changes back into the main project.

# RootCommands Service

## Compile service

1. ``ant release``
2. ``cd bin``
3. ``dx --dex --output=classes-dex.jar classes.jar``

# Authors
RootCommands is based on several other open source projects, thus several authors are involved:

* Dominik Schürmann (RootCommands)
* Michael Elsdörfer (Android Autostarts)
* Stephen Erickson, Chris Ravenscroft, Adam Shanks, Jeremy Lakeman (RootTools)