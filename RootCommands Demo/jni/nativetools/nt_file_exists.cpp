// (c) Chris F. Ravenscroft, VoilaWeb. For licensing information, check attached LICENSE file.

#include "nativetools.hpp"

int nt_file_exists(int argc, char** argv, char** env) {
    int ret = EXIT_SUCCESS;

    if(argc != 2) {
        ret = nt_error("Wrong # of arguments for %s: %d", __FUNCTION__, argc);
    }
    else {
        char *s = argv[1];
        char exists;

        if(0 == access(s, F_OK)) {
            exists = 'y';
        }
        else {
            exists = 'n';
        }
        // E,exists
        printf("E,%c", exists);
    }
    return ret;
}

