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
 * @file	jni/cvssrv.cpp
 * @author	Eric.Tsai
 *
 */

#include "cvssrv.h"

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <assert.h>
#include <unistd.h>
#include <sys/stat.h>
#include <errno.h>
#include <time.h>
#include <ctype.h>
#include <sys/wait.h>
#include <pwd.h>
#include <pthread.h>
#include <signal.h>
#include <sys/select.h>
#include <sys/socket.h>
#include <arpa/inet.h>

/** External crypt function.*/
extern "C" char* crypt (const char *__key, const char *__salt);

/**
 * Encrypt the specified key with a random salt.
 *
 * @param	key	The key which will be encrypted.
 *
 * @return		The encrypted string.
 *
 */
char* __crypt(const char *key)
{
	char salt[3] = { 0, 0, 0};

	srand(time(NULL));

	for (int i = 0; i < 2; ++i) {
		if (isalnum(salt[i] = rand() %128) || salt[i] == '.' || salt[i] == '/')
			continue;
		--i;
	}

	return crypt(key, salt);
}

/**
 * Initial the CVSService object and the CVS repository.
 */
CVSService::CVSService(const char *repoPath, const char *tempPath, const char *execPath)
{
	assert(NULL != repoPath);
	assert(NULL != tempPath);
	assert(NULL != execPath);

	// Fill up object fields.
	strncpy(m_repoPath, repoPath, MAX_PATH);
	strncpy(m_tempPath, tempPath, MAX_PATH);
	strncpy(m_execPath, execPath, MAX_PATH);

	// Set up flags.
	m_isRepoInitialized = false;
	m_isDaemonRunning = false;

	// Initial semaphores.
	sem_init(&m_exitThread, 0, 0);
	sem_init(&m_syncThread, 0, 0);

	struct stat statbuf;
	char cvsroot[MAX_PATH + 16];

	// Check repository status.
	if (stat(m_repoPath, &statbuf) < 0) {
		if (ENOENT != errno)
			return;
	} else if (!S_ISDIR(statbuf.st_mode)) {
		return;
	} else if (stat(strcat(strcpy(cvsroot, m_repoPath), "/CVSROOT"), &statbuf) < 0) {
		if (ENOENT != errno)
			return;
	} else if (!S_ISDIR(statbuf.st_mode)) {
		return;
	} else {
		m_isRepoInitialized = true;
	}

	// Initial CVS repository if it is not initialized.
	if (!m_isRepoInitialized) {
		pid_t pid = fork();

		// Invoke "cvs -d <repo> init" command.
		if (pid == 0) {
			execl(m_execPath, "cvs", "-d", m_repoPath, "init", NULL);
			exit(1);
		}

		// Wait for process exit and check the result.
		int status = 0;
		if (pid < 0 || waitpid(pid, &status, 0) < 0 || status != 0)
			return;

		// Set repository initialized flag.
		m_isRepoInitialized = true;

		// Set default user name and password.
		if (!setUserPassword("User", "Ab-123456"))
			return;
	}

}

/**
 * Stop the daemon thread if it is running.
 */
CVSService::~CVSService()
{
	stopDaemon();
}

/**
 * Fork a new process and duplicate the client socket to it stdin stdout and
 * stderr file descriptor.
 *
 */
pid_t CVSService::serveForClient(int client)
{
	assert(client >= 0);

	// Fork process.
	pid_t pid = fork();

	// Child side.
	if (0 == pid) {
		// The parent do not care about the SIGCHID signal, but the child may
		// need fork and wait child process for some CVS request. Set SIGCHILD
		// signal handler to default.
		signal(SIGCHLD, SIG_DFL);

		// Duplicate socket file descriptor.
		dup2(client, STDIN_FILENO);
		close(client);
		dup2(STDIN_FILENO, STDOUT_FILENO);
		dup2(STDIN_FILENO, STDERR_FILENO);

		// Execute CVS on pserver mode.
		char temp[256];
		execl(
			m_execPath,
			"cvs", "-f", "-T", m_tempPath,
			strcat(strcpy(temp, "--allow-root="), m_repoPath),
			"pserver",
			(char *)(0)
			);

		// We hope this line never be executed.
		exit(1);
	}

	// Return the PID on parent side.
	return pid;
}

