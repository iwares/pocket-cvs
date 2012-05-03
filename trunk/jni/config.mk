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
# This is the config file for acvs JNI library building.
#
# The pocket cvs JNI library is based on CVS and Glibc open source project. The
# recommended pair is CVS-1.11.23 and Glib-2.14. You can use other different
# packages, but they are not valified and may not work.
#

# CVS source file path. This is the CVS source code path relative to the
# directory of this config file.
CVS_SRC_PATH := ./cvs-src

# Glibc source file path. This is the Glibc source code path relative to the
# directory of this config file.
GLIBC_SRC_PATH := ./glibc-src

