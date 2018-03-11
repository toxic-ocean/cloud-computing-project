export MASTER_IP=159.203.71.174
export SLAVE_IP=159.203.167.80

# generate ssh key on master
ssh-keygen -t rsa -P ""

#delivery ssh key from master to every node
ssh-copy-id -i $HOME/.ssh/id_rsa.pub root@master
ssh-copy-id -i $HOME/.ssh/id_rsa.pub root@slave

# test none-password SSH login
ssh slave

#Configure cluster
cd /usr/local/hadoop

#assign master on every node
#nano etc/hadoop/masters
#assign slaves on master node
#master and slave nodes both act as slaves
nano etc/hadoop/workers

#setup enviroments in $HOME/.bashrc
## Set Hadoop-related environment variables
export HADOOP_PREFIX=/usr/local/hadoop
export HADOOP_HOME=/usr/local/hadoop
export HADOOP_CONF_DIR=${HADOOP_HOME}/etc/hadoop
export HADOOP_MAPRED_HOME=${HADOOP_HOME}
export HADOOP_COMMON_HOME=${HADOOP_HOME}
export HADOOP_HDFS_HOME=${HADOOP_HOME}
export YARN_HOME=${HADOOP_HOME}
export HADOOP_LIBEXEC_DIR=${HADOOP_HOME}/libexec
export HADOOP_COMMON_LIB_NATIVE_DIR=${HADOOP_HOME}/lib/native
export HADOOP_OPTS="-Djava.library.path=${HADOOP_HOME}/lib"
export YARN_NODEMANAGER_OPTS="-XX:+UseParallelGC -Xmx2g"
## Add Hadoop bin/ directory to PATH
export PATH=$PATH:$HADOOP_PREFIX/bin
# set the user to run hadoop
export HDFS_NAMENODE_USER=root
export HDFS_DATANODE_USER=root
export HDFS_SECONDARYNAMENODE_USER=root
export YARN_RESOURCEMANAGER_USER=root
export YARN_NODEMANAGER_USER=root
# set pdsh to use ssh
export PDSH_RCMD_TYPE=ssh

nano ~/.bashrc
#copy .bashrc
scp -r ~/.bashrc root@slave:/root/

source ~/.bashrc



#create directory for HDFS
#create directories for Name node and data node on master
mkdir -p /hdfs/namenode
mkdir -p /hdfs/datanode
#create direcotry for Datanode on slave
ssh root@slave "mkdir -p /hdfs/datanode"

#configure the setting
cd $HADOOP_PREFIX/etc/hadoop/
#See the example in the HadoopTutorial package
nano core-site.xml
nano hdfs-site.xml
nano yarn-site.xml
nano mapred-site.xml
#copy the setting to slave
scp -r * root@slave:/usr/local/hadoop/etc/hadoop/

#Formate the namenode
hadoop namenode -format
