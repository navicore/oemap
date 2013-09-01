Ubuntu 12.04 LTS 64-bit

useradd USERNAME -m -s /bin/bash
passwd USERNAME

# for vim get .vimrc and .vim dir from user git

# for ssh
# from client machine (your laptop or desktop) to server machine:
ssh-copy-id -i ~/.ssh/id_rsa.pub esweeney@nashua.onextent.com

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

#redis
# add the 2 lines below to /etc/apt/sources.list.d/dotdeb.org.list
deb http://packages.dotdeb.org squeeze all
deb-src http://packages.dotdeb.org squeeze all

wget -q -O - http://www.dotdeb.org/dotdeb.gpg | sudo apt-key add -

apt-get update
apt-get install redis-server

echo 1 > /proc/sys/vm/overcommit_memory

