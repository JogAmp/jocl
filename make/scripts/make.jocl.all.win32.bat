set THISDIR="C:\JogAmp"

set J2RE_HOME=c:\jdk-21
set JAVA_HOME=c:\jdk-21
set ANT_PATH=C:\apache-ant-1.10.5
set GIT_PATH=C:\cygwin64\bin
set SEVENZIP=C:\Program Files\7-Zip

set CMAKE_PATH=C:\cmake-3.25.1-windows-x86_64
set CMAKE_C_COMPILER=c:\mingw32\bin\gcc

set PATH=%J2RE_HOME%\bin;%JAVA_HOME%\bin;%ANT_PATH%\bin;c:\mingw32\bin;%CMAKE_PATH%\bin;%GIT_PATH%;%SEVENZIP%;%PATH%

REM set LIB_GEN=%THISDIR%\lib
REM set CLASSPATH=.;%THISDIR%\build-win32\classes

REM set JOGAMP_JAR_CODEBASE=Codebase: *.jogamp.org
set JOGAMP_JAR_CODEBASE=Codebase: *.goethel.localnet

ant -Drootrel.build=build-win32 %1 %2 %3 %4 %5 %6 %7 %8 %9 > make.jocl.all.win32.log 2>&1
