# Copyright (C) 2009 The Android Open Source Project
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_CPPFLAGS +=  -fpermissive

LOCAL_C_INCLUDES += \
		/android_source/frameworks/av/include/media/stagefright \
		/android_source/frameworks/native/include/media/openmax \
		/android_source/customize/native/include/utils \
		/android_source/customize/native/include        

LOCAL_MODULE    := colorconvert-jni
LOCAL_SRC_FILES := colorconvert-jni.cpp
LOCAL_MODULE_CLASS := STATIC_LIBRARIES
LOCAL_LDLIBS  := -llog
LOCAL_LDLIBS  += -lstagefright
LOCAL_LDLIBS  += -landroid
LOCAL_LDLIBS  += -lutils


include $(BUILD_SHARED_LIBRARY)
