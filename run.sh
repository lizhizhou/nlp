#export SPARK_JAVA_OPTS="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=10000"
~/Downloads/spark-2.1.0-bin-hadoop2.7/bin/spark-submit --driver-memory 1g --master local[4] --class NLP ~/nlp/target/scala-2.11/NLP\ Project-assembly-1.0.jar

#SPARK_PRINT_LAUNCH_COMMAND=true ~/Downloads/spark-2.1.0-bin-hadoop2.7/bin/spark-submit --driver-memory 4g --jars /home/bigdata/Downloads/stanford-corenlp-full-2016-10-31/stanford-corenlp-3.7.0-models.jar,/home/bigdata/Downloads/stanford-corenlp-full-2016-10-31/stanford-chinese-corenlp-2016-10-31-models.jar,/home/bigdata/Downloads/stanford-corenlp-full-2016-10-31/ejml-0.23.jar,/home/bigdata/Downloads/stanford-corenlp-full-2016-10-31/stanford-corenlp-3.7.0.jar,/home/bigdata/Downloads/stanford-corenlp-full-2016-10-31/protobuf.jar,/home/bigdata/Downloads/stanford-corenlp-full-2016-10-31/jollyday.jar --class NLP ~/nlp/target/scala-2.11/NLP\ Project-assembly-1.0.jar

#spark-shell --master yarn --deploy-mode client --queue multivac --driver-cores 5 --driver-memory 8g --executor-cores 5 --executor-memory 4g --num-executors 30 --jars /home/jars/stanford-corenlp-3.7.0/ejml-0.23.jar,/home/jars/stanford-corenlp-3.7.0/stanford-corenlp-3.7.0.jar,/home/jars/stanford-corenlp-3.7.0/stanford-corenlp-3.7.0-models.jar,/home/jars/stanford-corenlp-3.7.0/protobuf.jar,/home/jars/stanford-corenlp-3.7.0/jollyday.jar
