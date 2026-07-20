#!/bin/bash
# start the vibematch server
# run this first in one terminal, then run ./run.sh in another

mkdir -p out
echo "compiling..."
javac -cp "lib/*" $(find . -name "*.java" ! -path "./view/*" ! -path "./model_cs/*" ! -path "./controller/*") -d out
if [ $? -ne 0 ]; then
    echo "compile failed"
    exit 1
fi

echo "starting server..."
java -cp "out:lib/*" server.ServerMain
