#!/bin/bash
# start the vibematch client
# pass a server address to connect to another machine: ./run.sh 192.168.1.20
# default is localhost

mkdir -p out
echo "compiling..."
# build everything EXCEPT the old in-memory layer: model_cs/ and controller/ are
# Khalil's original stand-ins (replaced by the real server now), and view's own
# Community/Message demo classes are unused since the UI uses model/ + net/Api.
javac -cp "lib/*" $(find . -name "*.java" ! -path "./model_cs/*" ! -path "./controller/*" ! -path "./view/Community.java" ! -path "./view/Message.java") -d out
if [ $? -ne 0 ]; then
    echo "compile failed"
    exit 1
fi

echo "starting VibeMatch..."
java -cp "out:lib/*" view.Main "$@"
