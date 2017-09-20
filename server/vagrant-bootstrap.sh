#install oracle jdk 8
apt-get update
apt-get install -y software-properties-common python-software-properties
echo oracle-java8-installer shared/accepted-oracle-license-v1-1 select true |  /usr/bin/debconf-set-selections
add-apt-repository ppa:webupd8team/java -y
apt-get update
apt-get install oracle-java8-installer
echo "Setting environment variables for Java 8.."
apt-get install -y oracle-java8-set-default



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
kong migrations up -c /vagrant/kong.conf
kong start -c /vagrant/kong.conf


