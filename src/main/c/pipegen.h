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
#define PIPEGEN_DIRECTORY_ADDRESS "127.0.0.1:8888/"
#define PIPEGEN_DIRECTORY_EXPORT_SUB "export"
#define PIPEGEN_SYSTEM_PARAM "?system="

struct MemoryStruct {
	char *memory;
	size_t size;
};

struct DB_ADDRESS {
	char *ip;
	char *port;
};

FILE *pipegen_fopen(const char *path, const char *mode);

#endif /* #ifndef _PIPIEGEN_H_ */