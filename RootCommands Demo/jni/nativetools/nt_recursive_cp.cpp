// (c) Chris F. Ravenscroft, VoilaWeb. For licensing information, check attached LICENSE file.

#include "nativetools.hpp"

int nt_recursive_cp(int argc, char** argv, char** env) {
    int ret = EXIT_SUCCESS;

    if(argc != 3) {
        ret = nt_error("Wrong # of arguments for %s: %d", __FUNCTION__, argc);
    }
    else {
        char *s    = argv[1];
        char *dest = argv[2];
        struct stat sf;

        if(lstat(s, &sf) < 0) {
            ret = EXIT_FAILURE;
        }
        else {
            char *dirname = strrchr(s, nt_separator());
            if(dirname) {
                ++dirname;
            }
            else {
                dirname = s;
            }
            char *destpath = (char*)malloc(sizeof(char) * (strlen(dirname) + strlen(dest) + 2));
            snprintf(destpath, sizeof(char)*(strlen(dirname) + strlen(dest) + 2), "%s%c%s", dest, nt_separator(), dirname);
            if(0 == mkdir(destpath, sf.st_mode) || errno == EEXIST) {
                chown(destpath, sf.st_uid, sf.st_gid);
                ret = nt_cpdir(s, destpath);
            }
            else {
                ret = EXIT_FAILURE;
            }
            free(destpath);
        }

        if(ret == EXIT_FAILURE) {
            nt_error("Failure in function %s", __FUNCTION__);
        }
    }
    return ret;
}