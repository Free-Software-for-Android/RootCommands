#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <signal.h>
#include <android/log.h>


// from http://www.enderunix.org/documents/eng/daemon.php
void daemonize()
{
int i,lfp;
char str[10];
    if(getppid()==1) return; /* already a daemon */
    i=fork();
    if (i<0) exit(1); /* fork error */
    if (i>0) exit(0); /* parent exits */
    /* child (daemon) continues */
    setsid(); /* obtain a new process group */
}
  
int main(void) {
    __android_log_print(ANDROID_LOG_INFO,"Demo", "Starting Hello World daemon...");
    
    daemonize();
    
    __android_log_print(ANDROID_LOG_INFO,"Demo", "Hello World daemon started!");
    
    while (1) {
      sleep(1);
    }
    
    return 0;
}