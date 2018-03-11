export MASTER_IP=159.203.71.174
export SLAVE_IP=159.203.167.80

#ssh login
ssh $MASTER_IP

######WARNING: 
#change the password to a strong enough password if you do not want to lose what you did in the VM
##############

# Commands on every node
#setup JAVA 8
sudo add-apt-repository ppa:webupd8team/java 
sudo apt-get update
sudo apt-get install oracle-java8-installer
sudo update-alternatives --config java

# Config java path
# add JAVA_HOME="/usr/lib/jvm/java-8-oracle" to /etc/environment
sudo nano /etc/environment

#test java setup
source /etc/environment
echo $JAVA_HOME
java -version

#setup ssh and rsync
sudo apt-get install ssh
sudo apt-get install pdsh

#download Hadoop and test locally
cd /usr/local/
wget http://www-us.apache.org/dist/hadoop/common/hadoop-3.0.0/hadoop-3.0.0.tar.gz
tar -zxf hadoop-3.0.0.tar.gz
ln -s hadoop-3.0.0 hadoop
cd hadoop

# set to the root of your Java installation
# export JAVA_HOME=/usr/lib/jvm/java-7-oracle
nano etc/hadoop/hadoop_env.sh

#test single node hadoop
mkdir input
cp etc/hadoop/*.xml input
bin/hadoop jar share/hadoop/mapreduce/hadoop-mapreduce-examples-3.0.0.jar grep input output 'dfs[a-z.]+'
cat output/*

# setup etc/hosts with the IP and Node Name on every node
# 159.203.71.174 master
# 159.203.167.80 slave
# Warning: delete every IP setting with 127.0.1.1 and 127.0.0.1
nano /etc/hosts

#modify the hostname
nano /etc/hostname
# in master make the file look like:
###### /etc/hostname
# master
######
# in master change the hostname with the command
hostname master

# in slave make the file look like:
###### /etc/hostname
# slave
######
# in slave change the hostname with the command
hostname slave
