#nativetools

The native code companion to [Root Tools](http://code.google.com/p/roottools/).

##Overview

This project is still in its infancy; the source code was used in Android applications such as NotEnoughSpace. 

NotEnoughSpace is a Java application that performs root-level operations on your device. It does so by invoking native binaries like most Android root applications do (a good chunk of code in RootTools comes from NES).
However, in order to not be dependent on third-party packages such as BusyBox, most of the native functionality was written from scratch.

I refactored most of that native code and am now releasing it as a dual-license project, allowing you to use it in your own GPL project or in a commercial project (you will need my permission first for non-GPL uses)

## Licensing

Please have a look at the LICENSE file to learn about your options when using this code. See the FAQ at the end of this document if you are curious about my motivation for dual-licensing.

## Usage

### Linking

This code can be incorporated directly in your own software or may be kept independent and you may invoke it through the shell. Note that this code does not have to be run by a root user, as this will depend on your needs.

### Syntax

When you build a binary, if can be invoked one of two ways:

#### The argument way

Assuming that you are preserving the package's default name of `nativetools`:

    nativetools <action name> <action parameters>

For instance:

    nativetools df /data/data

which will return disk usage information for that partition.

#### The alias way

Like other packages, such as BusyBox, you can create aliases to the main binary and it will detect which command you are trying to execute based on that alias' name.

For instance:

    ln -s nativetools df
    ./df /data/data

which will behave the same way as the previous example.

#### Output

Currently, the output produced by this code is very simplistic: our goal is to provide output that can be easily parsed by another process of piece of code.

We may, in the future, provide a more "shell-like" output, should the need arise.

All output typically looks like this:

    < letter >,< value#1 >,< value#2 >,…,< value#n >

`< letter >` is a convenient way to confirm that the output matches the command you issued. For instance, `df /data/data` will return:

    D,152576,144420,8156,4096

The first letter will confirm that this is the output for 'df', the first number will be the partition's total size, the second one will be space used, the third one will be space available and the last one will be that partition's block size.

### Building

If you are not building for Android, you are welcome to use your own toolchain.

If you are building for Android, several toolchains are available.

#### Using the ndk

You need to install the ndk first and set your path correctly.

When this is done, you can build the source tree using:

    ./ndk-comp++ nativetools.cpp < applet#1 > … < applet#n > -o nativetools

#### Using a ROM toolchain

For instance, to build using the CyanogenMod7 toolchain:

    cd < toolchain path >/android/system
    mkdir external/nativetools

Drop your files in this new directory,

Edit `build/core/main.mk`. This line:

        external/zlib \

becomes:

        external/nativetools \
        external/zlib \ 

(You may need to modify the file in multiple places)

Don't forget that Cyanogen lets you build a single directory:

    mmm external/nativetools
    
### Currently available

These commands are currently implemented:

* df < partition > *partition usage*
* du < directory path > *directory usage*
* fe < file path > *checks whether file exists*
* go < file path > *retrieve files owner id*
* ll < directory path > *list links*
* ml, mr, mw **are currently disabled** *mount devices/loop devices*
* rf < file path > *display file content*
* co < directory path > < max depth > < owner > *recursively change owner*
* cp < source path > < destination path > *recursively copy files*
* cr < directory path > *crawl directory structure and display file stats*
* rm < directory path > *recursively delete directory structure*

### Creating new applets

Adding new commands is very simple as each command is defined as a C or C++ applet.

#### Step 1: Create new applet

Create a new file that you will store in nativetool's main directory. For example, let's call this file `nt_my_applet.cpp`

Here is the minimum code that you will need to put in this file:

    #include "nativetools.hpp"
    int nt_my_applet(int argc, char** argv, char** env) {
        int ret = EXIT_SUCCESS;
        return ret;
    }

Obviously, this applet doesn't do anything.

#### Step 2: Tell everybody about the applet

First, add the applet to `Android.mk` so that it will build:

    LOCAL_SRC_FILES := \
        ...
        nt_my_applet.cpp \
    	\
    	nt_utils.cpp \
    	nativetools.cpp

Now, declare the applet so that nativetools will be aware of its existence. In `nt_applets.hpp`:

	APPLET(nt_my_applet);

Be sure to declare it in the correct section (C or C++)

Then:

	APPLET_DEF applets[] = {
        …
        {"myapplet", &nt_my_applet}
    };

That's all!

## FAQ

**Why the dual license?**

Because [I agree with Zed Shaw](http://zedshaw.com/essays/why_i_gpl.html). Of course, I did not produce software that's as broadly used as his so that may explain why I do not have a chip on my shoulder the size of his.

So, that's it for GPL. The commercial license is here as an attempt to offer a straightforward solution for you if you are interested in a closed source use (I don't have much time for consulting)

**Can I fork this project?**

You are welcome to as long as you abide by the GPL license. Your code will be GPL'd as well.

**Can I use this in my ROM?**

You are welcome to as long as said ROM's source code is available for public consumption.