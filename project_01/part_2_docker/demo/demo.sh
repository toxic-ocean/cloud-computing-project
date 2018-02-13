cd ${HADOOP_HOME}
bin/hdfs dfs -mkdir /input
bin/hdfs dfs -put testfile/* /input
bin/hadoop jar share/hadoop/mapreduce/hadoop-mapreduce-examples-3.0.0.jar wordcount /input /output
bin/hdfs dfs -cat /output/*