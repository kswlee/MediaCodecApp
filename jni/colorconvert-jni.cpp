/*
 * Copyright (C) 2009 The Android Open Source Project
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
 *
 */
#include <string.h>
#include <jni.h>
#include <dlfcn.h>
#include <sys/types.h>
#include <android/log.h>
#include <stdio.h>

#include "colorconvert-jni.h"
#include <Errors.h>
#include <ColorConverter.h>

extern "C"  jstring JNICALL Java_com_test_colorconverter_ColorConvertUtil_convert
  (JNIEnv *env, jclass clazz, int srcWidth, int srcHeight, jbyteArray array,  jbyteArray outarray)
{
	android::ColorConverter converter(
            (OMX_COLOR_FORMATTYPE)0x7FA30C03,  OMX_COLOR_Format16bitRGB565);

	jbyte* bufferPtr = env->GetByteArrayElements(array, NULL);
	jbyte* outbufferPtr = env->GetByteArrayElements(outarray, NULL);
    converter.convert(
    		bufferPtr,
    		srcWidth, srcHeight,
            0, 0, srcWidth - 1, srcHeight - 1,
            outbufferPtr,
            srcWidth,
            srcHeight,
            0, 0, srcWidth - 1, srcHeight - 1);

	env->ReleaseByteArrayElements(array, bufferPtr, 0);
	env->ReleaseByteArrayElements(outarray, outbufferPtr, 0);

    return env->NewStringUTF("OK");
}

extern "C"  jstring JNICALL Java_com_test_colorconverter_ColorConvertUtil_convertColor
  (JNIEnv *env, jclass clazz, int srcFormat, int srcWidth, int srcHeight, jbyteArray array,  jbyteArray outarray)
{
	android::ColorConverter converter(
            (OMX_COLOR_FORMATTYPE)srcFormat,  OMX_COLOR_Format16bitRGB565);

	jbyte* bufferPtr = env->GetByteArrayElements(array, NULL);
	jbyte* outbufferPtr = env->GetByteArrayElements(outarray, NULL);
    converter.convert(
    		bufferPtr,
    		srcWidth, srcHeight,
            0, 0, srcWidth - 1, srcHeight - 1,
            outbufferPtr,
            srcWidth,
            srcHeight,
            0, 0, srcWidth - 1, srcHeight - 1);

	env->ReleaseByteArrayElements(array, bufferPtr, 0);
	env->ReleaseByteArrayElements(outarray, outbufferPtr, 0);

    return env->NewStringUTF("OK");
}

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM* vm, void* reserved) {
    return JNI_VERSION_1_6;
}

JNIEXPORT void JNICALL JNI_OnUnload(JavaVM* vm, void* reserved) {
}
