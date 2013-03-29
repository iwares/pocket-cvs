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
 * @file	jni/cvssrv.h
 * @author	Eric.Tsai
 *
 */

#ifndef __CVSSERVICE_H__
#define __CVSSERVICE_H__

#define MAX_PATH	256

#include <sys/types.h>
#include <semaphore.h>

/**
 * \class CVSService
 *
 * CVSService maintains a repository and starts a thread listen and process all
 * CVS request.
 *
 */
class CVSService {
public:
	/**
	 * Constructor.
	 *
	 * @param	repoPath	Path of the CVS repository.
	 * @param	tempPath	Path of the temporary directory.
	 * @param	execPath	Path of the CVS executable.
	 *
	 */
	CVSService(const char *repoPath, const char *tempPath, const char *execPath);
	/**
	 * Destructor.
	 *
	 */
	~CVSService();
private:
	/** Hidden copy constructor. */
	CVSService(const CVSService &other);
	/** Hidden assign operator. */
	CVSService& operator =(const CVSService &other);
public:
	/**
	 * Set user name and password for the CVSService.
	 *
	 * @param	username	New user name.
	 * @param	password	New password.
	 *
	 * @return				Return true if the user name and password changed
	 * 						successfully, otherwise return false.
	 *
	 */
	bool setUserPassword(const char *username, const char *password);
	/**
	 * Start the CVS daemon thread.
	 *
	 * @return		Return true if the CVS daemon thread started successfully or
	 * 				the daemon thread has already been started, otherwise return
	 * 				false.
	 *
	 */
	bool startDaemon(void);
	/**
	 * Get the running status of the CVS thread.
	 *
	 * @return		Return true if the CVS daemon thread is running, otherwise
	 * 				return false.
	 *
	 */
	bool isDaemonRunning(void);
	/**
	 * Stop the CVS daemon thread.
	 *
	 * @return		Return true if the CVS daemon thread stopped successfully or
	 * 				the daemon thread has already been stopped, otherwise return
	 * 				false.
	 *
	 */
	bool stopDaemon(void);
private:
	/**
	 * Start a new process the serve for the specified CVS client.
	 *
	 * @param	client	The socket of the CVS client.
	 *
	 * @return			Return the PID of the created process if success,
	 * 					otherwise return -1;
	 */
	pid_t serveForClient(int client);
private:
	/** CVS Daemon thread function. */
	static void* daemonThreadFunc(void *param);
private:
	/** CVS repository path. */
	char m_repoPath[MAX_PATH];
	/** CVS temporary path. */
	char m_tempPath[MAX_PATH];
	/** CVS executable path. */
	char m_execPath[MAX_PATH];
	/** Flag to indicate whether the CVS repository is initialized. */
	bool m_isRepoInitialized;
	/** Flag to indicate whether the CVS daemon is running. */
	bool m_isDaemonRunning;
	/** Semaphore used to handler the exit event of CVS daemon thread. */
	sem_t m_exitThread;
	/** Semaphore used to sync the thread status. */
	sem_t m_syncThread;
};

#endif//__CVSSERVICE_H__
