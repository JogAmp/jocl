#! /bin/bash

export DYLD_LIBRARY_PATH=/usr/local/libav:$DYLD_LIBRARY_PATH

JAVA_HOME=`/usr/libexec/java_home -version 21`
#JAVA_HOME=`/usr/libexec/java_home -version 1.7.0_25`
#JAVA_HOME=`/usr/libexec/java_home -version 1.6.0`
PATH=$JAVA_HOME/bin:$PATH
export JAVA_HOME PATH

spath=`dirname $0`

. $spath/tests.sh  $JAVA_HOME/bin/java -DummyArg ../build-macosx $*

