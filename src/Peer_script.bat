@echo off

javac -d ../bin peer/*.java

java peer.Peer 224.0.0.3 4000 224.0.0.3 4000 224.0.0.3 4000 8000

pause
