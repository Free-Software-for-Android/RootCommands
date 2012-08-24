// (c) Chris F. Ravenscroft, VoilaWeb. For licensing information, check attached LICENSE file.

#include "nativetools.hpp"

int nt_recursive_remove(int argc, char** argv, char** env) {
    int ret = EXIT_SUCCESS;

    if(argc != 2) {
        ret = nt_error("Wrong # of arguments for %s: %d", __FUNCTION__, argc);
    }
    else {
        char *s    = argv[1];
        struct stat sf;

        if(strlen(s) > strlen("/data/noensp/")) {
            if(lstat(s, &sf) < 0) {
                ret = EXIT_FAILURE;
            }
            else {
                ret = nt_rmdir(s);
                if(ret != EXIT_FAILURE) {
                    if(0 != rmdir(s)) {
                        ret = EXIT_FAILURE;
                    }
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
