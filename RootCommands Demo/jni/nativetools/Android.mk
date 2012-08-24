LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)

LOCAL_SRC_FILES := \
	nt_df.cpp \
	nt_du.cpp \
	nt_file_exists.cpp \
	nt_get_owner.cpp \
	nt_list_links.cpp \
	nt_mounter.cpp \
	nt_read_file.cpp \
	nt_recursive_chown.cpp \
	nt_recursive_cp.cpp \
	nt_recursive_crawl.cpp \
	nt_recursive_remove.cpp \
	\
	nt_utils.cpp \
	nativetools.cpp

LOCAL_C_INCLUDES := external/cfr/lib

LOCAL_CFLAGS := -Os -g -W -Wall \
	-DHAVE_UNISTD_H \
	-DHAVE_ERRNO_H \
	-DHAVE_NETINET_IN_H \
	-DHAVE_SYS_IOCTL_H \
	-DHAVE_SYS_MMAN_H \
	-DHAVE_SYS_MOUNT_H \
	-DHAVE_SYS_PRCTL_H \
	-DHAVE_SYS_RESOURCE_H \
	-DHAVE_SYS_SELECT_H \
	-DHAVE_SYS_STAT_H \
	-DHAVE_SYS_TYPES_H \
	-DHAVE_STDLIB_H \
	-DHAVE_STRDUP \
	-DHAVE_MMAP \
	-DHAVE_UTIME_H \
	-DHAVE_GETPAGESIZE \
	-DHAVE_LSEEK64 \
	-DHAVE_LSEEK64_PROTOTYPE \
	-DHAVE_EXT2_IOCTLS \
	-DHAVE_LINUX_FD_H \
	-DHAVE_TYPE_SSIZE_T


LOCAL_MODULE := nativetools
LOCAL_MODULE_TAGS := eng
LOCAL_SYSTEM_SHARED_LIBRARIES := libc


include $(BUILD_EXECUTABLE)
