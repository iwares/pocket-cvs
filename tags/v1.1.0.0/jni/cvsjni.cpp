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
 * @file	jni/cvsjni.cpp
 * @author	Eric.Tsai
 *
 */

#include "cvsjni.h"
#include "cvssrv.h"

#include <android/log.h>

#define LOG_TAG		"cvsjni.cpp"

#define LOG_V(...)	((void)__android_log_print(ANDROID_LOG_VERBOSE,	LOG_TAG, __VA_ARGS__))
#define LOG_D(...)	((void)__android_log_print(ANDROID_LOG_DEBUG,	LOG_TAG, __VA_ARGS__))
#define LOG_I(...)	((void)__android_log_print(ANDROID_LOG_INFO,	LOG_TAG, __VA_ARGS__))
#define LOG_W(...)	((void)__android_log_print(ANDROID_LOG_WARN,	LOG_TAG, __VA_ARGS__))
#define LOG_E(...)	((void)__android_log_print(ANDROID_LOG_ERROR,	LOG_TAG, __VA_ARGS__))

#include <stdlib.h>

/** CVS executable path. */
static const char *CVS_EXEC_PATH = "/data/data/com.iwares.app.pocketcvs/lib/libcvsexec.so";

/**
 * Convert Java string which only contains ASCII characters to a C string. If
 * the Java string contains one or more non-ASCII characters, an empty C string
 * will returned.
 *
 * @param	cstr	Pointer of the buffer to hold the C string.
 * @param	max		Max length of the buffer.
 * @param	env		Java environment object pointer.
 * @param	jstr	The Java string.
 *
 * @return			The pointer of the C string buffer.
 *
 */
static char* __Java_internal_ASCIIJStringToCString(
	char *cstr, int max,
	JNIEnv *env, jstring jstr
	)
{
	int length = env->GetStringLength(jstr);

	if (length < max) {
		const jchar *temp = env->GetStringChars(jstr, JNI_FALSE);
		for (int i = 0; i < length; ++i) {
			cstr[i] = (char)temp[i];
			if (temp[i] > 127) {
				length = 0;
				break;
			}
		}
	} else {
		length = 0;
	}

	cstr[length] = 0;

	return cstr;
}

/**
 * Get the specified field ID of the specified Java object.
 *
 * @param	env		Java environment object pointer.
 * @param	obj		The Java object.
 * @param	name	The name of the field.
 * @param	sig		The type sign of the field.
 *
 * @return			The field ID.
 *
 */
static inline jfieldID __Java_internal_GetFieldID(
	JNIEnv *env, jobject obj,
	const char *name,
	const char *sig
	)
{
	jclass clazz = env->GetObjectClass(obj);
	return env->GetFieldID(clazz, name, sig);
}

/**
 * Create a new C++ CVSService object and bind it to the specified Java
 * CVSService object.
 *
 */
jboolean Java_com_iwares_app_pocketcvs_CVSService_nativeOnCreate(
	JNIEnv* env, jobject thiz,
	jstring rstr,
	jstring tstr
	)
{
	char repoPath[128], tempPath[128];
	if (__Java_internal_ASCIIJStringToCString(repoPath, 128, env, rstr)[0] == 0) {
		LOG_W("nativeOnCreate return JNI_FALSE: Cannot convert repo path Java String to C string.");
		return JNI_FALSE;
	}
	if (__Java_internal_ASCIIJStringToCString(tempPath, 128, env, tstr)[0] == 0) {
		LOG_W("nativeOnCreate return JNI_FALSE: Cannot convert temp path Java String to C string.");
		return JNI_FALSE;
	}
	CVSService *service = new CVSService(repoPath, tempPath, CVS_EXEC_PATH);
	LOG_I("Created native CVSService object 0x%08x", service);
	jfieldID field = __Java_internal_GetFieldID(env, thiz, "mNativePtr", "I");
	env->SetIntField(thiz, field, (int)service);
	return JNI_TRUE;
}

/**
 * Get the binded C++ CVSService object from the Java CVSService object and call
 * the setUserPassword() method.
 *
 */
