@echo off

mkdir bin

javac -d bin src/handlers/*.java src/interfaces/*.java src/peer/*.java

java -cp bin peer.Client 1993 REMOVED 100000

pause
