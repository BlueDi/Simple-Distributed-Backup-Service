@echo off

mkdir bin

javac -d bin src/handlers/*.java src/interfaces/*.java src/peer/*.java

java -cp bin peer.Peer 224.0.0.2 4002 224.0.0.3 4003 224.0.0.4 4004

pause