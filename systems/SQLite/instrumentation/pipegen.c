#include "pipegen.h"

int import_socket = -1;
int listen_port = PIPEGEN_SOCKET_PORT_BASE;
int ccurl_global_init = 0;

static size_t
WriteMemoryCallback(void *contents, size_t size, size_t nmemb, void *userp)
{
    size_t realsize = size * nmemb;
    struct MemoryStruct *mem = (struct MemoryStruct *)userp;

    mem->memory = realloc(mem->memory, mem->size + realsize + 1);
    if(mem->memory == NULL) {
        printf("not enough memory (realloc returned NULL)\n");
        return 0;
    }

    memcpy(&(mem->memory[mem->size]), contents, realsize);
    mem->size += realsize;
    mem->memory[mem->size] = 0;

    return realsize;
}

struct DB_ADDRESS *extract_db_address(char *raw_data) {
    int success = 0;
    struct DB_ADDRESS *db = (struct DB_ADDRESS *)malloc(sizeof(struct DB_ADDRESS));
    char* token = strtok(raw_data, ",");
    int idx = 0;
    while (token)
    {
        if (idx == 3) {
            db->ip = strdup(token);
        } else if (idx == 4) {
            db->port = strdup(token);
            success = 1;
        }
        idx ++;
        token = strtok(0, ",");
    }
    if (success) {
        return db;
    } else {
        return NULL;
    }
}

struct DB_ADDRESS *get_importer(char *name) {
    CURL *curl;
    CURLcode res;
    struct MemoryStruct chunk;
    char *request_url;

    int url_length = strlen(name) + strlen(PIPEGEN_DIRECTORY_ADDRESS) + strlen(PIPEGEN_DIRECTORY_EXPORT_SUB) + strlen(PIPEGEN_SYSTEM_PARAM) + 2;;
    struct DB_ADDRESS *db = NULL;
    chunk.memory = malloc(1); 
    chunk.size = 0;
    request_url = (char *)calloc(url_length, 1);
    strncpy(request_url, PIPEGEN_DIRECTORY_ADDRESS, strlen(PIPEGEN_DIRECTORY_ADDRESS));
    strncat(request_url, PIPEGEN_DIRECTORY_EXPORT_SUB, strlen(PIPEGEN_DIRECTORY_EXPORT_SUB));
    strncat(request_url, PIPEGEN_SYSTEM_PARAM, strlen(PIPEGEN_SYSTEM_PARAM));
    strncat(request_url, name, strlen(name));

    if (!ccurl_global_init) {
        curl_global_init(CURL_GLOBAL_ALL);
        ccurl_global_init = 1;
    }
    curl = curl_easy_init();
    if(curl) {
        curl_easy_setopt(curl, CURLOPT_URL, request_url);
        curl_easy_setopt(curl, CURLOPT_WRITEFUNCTION, WriteMemoryCallback);
        curl_easy_setopt(curl, CURLOPT_WRITEDATA, (void *)&chunk);
        curl_easy_setopt(curl, CURLOPT_USERAGENT, "libcurl-agent/1.0");
        res = curl_easy_perform(curl);
        if(res != CURLE_OK) {
            fprintf(stderr, "Cannot connect to directory: %s\n",
                  curl_easy_strerror(res));
            return NULL;
        } 
        db = extract_db_address(chunk.memory);
    }
    free(chunk.memory);
    free(request_url);
    curl_easy_cleanup(curl);
    return db;
}

