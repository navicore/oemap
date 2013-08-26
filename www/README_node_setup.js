sudo adduser --system --shell /bin/bash --gecos 'user for running node.js projects' --group --disabled-password --home /home/node node

sudo cp upstart/oemapd.conf /etc/init/oemapd.conf

sudo touch /var/log/oemapd.log
sudo chown node /var/log/oemapd.log

sudo start oemapd

