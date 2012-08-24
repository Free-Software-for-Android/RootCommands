// (c) Chris F. Ravenscroft, VoilaWeb. For licensing information, check attached LICENSE file.

#include "nativetools.hpp"

int nt_recursive_crawl_(char *path, int parentindex, struct stat *sf) {
    int ret = EXIT_SUCCESS;
    char f_type = S_ISLNK(sf->st_mode) ? 'l' : S_ISDIR(sf->st_mode) ? 'd' : 'f';
    char f_exec = f_type != 'l' && sf->st_mode & S_IXUSR ? 'x' : '-';
    printf("%u,%u,%c,%c,%lld,%lld,%s\n", work_index, parentindex, f_type, f_exec, sf->st_size, sf->st_blocks, path);
	if(ret==EXIT_FAILURE) {nt_error("%s:#1", __FUNCTION__);}
    return ret;
}

int nt_recursive_crawl(int argc, char** argv, char** env) {
    int ret = EXIT_SUCCESS;

    if(argc != 2) {
        ret = nt_error("Wrong # of arguments for %s: %d", __FUNCTION__, argc);
    }
    else {
        char *s = argv[1];
	    struct stat sf;

	    if(lstat(s, &sf) < 0) {
	        ret = EXIT_FAILURE;
	        if(ret==EXIT_FAILURE) {nt_error("%s:#0", __FUNCTION__);}
	    }
	    else {
	        work_index = 0;
	        ret = nt_fileop(s, 99, 0, nt_recursive_crawl_);
	        if(ret != EXIT_FAILURE) {
	//            if(0 != nt_recursive_crawl_(true, s)) {
	//                ret = EXIT_FAILURE;
	//            }
	        }
	    }
	    
	    if(ret == EXIT_FAILURE) {
	        nt_error("%s:Overall", __FUNCTION__);
	    }
	}

    return ret;
}