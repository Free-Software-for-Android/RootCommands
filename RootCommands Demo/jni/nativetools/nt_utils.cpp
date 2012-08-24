// (c) Chris F. Ravenscroft, VoilaWeb. For licensing information, check attached LICENSE file.

#include "nativetools.hpp"
#include <stdarg.h>

int nt_error(const char* format, ...) {
	fprintf(stderr, "~ERR-");
	va_list argptr;
	va_start(argptr, format);
	vfprintf(stderr, format, argptr);
	va_end(argptr);
	return EXIT_FAILURE;
}

char nt_separator() {
	if(NT_OS == NT_OS_WIN32) {
		return '\\';		
	}	
	else {
		return '/';
	}
}

char *nt_basename(const char* name) {
	char *basename = strrchr(name, nt_separator());
	if(basename) {
		++basename;
	}
	else {
		basename = (char*)name;
	}
	return basename;
}

int nt_listdir(const char* s) {
    int ret = EXIT_SUCCESS;

    char tmp[4096];
    struct stat sf;

    DIR *d;
    if(0 == (d  = opendir(s))) {
        ret = EXIT_FAILURE;
    }
    else {
        struct dirent *entry;
        while(0 != (entry = readdir(d))) {
            if (!strcmp(entry->d_name, ".") || !strcmp(entry->d_name, "..")) continue;
            snprintf(tmp, sizeof(tmp), "%s/%s", s, entry->d_name);
            if(lstat(tmp, &sf) < 0) {
                ret = EXIT_FAILURE;
                // TODO: Break or not?
            }
            else {
                char f_type = S_ISLNK(sf.st_mode) ? 'l' : S_ISDIR(sf.st_mode) ? 'd' : 'f';
                char f_exec = f_type != 'l' && sf.st_mode & S_IXUSR ? 'x' : '-';
                if(f_type == 'l') {
                    char dest[4096];
                    if(readlink(tmp, dest, 4096) < 0) {
                        ret = EXIT_FAILURE;
                    }
                    else {
                        if(strstr(dest, "/asec/") ||
                           strstr(dest, "/openfeint/")) {
                            f_type = 'L';
                        }
                    }
                }
                printf("%c,%c,%lld,%lld,%s\n", f_type, f_exec, sf.st_size, sf.st_blocks, entry->d_name);
            }
        }
        rewinddir(d);
        while(0 != (entry = readdir(d))) {
            if (!strcmp(entry->d_name, ".") || !strcmp(entry->d_name, "..")) continue;
            snprintf(tmp, sizeof(tmp), "%s/%s", s, entry->d_name);
                if(lstat(tmp, &sf) < 0) {
                    ret = EXIT_FAILURE;
                    // TODO: Break or not?
                }
                else {
                    if (S_ISDIR(sf.st_mode)) {
                        printf("%s:\n", tmp);
                        nt_listdir(tmp);
                    }
                }
        }
        closedir(d);
    }

    return ret;
}

int nt_cpfile(char* s, char* dest, char* filename, struct stat* sf) {
    int ret = EXIT_SUCCESS;

    char *srcpath = (char*)malloc(sizeof(char) * (strlen(s) + strlen(filename) + 2));
    snprintf(srcpath, sizeof(char)*(strlen(s) + strlen(filename) + 2), "%s%c%s", s, nt_separator(), filename);
    char *destpath = (char*)malloc(sizeof(char) * (strlen(dest) + strlen(filename) + 2));
    snprintf(destpath, sizeof(char)*(strlen(dest) + strlen(filename) + 2), "%s%c%s", dest, nt_separator(), filename);

    int src_fd = open(srcpath, O_RDONLY);
    if(-1 < src_fd) {
        int dest_fd = open(destpath, O_WRONLY | O_CREAT | O_TRUNC, sf->st_mode);
        if(-1 < dest_fd) {
            char buf[4096], *bufptr; // aligned on 4 bytes -- 8 if needed.
            int bufsize = sizeof(buf);
            int readcount = 0;
            int remcount = 0;
            int writecount = 0;
            do {
                do {
                    readcount = read(src_fd, buf, bufsize);
                } while(0 > readcount && errno == EINTR);
                if(0 < readcount) {
                    bufptr = buf;
                    remcount = readcount;
                    do {
                        do {
                            writecount = write(dest_fd, bufptr, remcount);
                        } while(0 > writecount && errno == EINTR);
                        if(0 < writecount) {
                            bufptr += writecount;
                            remcount -= writecount;
                        }
                        else {
                            ret = EXIT_FAILURE;
                        }
                    } while(0 < remcount && ret != EXIT_FAILURE);
                }
                else if(0 > readcount) {
                    ret = EXIT_FAILURE;
                }
            } while(0 < readcount && ret != EXIT_FAILURE);

            close(dest_fd);
        }
        else {
            ret = EXIT_FAILURE;
        }

        close(src_fd);
    }
    else {
        ret = EXIT_FAILURE;
    }

    if(ret != EXIT_FAILURE) {
        chown(destpath, sf->st_uid, sf->st_gid);
    }

    free(destpath);
    free(srcpath);

    return ret;
}

