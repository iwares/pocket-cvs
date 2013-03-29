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
 * @file	jni/fakers.c
 * @author	Eric.Tsai
 *
 * Since our repository is stored in SD card, Android system does not allow to
 * change mode or change owner for SD card files. CVS can also works fine
 * without these functions. So, we use these strong symbols to cover the systems
 * weak symbols to fake the CVS program.
 *
 */
#include <sys/stat.h>

int chmod(const char *path, mode_t mode)
{
	return 0;
}

int fchmod(int fildes, mode_t mode)
{
	return 0;
}

int chown(const char *path, uid_t owner, gid_t group)
{
	return 0;
}

int fchown(int fildes, uid_t owner, gid_t group)
{
	return 0;
}

int lchown(const char *path, uid_t owner, gid_t group)
{
	return 0;
}

/*
 * Since the mode of SD card files is ---rwxr-x and we can not change mode for
 * SD card files, we need a default mode for checkout operation, here it is.
 *
 */
static const mode_t DEFAULT_MODE =
	S_IRUSR | S_IWUSR | S_IXUSR | S_IRGRP | S_IWGRP | S_IROTH | S_IWOTH
	;

int android_stat(const char *path, struct stat *buf)
{
	int retval = stat(path, buf);
	if (retval == 0)
		buf->st_mode |= DEFAULT_MODE;
	return retval;
}

int android_fstat(int fildes, struct stat *buf)
{
	int retval = fstat(fildes, buf);
	if (retval == 0)
		buf->st_mode |= DEFAULT_MODE;
	return retval;
}

int android_lstat(const char *path, struct stat *buf)
{
	int retval = lstat(path, buf);
	if (retval == 0)
		buf->st_mode |= DEFAULT_MODE;
	return retval;
}
