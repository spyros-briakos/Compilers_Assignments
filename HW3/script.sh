#!/bin/bash
msg="$*"
repl=".ll"
llvmfile="${msg/.java/$repl}"
make
java Main "$msg"
clang -o out "$llvmfile"
./out

# rm "$llvmfile"