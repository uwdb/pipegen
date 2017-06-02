#!/bin/bash

rm pipegen_instrumentation.log
rm log.bc
rm sqlite3_1.bc
rm pipegen.bc
rm sqlite3_1.ll
rm sqlite3_2.ll
rm sqlite3_2_1.bc
rm sqlite3_2_1.ll
rm sqlite3_2_2.ll
rm sqlite3


clang -emit-llvm log.c -c -o log.bc
make -j8
bin/opt -load lib/LLVMPipegen.so -instrumentation1 < sqlite3.bc > sqlite3_1.bc
# clang 3.8 cannot read llvm 5 bytecode, need to convert to ll file and do some tweaks
bin/llvm-dis sqlite3_1.bc -o sqlite3_1.ll
# llvm 5 will create a new line that llvm 3.8 does not recognize, need to delete that line
# delete the second line of the file
sed '2d' sqlite3_1.ll > sqlite3_2.ll 
clang sqlite3_2.ll log.bc ../../mcsema/lib/libmcsema_rt64.a -lpthread -ldl -lsqlite3 -o sqlite3


echo 'Running import test case...'
# tclsh m1.test
./sqlite3 << EOS
create table foo(a, b);
.separator ,
.import pipegen.csv foo
EOS
echo 'Import log generated.'

bin/opt -load lib/LLVMPipegen.so -instrumentation2 < sqlite3.bc > sqlite3_2_1.bc
bin/llvm-dis sqlite3_2_1.bc -o sqlite3_2_1.ll
sed '2d' sqlite3_2_1.ll > sqlite3_2_2.ll 
clang -Dverification=1 -emit-llvm pipegen.c -c -o pipegen.bc
clang sqlite3_2_2.ll pipegen.bc ../../mcsema/lib/libmcsema_rt64.a -lpthread -ldl -lcurl -lsqlite3 -o sqlite3

echo 'Verifying...'
./sqlite3 << EOS
create table foo(a, b);
.separator ,
.import pipegen.csv foo
EOS

if [[ $? != 0 ]]; then
	echo 'Import Verification Failed.'
	exit
fi

echo 'Import Verification Success.'