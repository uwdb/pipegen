#include "pipegen.h"
#include "linkedlist.h"

struct DB_ADDRESS *pipegen_importer = NULL;
int import_socket = -1;
LinkedList curl_handles = NULL;
int ccurl_global_init = 0;
// int verification = 1;

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

    // printf("Retrieving importer...\n");

    if (curl_handles == NULL) {
        curl_handles = init_linkedlist();
    }

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
    insertFirst(curl_handles, 0, curl);
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
    return db;
}

// return 0 if success
int register_importer(const char *name, char *ip, char *port) {
    CURL *curl;
    CURLcode res;
    char *request_url;

    if (curl_handles == NULL) {
        curl_handles = init_linkedlist();
    }

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
    insertFirst(curl_handles, 0, curl);
    int ret = 0;
    if(curl) {
        curl_easy_setopt(curl, CURLOPT_URL, request_url);
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
    return ret;
}

FILE *pipegen_fopen_st(char *path, char *mode) {
    if (verification || strncmp(PIPEGEN_SOCKET_NAME, path, strlen(PIPEGEN_SOCKET_NAME)) == 0) {
        if (mode[0] == 'r') {
            if (register_importer(path, "0.0.0.0", PIPEGEN_SOCKET_PORT_STR) != 0) {
                printf("Failed to register to directory.\n");
                return NULL;
            }

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
                serv_addr.sin_port = htons(PIPEGEN_SOCKET_PORT);

                if (bind(import_socket, (struct sockaddr *) &serv_addr, sizeof(serv_addr)) < 0) {
                    printf("ERROR on binding.");
                    return NULL;
                }
                listen(import_socket,5);
                
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

            if (pipegen_importer == NULL) {
                pipegen_importer = get_importer(verification ? "*" : path);
            }
            
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
            server.sin_port = htons(atoi(pipegen_importer->port)/*PIPEGEN_SOCKET_PORT*/);
            //Connect to remote server
            if (connect(socket_desc , (struct sockaddr *)&server , sizeof(server)) < 0)
            {
                puts("Connect error");
                return NULL;
            }
            FILE *f = fdopen(socket_desc, mode);
            fputc('\0', f);
            // send filename to socket before return
            fputs(path, f);
            // send seperator \n
            fputc('\n', f);
            return f;
        }
        
    } else {
        return fopen(path, mode);
    }
}

unsigned long pipegen_fopen(unsigned long path, unsigned long mode) {
    return (unsigned long)pipegen_fopen_st((char *)path, (char *)mode);
}

void cleanup_curl() {
    while(!isEmpty(curl_handles)) {
        struct node *n = deleteFirst(curl_handles);
        curl_easy_cleanup((CURL *)n->data);
        free(n);
    }
    free(curl_handles);
}