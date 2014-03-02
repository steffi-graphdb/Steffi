#!/bin/bash
cd ..
cd prerequisites
git clone https://github.com/zeromq/jzmq.git
cd jzmq
./autogen.sh
./configure
make
sudo make install
mvn clean package
mvn install:install-file -Dfile=./target/jzmq4-4.0.0-SNAPSHOT.jar -DgroupId=org.zeromq -DartifactId=jzmq4 -Dversion=4.0.0-SNAPSHOT -Dpackaging=jar
mvn install:install-file -Dfile=./target/jzmq4-4.0.0-SNAPSHOT-native-amd64-Linux.jar -DgroupId=org.zeromq -DartifactId=jzmq4-native -Dversion=4.0.0-SNAPSHOT -Dpackaging=jar

