@echo off

mkdir bin

javac -d bin src/handlers/*.java src/interfaces/*.java src/peer/*.java

cd bin

start rmiregistry

cd ..

java -Djava.rmi.server.codebase=file:bin/ -cp bin peer.Peer 1.0 1994 1994 224.0.0.2 4002 224.0.0.3 4003 224.0.0.4 4004

pause 
