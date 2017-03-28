#include "pipegen.h"

FILE *pipegen_fopen(const char *path, const char *mode) {
	if (strncmp(PIPEGEN_SOCKET_NAME, path, strlen(PIPEGEN_SOCKET_NAME)) == 0) {
		// open socket
		int socket_desc;
	    struct sockaddr_in server;
	     
	    //Create socket
	    socket_desc = socket(AF_INET , SOCK_STREAM , 0);
	    if (socket_desc == -1) {
	        printf("Could not create socket.");
	        return NULL;
	    }
	         
	    server.sin_addr.s_addr = inet_addr(PIPEGEN_SOCKET_HOST);
	    server.sin_family = AF_INET;
	    server.sin_port = htons(PIPEGEN_SOCKET_PORT);
	 
	    //Connect to remote server
	    if (connect(socket_desc , (struct sockaddr *)&server , sizeof(server)) < 0)
	    {
	        puts("Connect error");
	        return NULL;
	    }
		return fdopen(socket_desc, mode);
	} else {
		return fopen(path, mode);
	}
}