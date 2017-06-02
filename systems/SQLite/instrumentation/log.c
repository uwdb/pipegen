#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#define PIPEGEN_FILE_NAME "pipegen.csv"
#define PATH_MAX 4096

void pipegen_instrumentation_logging(int idx, unsigned long fnaddr) {
	char *filename = (char *)fnaddr;
	// if (strncmp(PIPEGEN_FILE_NAME, filename, strlen(PIPEGEN_FILE_NAME)) == 0) {
		// printf("Logging %d: %s\n", idx, filename);
		FILE *f = fopen("pipegen_instrumentation.log", "a");
		if (!f) {
			printf("Failed to log call %d.", idx);
			exit(1);
		}
		char str[PATH_MAX];
		sprintf(str, "%d %s\n", idx, filename);
		fwrite(str, sizeof(char), strlen(str), f);
		fclose(f);
	// }
}
