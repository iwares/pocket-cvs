/*
 * Copyright (C) 2011 iWARES Solution Provider
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

/**
 * @file	jni/cvsjni.h
 * @author	Eric.Tsai
 *
 */

#ifndef __CVSJNI_H__
#define __CVSJNI_H__

#include <jni.h>

#ifdef  __cplusplus
extern "C" {
#endif//__cplusplus

/**
 * Native function for nativeOnCreate() method of the
 * com.iwares.app.pocketcvs.CVSService class
 *
 * @param	env		The Java environment object pointer.
 * @param	thiz	The CVSService Java object.
 *
 */
JNIEXPORT void Java_com_iwares_app_pocketcvs_CVSService_nativeOnCreate(
	JNIEnv* env, jobject thiz
	);

/**
 * Native function for setUserPassword() method of the
 * com.iwares.app.pocketcvs.CVSService class
 *
 * @param	env		The Java environment object pointer.
 * @param	thiz	The CVSService Java object.
 * @param	ustr	User name Java string object.
 * @param	pstr	Password Java string object.
 *
 * @return			JNI_TRUE if success, otherwise JNI_FALSE.
 *
 */
JNIEXPORT jboolean Java_com_iwares_app_pocketcvs_CVSService_setUserPassword(
	JNIEnv* env, jobject thiz,
	jstring ustr,
	jstring pstr
	);

/**
 * Native function for startDaemon() method of the
 * com.iwares.app.pocketcvs.CVSService class
 *
 * @param	env		The Java environment object pointer.
 * @param	thiz	The CVSService Java object.
 *
 * @return			JNI_TRUE if success, otherwise JNI_FALSE.
 *
 */
JNIEXPORT jboolean Java_com_iwares_app_pocketcvs_CVSService_startDaemon(
	JNIEnv* env, jobject thiz
	);

/**
 * Native function for isDaemonRunning() method of the
 * com.iwares.app.pocketcvs.CVSService class
 *
 * @param	env		The Java environment object pointer.
 * @param	thiz	The CVSService Java object.
 *
 * @return			JNI_TRUE if the CVS daemon is running, otherwise JNI_FALSE.
 *
 */
JNIEXPORT jboolean Java_com_iwares_app_pocketcvs_CVSService_isDaemonRunning(
	JNIEnv* env, jobject thiz
	);

/**
 * Native function for stopDaemon() method of the
 * com.iwares.app.pocketcvs.CVSService class
 *
 * @param	env		The Java environment object pointer.
 * @param	thiz	The CVSService Java object.
 *
 * @return			JNI_TRUE if success, otherwise JNI_FALSE.
 *
 */
JNIEXPORT jboolean Java_com_iwares_app_pocketcvs_CVSService_stopDaemon(
	JNIEnv* env, jobject thiz
	);

/**
 * Native function for nativeOnDestroy() method of the
 * com.iwares.app.pocketcvs.CVSService class
 *
 * @param	env		The Java environment object pointer.
 * @param	thiz	The CVSService Java object.
 *
 */
JNIEXPORT void Java_com_iwares_app_pocketcvs_CVSService_nativeOnDestroy(
	JNIEnv* env, jobject thiz
	);

#ifdef  __cplusplus
}
#endif//__cplusplus

#endif//__CVSJNI_H__
