// (c) Chris F. Ravenscroft, VoilaWeb. For licensing information, check attached LICENSE file.

#include "nativetools.hpp"

int nt_list_links(int argc, char** argv, char** env) {
    int ret = EXIT_SUCCESS;

    if(argc != 2) {
        ret = nt_error("Wrong # of arguments for %s: %d", __FUNCTION__, argc);
    }
    else {
        char *s = argv[1];
        char tmp[4096];
        struct stat sf;

        DIR *d;
        if(0 == (d  = opendir(s))) {
            ret = EXIT_FAILURE;
        }
        else {
            struct dirent *entry;
            while(0 != (entry = readdir(d))) {
                snprintf(tmp, sizeof(tmp), "%s%c%s", s, nt_separator(), entry->d_name);
                if(lstat(tmp, &sf) < 0) {
                    ret = EXIT_FAILURE;
                    // TODO: Break or not?
                }
                else {
                    if (S_ISLNK(sf.st_mode)) {
                        char dest[4096];
                        if(readlink(tmp, dest, 4096) < 0) {
                            ret = EXIT_FAILURE;
                        }
                        else {
                            printf("l,%s,%s\n", entry->d_name, dest);
                        }
                    }
                }
            }
        }
    }

    return ret;
}