bool CVSService::setUserPassword(const char *username, const char *password)
{
	// Repository is not initialized, just return false.
	if (!m_isRepoInitialized)
		return false;

	char filename[MAX_PATH + 24];
	FILE *fp;

	// Write user name password to 'passwd' file.
	if ((fp = fopen(strcat(strcpy(filename, m_repoPath), "/CVSROOT/passwd"), "w")) == NULL)
		return false;
	fprintf(fp, "%s:%s:%s\n", username, __crypt(password), getpwuid(getuid())->pw_name);
	fclose(fp);

	// Write user name to 'reader' file.
	if ((fp = fopen(strcat(strcpy(filename, m_repoPath), "/CVSROOT/reader"), "w")) == NULL)
		return false;
	fprintf(fp, "%s\n", username);
	fclose(fp);

	// Write user name to 'writer' file.
	if ((fp = fopen(strcat(strcpy(filename, m_repoPath), "/CVSROOT/writer"), "w")) == NULL)
		return false;
	fprintf(fp, "%s\n", username);
	fclose(fp);

	// Return true.
	return true;
}

/**
 * Create a new thread, use 'this' object as the thread context.
 */
bool CVSService::startDaemon(void)
{
	if (!m_isRepoInitialized)
		return false;
	if (m_isDaemonRunning)
		return true;
	pthread_t thread;
	if (pthread_create(&thread, NULL, daemonThreadFunc, this) < 0)
		return false;
	sem_wait(&m_syncThread);
	m_isDaemonRunning = true;
	return true;
}

/**
 * Just return the thread running status.
 */
bool CVSService::isDaemonRunning(void)
{
	return m_isDaemonRunning;
}

/**
 * Post semaphore to notify the daemon thread to exit.
 */
bool CVSService::stopDaemon(void)
{
	if (!m_isRepoInitialized)
		return false;
	if (!m_isDaemonRunning)
		return true;
	sem_post(&m_exitThread);
	sem_wait(&m_syncThread);
	m_isDaemonRunning = false;
	return true;
}

/**
 * Listen on port 2401 and call CVSService's serveForClient() method for all
 * connected client.
 *
 */
void* CVSService::daemonThreadFunc(void *param)
{
	CVSService *cvssrv = (CVSService *)param;

	// Notify the main thread that the daemon thread is started.
	sem_post(&cvssrv->m_syncThread);

	// Create socket and listen on port 2401.
	int server = socket(AF_INET, SOCK_STREAM, 0);
	int option = 1;
	setsockopt(server, SOL_SOCKET, SO_REUSEADDR, (char *)&option, sizeof(int));
	struct sockaddr_in saddr;
	memset(&saddr, 0, sizeof(saddr));
	saddr.sin_family = AF_INET;
	saddr.sin_addr.s_addr = INADDR_ANY;
	saddr.sin_port = htons(2401);
	bind(server, (struct sockaddr *)&saddr, sizeof(saddr));
	listen(server, 5);

	// We do not care about any of our child process.
	signal(SIGCHLD, SIG_IGN);

	// Loop until the exit semaphore is posted.
	while (sem_trywait(&cvssrv->m_exitThread) < 0 && errno == EAGAIN) {
		fd_set fdset;
		struct timeval tv = { 0, 1000000 };

		struct sockaddr_in iaddr;
		memset(&iaddr, 0, sizeof(iaddr));
		socklen_t socklen = sizeof(iaddr);

		FD_ZERO(&fdset);
		FD_SET(server, &fdset);

		// Select server socket.
		if (select(server + 1, &fdset, NULL, NULL, &tv) <= 0)
			continue;

		// Accept a client.
		int client = accept(server, (struct sockaddr *)&iaddr, &socklen);

		// Invalid client socket, continue loop.
		if (client < 0)
			continue;

		// Call serveForClient() to process request.
		cvssrv->serveForClient(client);
	}

	// Close server socket.
	close(server);

	// Notify the main thread that the daemon thread is stopped.
	sem_post(&cvssrv->m_syncThread);

	return NULL;
}
