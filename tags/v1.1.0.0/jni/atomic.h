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
 * @file	jni/atomic.h
 * @author	Eric.Tsai
 *
 * NDK does not have a "atomic.h" file. so we provide this file to make the
 * compiler happy.
 *
 */

#ifndef _ATOMIC_H
#define _ATOMIC_H

/*
 * crypt_util.c use this function in __init_des_r(). this line is copied from
 * the GNU glibc.
 *
 */
#define atomic_write_barrier() __asm ("" ::: "memory")

#endif//_ATOMIC_H

