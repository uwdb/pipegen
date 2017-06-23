#ifndef _PIPIEGEN_H_
#define _PIPIEGEN_H_

#include <string.h>
#include <sys/socket.h> 
#include <arpa/inet.h>
#include <stdio.h>
#include <stdlib.h>
#include <curl/curl.h>
#include <unistd.h>

#define PIPEGEN_SOCKET_HOST "127.0.0.1"
#define PIPEGEN_SOCKET_LISTEN_RETRY_TIME 5
#define PIPEGEN_SOCKET_PORT_BASE 8000
#define PIPEGEN_DIRECTORY_ADDRESS "127.0.0.1:8888/"
#define PIPEGEN_DIRECTORY_EXPORT_SUB "export?"
#define PIPEGEN_DIRECTORY_IMPORT_SUB "import?"
#define PIPEGEN_SYSTEM_PARAM "system="
#define PIPEGEN_HOST_PARAM "hostname="
#define PIPEGEN_PORT_PARAM "port="

struct MemoryStruct {
	char *memory;
	size_t size;
};

struct DB_ADDRESS {
	char *ip;
	char *port;
};

FILE *pipegen_fopen_st(char *path, char *mode);

unsigned long pipegen_fopen(unsigned long path, unsigned long mode);

#endif /* #ifndef _PIPIEGEN_H_ */