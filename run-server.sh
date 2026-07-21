#!/bin/bash
# start the vibematch server
# run this first in one terminal, then run ./run.sh in another

mkdir -p out
echo "compiling..."
javac -cp "lib/*" $(find . -name "*.java" ! -path "./model_cs/*" ! -path "./controller/*" ! -path "./view/Community.java" ! -path "./view/Message.java") -d out
if [ $? -ne 0 ]; then
    echo "compile failed"
    exit 1
fi

echo "starting server..."
java -cp "out:lib/*" server.ServerMain
