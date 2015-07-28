LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
APP_ABI := armeabi-v7a
LOCAL_MODULE := MotorControl
LOCAL_SRC_FILES := MotorControl.c
LOCAL_LDLIBS    := -llog
include $(BUILD_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE_TAGS := eng
LOCAL_C_INCLUDES += $(LOCAL_PATH) $(LOCAL_PATH)/$(KERNEL_DIR)/include
LOCAL_SRC_FILES := tools/i2cbusses.c tools/util.c
LOCAL_MODULE := i2c-tools
LOCAL_LDLIBS    := -llog
include $(BUILD_STATIC_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE_TAGS := eng
LOCAL_SRC_FILES:=tools/i2cTools.c
LOCAL_MODULE:=i2cTools
LOCAL_LDLIBS    := -llog
LOCAL_CPPFLAGS += -DANDROID
LOCAL_SHARED_LIBRARIES:=libc
LOCAL_STATIC_LIBRARIES := i2c-tools
LOCAL_C_INCLUDES += $(LOCAL_PATH) $(LOCAL_PATH)/$(KERNEL_DIR)/include
include $(BUILD_SHARED_LIBRARY)