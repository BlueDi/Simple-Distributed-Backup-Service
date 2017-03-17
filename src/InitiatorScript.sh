mkdir ../bin
javac -d ../bin peer/*.java
java -cp ../bin peer.Client 4001 BACKUP test1.pdf 3 