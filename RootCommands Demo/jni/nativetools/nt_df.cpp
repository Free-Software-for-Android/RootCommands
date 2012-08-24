// (c) Chris F. Ravenscroft, VoilaWeb. For licensing information, check attached LICENSE file.

#include "nativetools.hpp"

int nt_df(int argc, char** argv, char** env) {
    int ret = EXIT_SUCCESS;

    if(argc != 2) {
        ret = nt_error("Wrong # of arguments for %s: %d", __FUNCTION__, argc);
    }
    else {
        char *s = argv[1];
        struct statfs st;

        if (statfs(s, &st) < 0) {
            ret = EXIT_FAILURE;
        }
        else {
            // D,total,used,available,block_size
            printf("D,%lld,%lld,%lld,%d",
                ((long long)st.f_blocks * (long long)st.f_bsize) / 1024,
                ((long long)(st.f_blocks - (long long)st.f_bfree) * st.f_bsize) / 1024,
                ((long long)st.f_bfree * (long long)st.f_bsize) / 1024,
                (int) st.f_bsize);
        }
    }
    return ret;
}