int nt_cpdir(char* s, char* dest) {
    int ret = EXIT_SUCCESS;

    struct stat sf;

    DIR *d;
    if(0 == (d  = opendir(s))) {
        ret = EXIT_FAILURE;
    }
    else {
        struct dirent *entry;
        while(ret != EXIT_FAILURE && 0 != (entry = readdir(d))) {
            if (!strcmp(entry->d_name, ".") || !strcmp(entry->d_name, "..")) continue;
            char *srcpath = (char*)malloc(sizeof(char) * (strlen(entry->d_name) + strlen(s) + 2));
            snprintf(srcpath, sizeof(char)*(strlen(entry->d_name) + strlen(s) + 2), "%s%c%s", s, nt_separator(), entry->d_name);
            if(lstat(srcpath, &sf) < 0) {
                ret = EXIT_FAILURE;
                // TODO: Break or not?
            }
            else {
                if(!S_ISDIR(sf.st_mode)) {
                    ret = nt_cpfile(s, dest, entry->d_name, &sf);
                }
            }
            free(srcpath);
        }
        if(ret != EXIT_FAILURE) {
            rewinddir(d);
            while(ret != EXIT_FAILURE && 0 != (entry = readdir(d))) {
                if (!strcmp(entry->d_name, ".") || !strcmp(entry->d_name, "..")) continue;
                char *srcpath = (char*)malloc(sizeof(char) * (strlen(entry->d_name) + strlen(s) + 2));
                snprintf(srcpath, sizeof(char)*(strlen(entry->d_name) + strlen(s) + 2), "%s%c%s", s, nt_separator(), entry->d_name);
                // Be sure to always use lstat ot we may end up in hairy situations
                if(lstat(srcpath, &sf) < 0) {
                    ret = EXIT_FAILURE;
                }
                else {
                    if (S_ISDIR(sf.st_mode)) {
                        char *destpath = (char*)malloc(sizeof(char) * (strlen(entry->d_name) + strlen(dest) + 2));
                        snprintf(destpath, sizeof(char)*(strlen(entry->d_name) + strlen(dest) + 2), "%s%c%s", dest, nt_separator(), entry->d_name);
                        if(0 == mkdir(destpath, sf.st_mode) || errno == EEXIST) {
                            chown(destpath, sf.st_uid, sf.st_gid);
                            ret = nt_cpdir(srcpath, destpath);
                        }
                        else {
                            ret = EXIT_FAILURE;
                        }
                        free(destpath);
                    }
                }
                free(srcpath);
            }
        }
        closedir(d);
    }

    return ret;
}

