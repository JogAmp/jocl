
set BLD_SUB=build-win32
set J2RE_HOME=c:\jdk-21
set JAVA_HOME=c:\jdk-21
set ANT_PATH=C:\apache-ant-1.9.4

set PROJECT_ROOT=D:\projects\jogamp\jogl
set BLD_DIR=..\%BLD_SUB%

REM set FFMPEG_LIB=C:\ffmpeg_libav\lavc53_lavf53_lavu51-ffmpeg\x32
set FFMPEG_LIB=C:\ffmpeg_libav\lavc55_lavf55_lavu52-ffmpeg\x32
REM set FFMPEG_LIB=C:\ffmpeg_libav\lavc54_lavf54_lavu52_lavr01-libav\x32

REM set PATH=%JAVA_HOME%\bin;%ANT_PATH%\bin;%PATH%
REM set PATH=%JAVA_HOME%\bin;%ANT_PATH%\bin;%PROJECT_ROOT%\make\lib\external\angle\win32\20120127;%PATH%
REM set PATH=%JAVA_HOME%\bin;%ANT_PATH%\bin;%PROJECT_ROOT%\make\lib\external\angle\win32\20121010-chrome;%PATH%
REM set PATH=%JAVA_HOME%\bin;%ANT_PATH%\bin;%PROJECT_ROOT%\make\lib\external\PVRVFrame\OGLES-2.0\Windows_x86_32;%PATH%
set PATH=%JAVA_HOME%\bin;%ANT_PATH%\bin;%FFMPEG_LIB%;%PATH%

set BLD_DIR=..\%BLD_SUB%
REM set LIB_DIR=..\..\gluegen\%BLD_SUB%\obj;%BLD_DIR%\lib
set LIB_DIR=

set CP_ALL=.;%BLD_DIR%\jar\jocl.jar;%BLD_DIR%\jar\jocl-test.jar;..\..\jogl\%BLD_SUB%\jar\jogl-all.jar;..\..\joal\%BLD_SUB%\jar\joal.jar;..\..\gluegen\%BLD_SUB%\gluegen-rt.jar;..\..\gluegen\make\lib\junit.jar;%ANT_PATH%\lib\ant.jar;%ANT_PATH%\lib\ant-junit.jar;%BLD_DIR%\..\make\lib\swt\win32-win32-x86\swt-debug.jar

echo CP_ALL %CP_ALL%

REM set D_ARGS="-Djogamp.debug=all" "-Djocl.debug=all"
REM set D_ARGS="-Djogamp.debug.ProcAddressHelper" "-Djogamp.debug.NativeLibrary" "-Djogamp.debug.NativeLibrary.Lookup" "-Djogamp.debug.JNILibLoader" "-Djogamp.debug.TempJarCache" "-Djogamp.debug.JarUtil" "-Djocl.debug=all"
set D_ARGS="-Djogamp.debug.ProcAddressHelper" "-Djogamp.debug.NativeLibrary" "-Djogamp.debug.NativeLibrary.Lookup" "-Djogamp.debug.JNILibLoader"

REM set X_ARGS="-Dsun.java2d.noddraw=true" "-Dsun.awt.noerasebackground=true"

scripts\tests-win.bat %*