jboolean Java_com_iwares_app_pocketcvs_CVSService_setUserPassword(
	JNIEnv* env, jobject thiz,
	jstring ustr,
	jstring pstr
	)
{
	// Convert Java strings to C strings.
	char username[32], password[32];
	if (__Java_internal_ASCIIJStringToCString(username, 32, env, ustr)[0] == 0) {
		LOG_W("setUserPassword return JNI_FALSE: Cannot convert user name Java String to C string.");
		return JNI_FALSE;
	}
	if (__Java_internal_ASCIIJStringToCString(password, 32, env, pstr)[0] == 0) {
		LOG_W("setUserPassword return JNI_FALSE: Cannot convert password Java String to C string.");
		return JNI_FALSE;
	}

	jfieldID field = __Java_internal_GetFieldID(env, thiz, "mNativePtr", "I");
	CVSService *service = (CVSService*)env->GetIntField(thiz, field);
	if (!service->setUserPassword(username, password)) {
		LOG_W("setUserPassword return JNI_FALSE: CVSService::setUserPassword return false.");
		return JNI_FALSE;
	}
	LOG_I("setUserPassword return JNI_TRUE");
	return JNI_TRUE;
}

/**
 * Get the binded C++ CVSService object from the Java CVSService object and call
 * the startDaemon() method.
 *
 */
jboolean Java_com_iwares_app_pocketcvs_CVSService_startDaemon(
	JNIEnv* env, jobject thiz
	)
{
	jfieldID field = __Java_internal_GetFieldID(env, thiz, "mNativePtr", "I");
	CVSService *service = (CVSService*)env->GetIntField(thiz, field);
	if (NULL == service) {
		LOG_W("startDaemon return JNI_FALSE: Native CVSService object is NULL.");
		return JNI_FALSE;
	}
	if (!service->startDaemon()) {
		LOG_W("startDaemon return JNI_FALSE: CVSService::startDaemon return false.");
		return JNI_FALSE;
	}
	LOG_I("startDaemon return JNI_TRUE");
	return JNI_TRUE;
}

/**
 * Get the binded C++ CVSService object from the Java CVSService object and call
 * the isDaemonRunning() method.
 *
 */
jboolean Java_com_iwares_app_pocketcvs_CVSService_isDaemonRunning(
	JNIEnv* env, jobject thiz
	)
{
	jfieldID field = __Java_internal_GetFieldID(env, thiz, "mNativePtr", "I");
	CVSService *service = (CVSService*)env->GetIntField(thiz, field);
	if (NULL == service) {
		LOG_W("isDaemonRunning return JNI_FALSE: Native CVSService object is NULL.");
		return JNI_FALSE;
	}
	bool result = service->isDaemonRunning();
	LOG_I("isDaemonRunning return %s", result ? "JNI_TRUE" : "JNI_FALSE");
	return result ? JNI_TRUE : JNI_FALSE;
}

/**
 * Get the binded C++ CVSService object from the Java CVSService object and call
 * the stopDaemon() method.
 *
 */
jboolean Java_com_iwares_app_pocketcvs_CVSService_stopDaemon(
	JNIEnv* env, jobject thiz
	)
{
	jfieldID field = __Java_internal_GetFieldID(env, thiz, "mNativePtr", "I");
	CVSService *service = (CVSService*)env->GetIntField(thiz, field);
	if (NULL == service) {
		LOG_W("stopDaemon return JNI_FALSE: Native CVSService object is NULL.");
		return JNI_FALSE;
	}
	if (!service->stopDaemon()) {
		LOG_W("stopDaemon return JNI_FALSE: CVSService::startDaemon return false.");
		return JNI_FALSE;
	}
	LOG_I("stopDaemon return JNI_TRUE");
	return JNI_TRUE;
}

/**
 * Get the binded C++ CVSService object from the Java CVSService object and
 * delete it.
 *
 */
void Java_com_iwares_app_pocketcvs_CVSService_nativeOnDestroy(
	JNIEnv* env, jobject thiz
	)
{
	jfieldID field = __Java_internal_GetFieldID(env, thiz, "mNativePtr", "I");
	CVSService *service = (CVSService*)env->GetIntField(thiz, field);
	LOG_I("Destroy native CVSService object 0x%08x", service);
	delete service;
}