// return 0 if success
int register_importer(const char *name, char *ip, char *port) {
    CURL *curl;
    CURLcode res;
    char *request_url;

    int url_length = strlen(name) + strlen(ip) + strlen(port) + 
        strlen(PIPEGEN_DIRECTORY_ADDRESS) + 
        strlen(PIPEGEN_DIRECTORY_IMPORT_SUB) + 
        strlen(PIPEGEN_SYSTEM_PARAM) +
        strlen(PIPEGEN_HOST_PARAM) + 
        strlen(PIPEGEN_PORT_PARAM) + 3 + 2;
    struct DB_ADDRESS *db = NULL;

    request_url = (char *)calloc(url_length, 1);
    strncpy(request_url, PIPEGEN_DIRECTORY_ADDRESS, strlen(PIPEGEN_DIRECTORY_ADDRESS));
    strncat(request_url, PIPEGEN_DIRECTORY_IMPORT_SUB, strlen(PIPEGEN_DIRECTORY_IMPORT_SUB));
    // system
    strncat(request_url, PIPEGEN_SYSTEM_PARAM, strlen(PIPEGEN_SYSTEM_PARAM));
    strncat(request_url, name, strlen(name));
    strncat(request_url, "&", strlen("&"));
    // host ip
    strncat(request_url, PIPEGEN_HOST_PARAM, strlen(PIPEGEN_HOST_PARAM));
    strncat(request_url, ip, strlen(ip));
    strncat(request_url, "&", strlen("&"));
    // port
    strncat(request_url, PIPEGEN_PORT_PARAM, strlen(PIPEGEN_PORT_PARAM));
    strncat(request_url, port, strlen(port));

    if (!ccurl_global_init) {
        curl_global_init(CURL_GLOBAL_ALL);
        ccurl_global_init = 1;
    }
    curl = curl_easy_init();
    int ret = 0;
    if(curl) {
        curl_easy_setopt(curl, CURLOPT_URL, request_url);
        curl_easy_setopt(curl, CURLOPT_NOBODY, 1);
        res = curl_easy_perform(curl);
        if(res != CURLE_OK) {
            fprintf(stderr, "Cannot connect to directory: %s\n",
                  curl_easy_strerror(res));
            ret = -1;
        }
    } else {
        ret = -1;
    }
    free(request_url);
    curl_easy_cleanup(curl);
    return ret;
}

FILE *pipegen_fopen_st(char *path, char *mode) {
    if (mode[0] == 'r') {
        // read mode
        // import

        char listen_port_str[6];
        sprintf(listen_port_str, "%d", listen_port);

        if (import_socket < 0) {
            struct sockaddr_in serv_addr;
            import_socket = socket(AF_INET, SOCK_STREAM, 0);            
            if (import_socket < 0) {
                printf("Could not create socket.");
                return NULL;
            }
            bzero((char *) &serv_addr, sizeof(serv_addr));

            serv_addr.sin_family = AF_INET;
            serv_addr.sin_addr.s_addr = INADDR_ANY;

            int try = 0;
            int bind_success = 0;
            // try to listen to a chosen port, retry if failed
            while (bind_success == 0 && try < PIPEGEN_SOCKET_LISTEN_RETRY_TIME) {
                try++;

                // randomly select a port between 8000-9000
                listen_port = PIPEGEN_SOCKET_PORT_BASE + rand() % 1000;
                sprintf(listen_port_str, "%d", listen_port);

                serv_addr.sin_port = htons(listen_port);

                if (bind(import_socket, (struct sockaddr *) &serv_addr, sizeof(serv_addr)) == 0) {
                    bind_success = 1;
                }
            }
            if (bind_success == 0) {
                printf("ERROR on binding. Failed after %d tries.\n", try);
                return NULL;
            }
            
            listen(import_socket,5);
            
        }

        if (register_importer(path, "0.0.0.0", listen_port_str) != 0) {
            printf("Failed to register to directory.\n");
            return NULL;
        }

        struct sockaddr_in cli_addr;
        socklen_t clilen = sizeof(cli_addr);
        int newsockfd = accept(import_socket, (struct sockaddr *)&cli_addr, &clilen);

        if (newsockfd < 0) {
            perror("ERROR on accept.");
            return NULL;
        }

        char buffer[1];
        buffer[0] = '\0';
        while (1) {
            int n = read(newsockfd,buffer,1);
            if (n > 0) {
                if (buffer[0] == '\n')
                    break;
            }
        }
        return fdopen(newsockfd, mode);
    } else {
        // write mode
        // export

        struct DB_ADDRESS *pipegen_importer = get_importer(verification ? "*" : path);
        
        // open socket
        int socket_desc;
        struct sockaddr_in server;
         
        //Create socket
        socket_desc = socket(AF_INET , SOCK_STREAM , 0);
        if (socket_desc < 0) {
            printf("Could not create socket.");
            return NULL;
        }
        bzero((char *) &server, sizeof(server));   
        server.sin_addr.s_addr = inet_addr(pipegen_importer->ip/*PIPEGEN_SOCKET_HOST*/);
        server.sin_family = AF_INET;
        server.sin_port = htons(atoi(pipegen_importer->port));
        //Connect to remote server
        if (connect(socket_desc , (struct sockaddr *)&server , sizeof(server)) < 0) {
            puts("Connect error");
            return NULL;
        }
        free(pipegen_importer);
        FILE *f = fdopen(socket_desc, mode);
        fputc('\0', f);
        // send filename to socket before return
        fputs(path, f);
        // send seperator \n
        fputc('\n', f);
        return f;
    }
}

unsigned long pipegen_fopen(unsigned long path, unsigned long mode) {
    return (unsigned long)pipegen_fopen_st((char *)path, (char *)mode);
}