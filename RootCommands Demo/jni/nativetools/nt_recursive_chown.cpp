// (c) Chris F. Ravenscroft, VoilaWeb. For licensing information, check attached LICENSE file.

#include "nativetools.hpp"

int nt_recursive_chown_(char *path, int parentindex, struct stat *sf) {
    int ret = EXIT_SUCCESS;
    if(parentindex){}; // This function does not care about parentindex
    if(sf){}; // This function does not use isdir
    if(0 != lchown(path, work_uid, work_uid)) {
        ret = EXIT_FAILURE;
    }
    return ret;
}

int nt_recursive_chown(int argc, char** argv, char** env) {    
    int ret = EXIT_SUCCESS;

    if(argc != 4) {
        ret = nt_error("Wrong # of arguments for %s: %d", __FUNCTION__, argc);
    }
    else {
        char *s     = argv[1];
        int depth   = atoi(argv[2]);
        char *uname = argv[3];
        struct stat sf;

        if(strlen(s) > strlen("/data/noensp/")) {
            if(lstat(s, &sf) < 0) {
                ret = EXIT_FAILURE;
            }
            else {
                work_uid = atoi(uname);
                if(0 == work_uid) {
                    struct passwd *pwd = getpwnam(uname);
                    if(pwd) {
                        work_uid = pwd->pw_uid;
                    }
                }
                if(0 != work_uid) {
                    ret = nt_fileop(s, depth, 0, nt_recursive_chown_);
                    if(ret != EXIT_FAILURE) {
                        if(0 != nt_recursive_chown_(s, 0, 0)) {
                            ret = EXIT_FAILURE;
                        }
                    }
                }
                else {
                    ret = EXIT_FAILURE;
                }
            }
        }
        else {
            ret = EXIT_FAILURE;
        }
        
        if(ret == EXIT_FAILURE) {
            nt_error("Failure in function %s", __FUNCTION__);
        }
    }
    
    return ret;
}