LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_SRC_FILES := hello_world.c
LOCAL_LDLIBS := -llog

LOCAL_MODULE := hello_world

include $(BUILD_EXECUTABLE)