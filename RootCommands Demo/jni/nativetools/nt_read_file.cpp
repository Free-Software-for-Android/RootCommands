// (c) Chris F. Ravenscroft, VoilaWeb. For licensing information, check attached LICENSE file.

#include "nativetools.hpp"

int nt_read_file(int argc, char** argv, char** env) {
    int ret = EXIT_SUCCESS;

    if(argc != 2) {
        ret = nt_error("Wrong # of arguments for %s: %d", __FUNCTION__, argc);
    }
    else {
        char *s = argv[1];    
        struct stat sf;

        if(lstat(s, &sf) < 0) {
            ret = EXIT_FAILURE;
        }
        else {
            FILE *f = fopen(s, "r");
            if(!f) {
                ret = EXIT_FAILURE;
            }
            else {
                int c;
                // content
                while((c = fgetc(f)) != EOF) {
                    printf("%c", c);
                }
                fclose(f);
            }
        }
    }
    
    return ret;
}