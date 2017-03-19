#!/bin/sh
mkdir bin
javac -d bin src/handlers/*.java src/interfaces/*.java src/peer/*.java
java -cp bin peer.Client 4001 BACKUP test1.pdf 3 
