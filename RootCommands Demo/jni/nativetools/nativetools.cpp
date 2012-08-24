// (c) Chris F. Ravenscroft, VoilaWeb. For licensing information, check attached LICENSE file.

#define NATIVETOOLS_MAIN 1
#include "nativetools.hpp"
#include "nt_applets.hpp"

/*
 * Build using ndk-comp++ nativetools.cpp -o nativetools
 * or better: CyanogenMod's mka
 */

/* If C compiler (not C++) declare these:
typedef enum {
    false = 0,
    true
} bool;
*/

/*
 * Notes:
 * Whenever I think about it, I need to allocate fixed-size memory on the stack, which is tremendously faster
 * than having a bunch of malloc/free.
 */

int main(int argc, char** argv, char** env) {
    int exit_code = EXIT_FAILURE;

    // Nothing to be proud of but, hey,
    // argv is not immutable.
    argv[0] = nt_basename(argv[0]);
    if(argc > 1 && !strcmp(argv[0], NT_BIN_NAME)) {
        --argc;
        ++argv;
        argv[0] = nt_basename(argv[0]);
    }
    // Else:
    // We are using a link, rather than passing
    // our action name as an argument

    for(int i=0; i<APPLETS_COUNT; i++) {
        if(!strcmp(applets[i].keyword, argv[0])) {
            exit_code = (applets[i].fn)(argc, argv, env);
            break;
        }
    }

    return exit_code;
}
