Ubuntu 12.04 LTS 64-bit

useradd USERNAME -m -s /bin/bash
passwd USERNAME

sudo adduser --system --shell /bin/bash --gecos 'user for running node.js projects' --group --disabled-password --home /home/node node

apt-get install git
apt-get install build-essential automake libtool autoconf
apt-get install python-dev

#for nodejs
apt-get install python-software-properties python g++ make
add-apt-repository ppa:chris-lea/node.js
apt-get update
apt-get install nodejs

#for mongodb
apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv 7F0CEB10
echo 'deb http://downloads-distro.mongodb.org/repo/ubuntu-upstart dist 10gen' | sudo tee /etc/apt/sources.list.d/10gen.list
apt-get update
apt-get install mongodb-10gen