int nt_rmdir(char *s) {
    int ret = EXIT_SUCCESS;

    struct stat sf;

    DIR *d;
    if(0 == (d  = opendir(s))) {
        ret = EXIT_FAILURE;
    }
    else {
        struct dirent *entry;
        while(ret != EXIT_FAILURE && 0 != (entry = readdir(d))) {
            if (!strcmp(entry->d_name, ".") || !strcmp(entry->d_name, "..")) continue;
            char *subpath = (char*)malloc(sizeof(char) * (strlen(entry->d_name) + strlen(s) + 2));
            snprintf(subpath, sizeof(char)*(strlen(entry->d_name) + strlen(s) + 2), "%s%c%s", s, nt_separator(), entry->d_name);
            if(lstat(subpath, &sf) < 0) {
                ret = EXIT_FAILURE;
            }
            else {
                if (S_ISDIR(sf.st_mode)) {
                    ret = nt_rmdir(subpath);
                    if(ret != EXIT_FAILURE) {
                        if(0 != rmdir(subpath)) {
                            ret = EXIT_FAILURE;
                        }
                    }
                }
            }
            free(subpath);
        }
        if(ret != EXIT_FAILURE) {
            rewinddir(d);
            while(ret != EXIT_FAILURE && 0 != (entry = readdir(d))) {
                if (!strcmp(entry->d_name, ".") || !strcmp(entry->d_name, "..")) continue;
                char *subpath = (char*)malloc(sizeof(char) * (strlen(entry->d_name) + strlen(s) + 2));
                snprintf(subpath, sizeof(char)*(strlen(entry->d_name) + strlen(s) + 2), "%s%c%s", s, nt_separator(), entry->d_name);
                if(lstat(subpath, &sf) < 0) {
                    ret = EXIT_FAILURE;
                }
                else {
                    if(!S_ISDIR(sf.st_mode)) {
                        if(0 != unlink(subpath)) {
                            ret = EXIT_FAILURE;
                        }
                    }
                }
                free(subpath);
            }
        }
        closedir(d);
    }

    return ret;
}

int nt_fileop(char* s, int curdepth, int parentindex, int (*cb)(char*, int, struct stat*)) {
/* If C Compiler:
int fileop_(char *s, int curdepth, int parentindex, int (*cb)()) {
*/
    int ret = EXIT_SUCCESS;

    struct stat sf;

    DIR *d;
    if(0 == (d  = opendir(s))) {
        ret = EXIT_FAILURE;
    }
    else {
        -- curdepth;

        struct dirent *entry;
        while(ret != EXIT_FAILURE && 0 != (entry = readdir(d))) {
            if (!strcmp(entry->d_name, ".") || !strcmp(entry->d_name, "..")) continue;
            char *subpath = (char*)malloc(sizeof(char) * (strlen(entry->d_name) + strlen(s) + 2));
            snprintf(subpath, sizeof(char)*(strlen(entry->d_name) + strlen(s) + 2), "%s%c%s", s, nt_separator(), entry->d_name);
            if(lstat(subpath, &sf) < 0) {
                ret = EXIT_FAILURE;
            }
            else {
                if (S_ISDIR(sf.st_mode)) {
                    ++ work_index;
                    int my_index = work_index;
                    if(curdepth > 0) {
                        ret = nt_fileop(subpath, curdepth, my_index, cb);
                    }
                    if(ret != EXIT_FAILURE) {
                        int saved_index = work_index;
                        work_index = my_index;
                        if(EXIT_SUCCESS != cb(subpath, parentindex, &sf)) {
                            ret = EXIT_FAILURE;
                        }
                        work_index = saved_index;
                    }
                }
            }
            free(subpath);
        }
        if(ret != EXIT_FAILURE) {
            rewinddir(d);
            while(ret != EXIT_FAILURE && 0 != (entry = readdir(d))) {
                if (!strcmp(entry->d_name, ".") || !strcmp(entry->d_name, "..")) continue;
                char *subpath = (char*)malloc(sizeof(char) * (strlen(entry->d_name) + strlen(s) + 2));
                snprintf(subpath, sizeof(char)*(strlen(entry->d_name) + strlen(s) + 2), "%s%c%s", s, nt_separator(), entry->d_name);
                if(lstat(subpath, &sf) < 0) {
                    ret = EXIT_FAILURE;
                }
                else {
                    if(!S_ISDIR(sf.st_mode)) {
                        ++ work_index;
                        if(0 != cb(subpath, parentindex, &sf)) {
                            ret = EXIT_FAILURE;
                        }
                    }
                }
                free(subpath);
            }
        }
        closedir(d);
    }

    return ret;
}