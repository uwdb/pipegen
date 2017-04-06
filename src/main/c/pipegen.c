#include "pipegen.h"

struct DB_ADDRESS *pipegen_importer = NULL;

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

    request_url = (char *)malloc(url_length);
    strncpy(request_url, PIPEGEN_DIRECTORY_ADDRESS, strlen(PIPEGEN_DIRECTORY_ADDRESS));
    strncat(request_url, PIPEGEN_DIRECTORY_EXPORT_SUB, strlen(PIPEGEN_DIRECTORY_EXPORT_SUB));
    strncat(request_url, PIPEGEN_SYSTEM_PARAM, strlen(PIPEGEN_SYSTEM_PARAM));
    strncat(request_url, name, strlen(name));

    curl_global_init(CURL_GLOBAL_ALL);
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
        free(chunk.memory);
        curl_easy_cleanup(curl);
    }
    return db;
}

FILE *pipegen_fopen(const char *path, const char *mode) {
    
    if (strncmp(PIPEGEN_SOCKET_NAME, path, strlen(PIPEGEN_SOCKET_NAME)) == 0) {
        if (pipegen_importer == NULL) {
            pipegen_importer = get_importer("*");
        }
        
        // open socket
        int socket_desc;
        struct sockaddr_in server;
         
        //Create socket
        socket_desc = socket(AF_INET , SOCK_STREAM , 0);
        if (socket_desc == -1) {
            printf("Could not create socket.");
            return NULL;
        }
             
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
    } else {
        return fopen(path, mode);
    }
}

// int main() {
//     pipegen_importer = get_importer("*");
//     printf("%s:%s\n", pipegen_importer->ip, pipegen_importer->port);
//     return 0;
// }