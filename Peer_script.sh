#!/bin/sh
mkdir bin
javac -d bin src/handlers/*.java src/interfaces/*.java src/peer/*.java
java -cp bin peer.Peer 224.0.0.1 4001 224.0.0.2 4002 224.0.0.3 4003
