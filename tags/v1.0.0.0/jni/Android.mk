#
# Copyright (C) 2011 iWARES Solution Provider
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

#
# @file	jni/Android.mk
# @author	Eric.Tsai
#
# This is the makefile for pocket cvs JNI library building.
#

LOCAL_PATH := $(call my-dir)

include config.mk


# Build rules for libcrypt.a
include $(CLEAR_VARS)

LOCAL_MODULE := crypt

LOCAL_SRC_FILES := \
	$(GLIBC_SRC_PATH)/crypt/crypt.c \
	$(GLIBC_SRC_PATH)/crypt/crypt-entry.c \
	$(GLIBC_SRC_PATH)/crypt/crypt_util.c \

LOCAL_C_INCLUDES := ./

LOCAL_CFLAGS := -D__USE_GNU -D__BEGIN_DECLS= -D__END_DECLS= -D__THROW= \
	-D'__nonnull(p)=' -D'weak_alias(a,b)=' -O2

include $(BUILD_STATIC_LIBRARY)


# Build rules for cvs, It will be rename to libcvsexec.so to make it be packed
# into the APK file.
include $(CLEAR_VARS)

LOCAL_MODULE := cvs

LOCAL_SRC_FILES := \
	$(CVS_SRC_PATH)/lib/argmatch.c \
	$(CVS_SRC_PATH)/lib/getdate.c \
	$(CVS_SRC_PATH)/lib/getline.c \
	$(CVS_SRC_PATH)/lib/getopt.c \
	$(CVS_SRC_PATH)/lib/getopt1.c \
	$(CVS_SRC_PATH)/lib/getpass.c \
	$(CVS_SRC_PATH)/lib/md5.c \
	$(CVS_SRC_PATH)/lib/regex.c \
	$(CVS_SRC_PATH)/lib/savecwd.c \
	$(CVS_SRC_PATH)/lib/sighandle.c \
	$(CVS_SRC_PATH)/lib/stripslash.c \
	$(CVS_SRC_PATH)/lib/xgetwd.c \
	$(CVS_SRC_PATH)/lib/yesno.c \
	$(CVS_SRC_PATH)/zlib/adler32.c \
	$(CVS_SRC_PATH)/zlib/compress.c \
	$(CVS_SRC_PATH)/zlib/crc32.c \
	$(CVS_SRC_PATH)/zlib/gzio.c \
	$(CVS_SRC_PATH)/zlib/uncompr.c \
	$(CVS_SRC_PATH)/zlib/deflate.c \
	$(CVS_SRC_PATH)/zlib/trees.c \
	$(CVS_SRC_PATH)/zlib/zutil.c \
	$(CVS_SRC_PATH)/zlib/inflate.c \
	$(CVS_SRC_PATH)/zlib/infblock.c \
	$(CVS_SRC_PATH)/zlib/inftrees.c \
	$(CVS_SRC_PATH)/zlib/infcodes.c \
	$(CVS_SRC_PATH)/zlib/infutil.c \
	$(CVS_SRC_PATH)/zlib/inffast.c \
	$(CVS_SRC_PATH)/diff/diff.c \
	$(CVS_SRC_PATH)/diff/diff3.c \
	$(CVS_SRC_PATH)/diff/analyze.c \
	$(CVS_SRC_PATH)/diff/cmpbuf.c \
	$(CVS_SRC_PATH)/diff/io.c \
	$(CVS_SRC_PATH)/diff/context.c \
	$(CVS_SRC_PATH)/diff/ed.c \
	$(CVS_SRC_PATH)/diff/normal.c \
	$(CVS_SRC_PATH)/diff/ifdef.c \
	$(CVS_SRC_PATH)/diff/util.c \
	$(CVS_SRC_PATH)/diff/dir.c \
	$(CVS_SRC_PATH)/diff/version.c \
	$(CVS_SRC_PATH)/diff/side.c \
	$(CVS_SRC_PATH)/src/add.c \
	$(CVS_SRC_PATH)/src/admin.c \
	$(CVS_SRC_PATH)/src/annotate.c \
	$(CVS_SRC_PATH)/src/buffer.c \
	$(CVS_SRC_PATH)/src/checkin.c \
	$(CVS_SRC_PATH)/src/checkout.c \
	$(CVS_SRC_PATH)/src/classify.c \
	$(CVS_SRC_PATH)/src/client.c \
	$(CVS_SRC_PATH)/src/commit.c \
	$(CVS_SRC_PATH)/src/create_adm.c \
	$(CVS_SRC_PATH)/src/cvsrc.c \
	$(CVS_SRC_PATH)/src/diff.c \
	$(CVS_SRC_PATH)/src/edit.c \
	$(CVS_SRC_PATH)/src/entries.c \
	$(CVS_SRC_PATH)/src/error.c \
	$(CVS_SRC_PATH)/src/expand_path.c \
	$(CVS_SRC_PATH)/src/fileattr.c \
	$(CVS_SRC_PATH)/src/filesubr.c \
	$(CVS_SRC_PATH)/src/find_names.c \
	$(CVS_SRC_PATH)/src/hardlink.c \
	$(CVS_SRC_PATH)/src/hash.c \
	$(CVS_SRC_PATH)/src/history.c \
	$(CVS_SRC_PATH)/src/ignore.c \
	$(CVS_SRC_PATH)/src/import.c \
	$(CVS_SRC_PATH)/src/lock.c \
	$(CVS_SRC_PATH)/src/log.c \
	$(CVS_SRC_PATH)/src/login.c \
	$(CVS_SRC_PATH)/src/logmsg.c \
	$(CVS_SRC_PATH)/src/main.c \
	$(CVS_SRC_PATH)/src/mkmodules.c \
	$(CVS_SRC_PATH)/src/modules.c \
	$(CVS_SRC_PATH)/src/myndbm.c \
	$(CVS_SRC_PATH)/src/no_diff.c \
	$(CVS_SRC_PATH)/src/parseinfo.c \
	$(CVS_SRC_PATH)/src/patch.c \
	$(CVS_SRC_PATH)/src/rcs.c \
	$(CVS_SRC_PATH)/src/rcscmds.c \
	$(CVS_SRC_PATH)/src/recurse.c \
	$(CVS_SRC_PATH)/src/release.c \
	$(CVS_SRC_PATH)/src/remove.c \
	$(CVS_SRC_PATH)/src/repos.c \
	$(CVS_SRC_PATH)/src/root.c \
	$(CVS_SRC_PATH)/src/run.c \
	$(CVS_SRC_PATH)/src/scramble.c \
	$(CVS_SRC_PATH)/src/server.c \
	$(CVS_SRC_PATH)/src/stack.c \
	$(CVS_SRC_PATH)/src/status.c \
	$(CVS_SRC_PATH)/src/subr.c \
	$(CVS_SRC_PATH)/src/tag.c \
	$(CVS_SRC_PATH)/src/update.c \
	$(CVS_SRC_PATH)/src/version.c \
	$(CVS_SRC_PATH)/src/vers_ts.c \
	$(CVS_SRC_PATH)/src/watch.c \
	$(CVS_SRC_PATH)/src/wrapper.c \
	$(CVS_SRC_PATH)/src/zlib.c \
	fakers.c \

LOCAL_STATIC_LIBRARIES := crypt

LOCAL_C_INCLUDES := ./ $(CVS_SRC_PATH)/src/ $(CVS_SRC_PATH)/lib/ \
	$(CVS_SRC_PATH)/zlib/ $(CVS_SRC_PATH)/diff/

LOCAL_CFLAGS := -DHAVE_CONFIG_H -O2

include $(BUILD_EXECUTABLE)


# Build rules for libcvsdsrv.so
include $(CLEAR_VARS)

LOCAL_MODULE := cvsdsrv

LOCAL_SRC_FILES := \
	cvssrv.cpp \
	cvsjni.cpp \

LOCAL_STATIC_LIBRARIES := crypt

LOCAL_LDLIBS := -llog

LOCAL_C_INCLUDES := ./

LOCAL_CFLAGS := -O2

include $(BUILD_SHARED_LIBRARY)


