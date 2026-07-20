#!/bin/bash
# start the vibematch client
# pass a server address to connect to another machine: ./run.sh 192.168.1.20
# default is localhost

mkdir -p out
echo "compiling..."
javac -cp "lib/*" $(find . -name "*.java" ! -path "./view/*" ! -path "./model_cs/*" ! -path "./controller/*") -d out
if [ $? -ne 0 ]; then
    echo "compile failed"
    exit 1
fi

echo "starting vibematch..."
java -cp "out:lib/*" app.Main "$@"
