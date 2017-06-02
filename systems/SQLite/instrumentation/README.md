# Instrumentation

## Files

| name | description |
| ---- | ----------- |
| pipegen.cpp | LLVM pass used to instrument source code. Need to put it in LLVM source directory. |
| LLVMPipegen.so | Linked version of pipegen LLVM pass |
| log.c | Log function call in instrumentation step |
| pipegen.c | Pipegen version of fopen call |
| pipegen_inst_import.sh | Script to replace import related fopen calls |
| pipegen_inst_export.sh | Script to replace export related fopen calls |

## Steps

1. Compile pipegen LLVM Pass to generate LLVMPipegen.so. You might be able to use LLVMPipegen.so provided, but it really depends on the version of the LLVM source you are using.  

2. Put decompiled bytecode of sqlite3 in the same directory as the log.c and the script in LLVMPATH/cmake directory.  

3. run the script for import or export and it will generate the pipegen version of sqlite3 (file name: sqlite3). For detailed information about the script, please check the comments.