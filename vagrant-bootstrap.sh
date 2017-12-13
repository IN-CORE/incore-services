#install oracle jdk 8
apt-get update
apt-get install -y software-properties-common python-software-properties
echo oracle-java8-installer shared/accepted-oracle-license-v1-1 select true |  /usr/bin/debconf-set-selections
add-apt-repository ppa:webupd8team/java -y
apt-get update
apt-get install oracle-java8-installer
echo "Setting environment variables for Java 8.."
apt-get install -y oracle-java8-set-default
apt-get install mongodb



#install kong
cd /tmp
wget -O kong-community-edition-0.11.0.precise.all.deb https://bintray.com/kong/kong-community-edition-deb/download_file?file_path=dists/kong-community-edition-0.11.0.precise.all.deb
apt-get install -y openssl libpcre3 procps perl
dpkg -i kong-community-edition-0.11.0.*.deb
#kong requires posgresql
apt-get install -y postgresql
sudo -u postgres psql -c "CREATE USER kong; GRANT kong to postgres;"
sudo -u postgres psql -c "CREATE DATABASE kong OWNER kong;"
sudo -u postgres psql -c "ALTER USER kong WITH password 'k0ng';"
#patch kong to support ldaps until they merge my ldaps PR
cd /usr/local/share/lua/5.1/kong/plugins/ldap-auth
sudo cp -f /vagrant/server/kong/access.lua .
#run kong migrations
sudo kong migrations up -c /vagrant/server/kong/kong.conf
sudo ln -s /vagrant/server/kong/kong.service /lib/systemd/system/kong.service
sudo systemctl enable kong
#start kong service
sudo service kong start
#now need to setup the kong server, but that will be in a different script
source /vagrant/server/kong/incore.kong.sh

#install logstash
cd /tmp
wget -qO - https://artifacts.elastic.co/GPG-KEY-elasticsearch | sudo apt-key add -
apt-get update
apt-get install apt-transport-https
echo "deb https://artifacts.elastic.co/packages/6.x/apt stable main" | sudo tee -a /etc/apt/sources.list.d/elastic-6.x.list
apt-get update
apt-get install logstash
ln -s /vagrant/server/logstashPipeline.conf /etc/logstash/conf.d
systemctl start logstash.service

#install datawolf
wget -O datawolf.zip https://opensource.ncsa.illinois.edu/bamboo/browse/WOLF-MAIN-45/artifact/CORE/datawolf-webapp-all.zip/datawolf-webapp-all-4.1.0-bin.zip
cd /opt
sudo mkdir datawolf
cd datawolf
sudo unzip /tmp/datawolf.zip
#create the systemd service
sudo ln -s /vagrant/vagrant/datawolf-service.sh /lib/systemd/system/datawolf.service
sudo systemctl enable datwolf
sudo service datawolf start

#setup mongo and install data?
mongo < /vagrant/vagrant/mongo-seed.js
