#install oracle jdk 8 and other base requirements
apt-get update
apt-get install -y software-properties-common python-software-properties
apt-get install -y openjdk-8-jdk
apt-get install -y mongodb mongodb-clients
apt-get install -y unzip

#setup incore user
sudo useradd -r incore


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
sudo cp -f /vagrant/server/kong/schema.lua .

#hack kong so that instead of caching ldap password in db,
#it stores and caches a token and uses that instead
sudo cp -f /vagrant/server/kong/access.lua .

#run kong migrations
sudo kong migrations up -c /vagrant/server/kong/kong.conf
sudo ln -s /vagrant/server/kong/kong.service /lib/systemd/system/kong.service
sudo systemctl enable kong

#start kong service
sudo service kong start

#now need to setup the kong server config, but that will be in a different script
source /vagrant/vagrant/kong-setup.sh

#need to replace the consumer_id in the hacked access.lua so our hack works
ANON_JSON=`curl -X GET  --url http://localhost:8001/consumers/anonymous`
ANON_ID=`echo "$ANON_JSON" | sed -e 's/.*"id":"\(.*\)"}/\1/g'`
sed -i "s/%%%CONSUMER_ID%%%/$ANON_ID/g" /usr/local/share/lua/5.1/kong/plugins/ldap-auth/access.lua
sudo service kong restart



#install logstash
cd /tmp
wget -qO - https://artifacts.elastic.co/GPG-KEY-elasticsearch | sudo apt-key add -
apt-get update
apt-get install -y apt-transport-https
echo "deb https://artifacts.elastic.co/packages/6.x/apt stable main" | sudo tee -a /etc/apt/sources.list.d/elastic-6.x.list
apt-get update
apt-get install -y logstash
ln -s /vagrant/server/logstashPipeline.conf /etc/logstash/conf.d
sudo systemctl enable logstash
systemctl start logstash.service

#make mongo available to host
sed -i 's/bind_ip/#bind_ip/g' /etc/mongodb.conf
sudo service mongodb restart


#install datawolf
wget -O datawolf.zip https://opensource.ncsa.illinois.edu/bamboo/browse/WOLF-MAIN-45/artifact/CORE/datawolf-webapp-all.zip/datawolf-webapp-all-4.1.0-bin.zip
cd /opt
sudo mkdir datawolf
cd datawolf
sudo unzip /tmp/datawolf.zip
sudo mv /opt/datawolf/datawolf-webapp*/* /opt/datawolf
sudo rmdir  /opt/datawolf/datawolf-webapp*
sudo chown -R incore /opt/datawolf
#create the systemd service
sudo ln -s /vagrant/vagrant/datawolf-service.sh /lib/systemd/system/datawolf.service
sudo service datawolf start

#setup mongo and install data?
#mongo < /vagrant/vagrant/mongo-seed.js


#setup pyincore and datawolf workflows
cd /home/ubuntu/pyincore
sudo python3 setup.py install
sudo ln -s /home/ubuntu/analyses/eq_bldg_str_dmg/building_damage.py /usr/local/bin
sudo apt-get -y install python3-pip libgdal-dev python3-tk
pip3 install numpy
pip3 install scipy
pip3 install rasterio
pip3 install fiona
pip3 install jsonpickle
pip3 install shapely
pip3 install matplotlib wikidata docopt

sudo service datawolf start

cd /home/ubuntu/analyses/eq_bldg_str_dmg
curl -X POST -F "workflow=@bldg_dmg_workflow.zip" http://localhost:8888/datawolf/workflows
