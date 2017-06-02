# Decompile SQLite binary to LLVM byte code with Mcsema

## Files

sqlite3.bc: reconstructed from non-amalgamation source  
sqlite3_1.bc: reconstructed from amalgamation source, not working correctly  

## Tools
| Name | Version | 
| ---- | ------- |
| [gcc](https://gcc.gnu.org/) | Latest |
| [clang](http://clang.llvm.org/) | 3.8 |
| [mcsema](https://github.com/trailofbits/mcsema/) | Latest | 
| [IDA Pro](https://www.hex-rays.com/products/ida) | 6.7+|
| [SQLite](https://github.com/mackyle/sqlite) | 3.7.5+ |

## Platform
Mcsema and IDA Pro is installed on Windows 10 64 bit.
SQLite binary is compiled on Ubuntu 16.04 64 bit virtual machine.

## Steps

### 1. Compile SQLite amalgamation source with gcc
```
gcc shell.c sqlite3.c -lpthread -ldl -o sqlite3
```

### 2. Copy the executable to Windows machine where Mcsema and IDA Pro is installed

### 3. Generate control flow graph
```
PATH_TO_MCSEMA\bin\mcsema-disass.exe --disassembler "PATH_TO_IDA_PRO\idaq64.exe" --arch amd64 --os linux --binary PATH_TO_BINARY\sqlite3 --output TEMP_DIR\sqlite3.cfg --entrypoint main --log_file TEMP_DIR\sqlite3.log
```

### 4. Generate LLVM byte code from control flow graph
```
PATH_TO_MCSEMA\build\Release\mcsema-lift.exe --arch amd64 --os linux --cfg TEMP_DIR\sqlite3.cfg --entrypoint main --output TEMP_DIR\sqlite3.bc
```

### 5. Copy the sqlite3.bc file back to Linux

### 6. Regenerate executable from the byte code
We need a mcsema library to link the byte code.  
```
libmcsema_rt64.a  # for 64 bit
libmcsema_rt32.a  # for 32 bit
```
The library is located in lib directory of the mcsema directory.  
```
clang sqlite3.bc PATH_TO_MCSEMA/lib/libmcsema_rt64.a -lpthread -ldl -o sqlite3
```


