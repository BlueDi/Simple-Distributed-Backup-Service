@echo off

mkdir ..\bin

javac -d ../bin peer/*.java

java -cp ../bin peer.Peer 224.0.0.3 4000 224.0.0.3 4000 224.0.0.3 4000

pause
