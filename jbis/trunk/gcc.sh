#! /bin/sh
jdk="/cygdrive/c/Programmi/Java/jdk1.6.0"
echo jdk=$jdk
gcc -mno-cygwin -I$jdk/include -I$jdk/include/win32 -LBisKyoUnit.dll -Wl,--add-stdcall-alias -shared -o JBisKyoUnit.dll JBisKyoUnit.c
