#!/bin/bash
mkdir /mnt/data/imgraph
mkdir /mnt/data/imgraph/software
cd /mnt/data/imgraph/software
wget http://download.zeromq.org/zeromq-3.2.2.tar.gz
tar xvfz zeromq-3.2.2.tar.gz
rm zeromq-3.2.2.tar.gz
cd zeromq-3.2.2
sudo apt-get -y install libtool autoconf
sudo apt-get clean
sudo apt-get -y install uuid-dev g++
sudo apt-get clean
sudo apt-get -y install make
sudo apt-get clean
./configure
make
sudo make install
sudo ldconfig

