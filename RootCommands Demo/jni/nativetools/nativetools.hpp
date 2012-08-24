// (c) Chris F. Ravenscroft, VoilaWeb. For licensing information, check attached LICENSE file.

#if !defined(NATIVETOOLS_GLOBAL_HPP)
	#define  NATIVETOOLS_GLOBAL_HPP 1
	#include <stdio.h>
	#include <stdlib.h>
	#include <string.h>
	#include <errno.h>
	#include <sys/statfs.h>
	#include <sys/stat.h>
	#include <sys/types.h>
	#include <sys/param.h>
	#include <unistd.h>
	#include <dirent.h>
	#include <pwd.h>
	#include <fcntl.h>

	#define NT_OS_WIN32	0x01
	#define NT_OS_UNIX  0x02

	#ifdef _WIN32
		#define	NT_OS NT_OS_WIN32
	#else
		#define NT_OS NT_OS_UNIX
	#endif

	#define NT_BIN_NAME "nativetools"

	int nt_error(const char*, ...);
	char nt_separator();
	char *nt_basename(const char*);
	int nt_listdir(const char*);
	int nt_cpfile(char*, char*, char*, struct stat*);
	int nt_cpdir(char*, char*);
	int nt_rmdir(char*);
	int nt_fileop(char*, int, int, int (*cb)(char*, int, struct stat*));

	/*
	 * Some functions use these globals and can therefore not be used
	 * in a thread pool. This needs fixing.
	 */
	#if defined(NATIVETOOLS_MAIN)
		unsigned int work_index = 0;
		unsigned int work_uid;
	#else
		extern unsigned int work_index;
		extern unsigned int work_uid;
	#endif

#endif /* NATIVETOOLS_GLOBAL_HPP */