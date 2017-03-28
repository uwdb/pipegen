#ifndef _PIPIEGEN_H_
#define _PIPIEGEN_H_

#include <string.h>
#include <sys/socket.h> 
#include <arpa/inet.h>
#include <stdio.h>
#include <stdlib.h>
#include <curl/curl.h>

#define PIPEGEN_SOCKET_NAME "pipegen.csv"
#define PIPEGEN_SOCKET_HOST "127.0.0.1"
#define PIPEGEN_SOCKET_PORT 2222

FILE *pipegen_fopen(const char *path, const char *mode);

#endif /* #ifndef _PIPIEGEN_H_ */