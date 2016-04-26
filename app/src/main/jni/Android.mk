LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := AndroidImageFilter
LOCAL_SRC_FILES := com_istudy_helper_filters_NativeFilterFunc.cpp \
                    AndroidImageFilter.cpp \
					PixelateFilter.cpp

LOCAL_LDLIBS := -lm -llog

include $(BUILD_SHARED_LIBRARY)