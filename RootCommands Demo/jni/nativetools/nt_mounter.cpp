// (c) Chris F. Ravenscroft, VoilaWeb. For licensing information, check attached LICENSE file.

#include "nativetools.hpp"

/*
 * For some weird reason this is dead code..?
 */

int nt_mount_loop(int argc, char** argv, char** env) {
    int ret = EXIT_SUCCESS;

    if(argc != 2) {
        ret = nt_error("Wrong # of arguments for %s: %d", __FUNCTION__, argc);
    }
    else {
        char *s = argv[1];
	    dev_t deviceid;
	    int fMountPoint = -1, fDevFile = -1;
	    const char *devfile = "/sdcard/data/noensp/bogus";
	    const char *mountpoint = "/data/bogus";

	    fMountPoint = open(mountpoint, O_RDWR);
	    if(fMountPoint < 0) {
	        ret = nt_error("%s:#1", __FUNCTION__);
	        goto mount_over;
	    }
	    char devname[128];
	    sprintf(devname, "/dev/loop%d", 99);
	    deviceid = makedev(7, 99);
	    if(mknod(devname, S_IFBLK | 0644, deviceid) < 0) {
	        ret = nt_error("%s:#2", __FUNCTION__);
	        goto mount_over;
	    }
	    fDevFile = open(devfile, O_RDWR);
	    if(fDevFile < 0) {
	        ret = nt_error("%s:#3", __FUNCTION__);
	        goto mount_over;
	    }
	    if(ioctl(fDevFile, 0x4C00, fMountPoint) < 0) {
	        ret = nt_error("%s:#4", __FUNCTION__);
	        goto mount_over;
	    }

mount_over:
	    if(fDevFile > -1)
	        close(fDevFile);
	    if(fMountPoint > -1)
	        close(fMountPoint);
	}

    return ret;
}

int nt_mount_read_write(int argc, char** argv, char** env) {
    int ret = EXIT_SUCCESS;

    if(argc != 3) {
        ret = nt_error("Wrong # of arguments for %s: %d", __FUNCTION__, argc);
    }
    else {
    	bool readwrite   = (!strcmp(argv[0], "mw"));
        char *dev 	     = argv[1];
        char *mountpoint = argv[2];
	}

    return ret;
}