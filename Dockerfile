FROM tensorflow/tensorflow:1.12.0-py3
MAINTAINER Zhizhou Li <lizhizhou1983@gmail.com>
RUN apt-get update
RUN apt install -y tmux
RUN apt install -y git
RUN apt install -y python3-tk

RUN mkdir ~/.pip
RUN echo "[global]" >> ~/.pip/pip.conf
RUN echo "trusted-host=pypi.douban.com" >> ~/.pip/pip.conf
RUN echo "index-url = http://pypi.douban.com/simple" >> ~/.pip/pip.conf

# PYTHON
RUN pip3 install pandas
RUN pip3 install django

# MACHINELEANING
RUN pip3 install sklearn

# DEEPLEANING
RUN pip3 install keras
RUN pip3 install tensorlayer

# JAVA
ARG JAVA_MAJOR_VERSION=8
ARG JAVA_UPDATE_VERSION=131
ARG JAVA_BUILD_NUMBER=11
ENV JAVA_HOME /usr/jdk1.${JAVA_MAJOR_VERSION}.0_${JAVA_UPDATE_VERSION}

# JAVA APT
RUN \
 apt-get update && \
 apt-get install -y software-properties-common && \
 echo oracle-java8-installer shared/accepted-oracle-license-v1-1 select true | debconf-set-selections && \
 add-apt-repository -y ppa:webupd8team/java && \
 apt-get update && \
 apt-get install -y oracle-java8-installer && \
 rm -rf /var/lib/apt/lists/* && \
 rm -rf /var/cache/oracle-jdk8-installer#


# SCALA
ARG SCALA_MAJOR_VERSION=2
ARG SCALA_UPDATE_VERSION=11
ARG SCALA_BUILD_NUMBER=8
ARG SBT_MAJOR_VERSION=0
ARG SBT_UPDATE_VERSION=13
ARG SBT_BUILD_NUMBER=15
ARG MAVEN_MAJOR_VERSION=3
ARG MAVEN_UPDATE_VERSION=3
ARG MAVEN_BUILD_NUMBER=9
ARG MAVEN_PATCH_NUMBER=4
RUN curl -L --retry 3 -o /tmp/scala.deb www.scala-lang.org/files/archive/scala-${SCALA_MAJOR_VERSION}.${SCALA_UPDATE_VERSION}.${SCALA_BUILD_NUMBER}.deb
RUN dpkg --force-all -i /tmp/scala.deb
RUN curl -L --retry 3 -o /tmp/sbt.deb http://dl.bintray.com/sbt/debian/sbt-0.13.15.deb
RUN dpkg --force-all -i /tmp/sbt.deb
RUN curl -L --retry 3 -o /tmp/maven.deb http://ftp.us.debian.org/debian/pool/main/m/maven/maven_${MAVEN_MAJOR_VERSION}.${MAVEN_UPDATE_VERSION}.${MAVEN_BUILD_NUMBER}-${MAVEN_PATCH_NUMBER}_all.deb
RUN dpkg --force-all -i /tmp/maven.deb

ARG APACHEMIRROR=https://archive.apache.org/dist

# ZOOKEEPER
ENV ZOOKEEPER_VERSION 3.4.12
ENV ZOOKEEPER_HOME /usr/zookeeper-$ZOOKEEPER_VERSION
ENV PATH $PATH:$ZOOKEEPER_HOME/bin
RUN curl -sL --retry 3 \
  "$APACHEMIRROR/zookeeper/zookeeper-$ZOOKEEPER_VERSION/zookeeper-$ZOOKEEPER_VERSION.tar.gz" \
  | gunzip \
  | tar -x -C /usr/ \
 && rm -rf $ZOOKEEPER_HOME/share/doc \
 && chown -R root:root $ZOOKEEPER_HOME

# HADOOP
ENV HADOOP_VERSION 2.8.3
ENV HADOOP_HOME /usr/hadoop-$HADOOP_VERSION
ENV HADOOP_CONF_DIR=$HADOOP_HOME/etc/hadoop
ENV PATH $PATH:$HADOOP_HOME/bin
RUN curl -sL --retry 3 \
  "$APACHEMIRROR/hadoop/common/hadoop-${HADOOP_VERSION}/hadoop-${HADOOP_VERSION}.tar.gz" \
  | gunzip \
  | tar -x -C /usr/ \
 && rm -rf $HADOOP_HOME/share/doc \
 && chown -R root:root $HADOOP_HOME

# SPARK
ENV SPARK_VERSION 2.1.2
ENV SPARK_PACKAGE spark-${SPARK_VERSION}-bin-without-hadoop
ENV SPARK_HOME /usr/spark-${SPARK_VERSION}
ENV SPARK_DIST_CLASSPATH="$HADOOP_HOME/etc/hadoop/*:$HADOOP_HOME/share/hadoop/common/lib/*:$HADOOP_HOME/share/hadoop/common/*:$HADOOP_HOME/share/hadoop/hdfs/*:$HADOOP_HOME/share/hadoop/hdfs/lib/*:$HADOOP_HOME/share/hadoop/hdfs/*:$HADOOP_HOME/share/hadoop/yarn/lib/*:$HADOOP_HOME/share/hadoop/yarn/*:$HADOOP_HOME/share/hadoop/mapreduce/lib/*:$HADOOP_HOME/share/hadoop/mapreduce/*:$HADOOP_HOME/share/hadoop/tools/lib/*"
ENV PATH $PATH:${SPARK_HOME}/bin
RUN curl -sL --retry 3 \
  "$APACHEMIRROR/spark/spark-${SPARK_VERSION}/${SPARK_PACKAGE}.tgz" \
  | gunzip \
  | tar x -C /usr/ \
 && mv /usr/$SPARK_PACKAGE $SPARK_HOME \
 && chown -R root:root $SPARK_HOME

# Zeppelin
ENV ZEPPELIN_PORT 8080
ENV ZEPPELIN_HOME /usr/zeppelin
ENV ZEPPELIN_CONF_DIR $ZEPPELIN_HOME/conf
ENV ZEPPELIN_NOTEBOOK_DIR $ZEPPELIN_HOME/notebook
ARG ZEPPELIN_MAJOR_VERSION=0
ARG ZEPPELIN_UPDATE_VERSION=8
ARG ZEPPELIN_BUILD_NUMBER=0
RUN curl -sL --retry 3 \
  "$APACHEMIRROR//zeppelin/zeppelin-${ZEPPELIN_MAJOR_VERSION}.${ZEPPELIN_UPDATE_VERSION}.${ZEPPELIN_BUILD_NUMBER}/zeppelin-${ZEPPELIN_MAJOR_VERSION}.${ZEPPELIN_UPDATE_VERSION}.${ZEPPELIN_BUILD_NUMBER}-bin-netinst.tgz" \
  | gunzip \
  | tar x -C /tmp/ \
 && mv /tmp/zeppelin* $ZEPPELIN_HOME \
 && mkdir -p $ZEPPELIN_HOME/logs \
 && mkdir -p $ZEPPELIN_HOME/run
RUN /usr/zeppelin/bin/install-interpreter.sh --name md,shell,jdbc,python

# KAFKA
ENV KAFKA_VERSION 1.0.0
ENV KAFKA_SCALA  2.11
ENV KAFKA_HOME /usr/kafka-${KAFKA_VERSION}
ENV KAFKA_PACKAGE kafka_${KAFKA_SCALA}-${KAFKA_VERSION}
ENV PATH $PATH:${KAFKA_HOME}/bin
RUN curl -sL --retry 3 \
  "$APACHEMIRROR//kafka/${KAFKA_VERSION}/${KAFKA_PACKAGE}.tgz" \
  | gunzip \
  | tar x -C /usr/ \
 && mv /usr/$KAFKA_PACKAGE $KAFKA_HOME \
 && chown -R root:root $KAFKA_HOME

# ArrangoDB
ARG ARRANGO_MAJOR_VERSION=3
ARG ARRANGO_UPDATE_VERSION=3
ARG ARRANGO_BUILD_NUMBER=9
ARG ARRANGO_PATCH_NUMBER=1
RUN curl -L --retry 3 -o /tmp/arangodb.deb https://download.arangodb.com/arangodb33/xUbuntu_16.04/amd64/arangodb${ARRANGO_MAJOR_VERSION}-${ARRANGO_MAJOR_VERSION}.${ARRANGO_UPDATE_VERSION}.${ARRANGO_BUILD_NUMBER}-${ARRANGO_PATCH_NUMBER}_amd64.deb
RUN (echo arangodb3 arangodb3/password password test | debconf-set-selections) && \
    (echo arangodb3 arangodb3/password_again password test | debconf-set-selections) && \
    DEBIAN_FRONTEND="noninteractive" dpkg -i /tmp/arangodb.deb && \
    rm -rf /var/lib/arangodb3/* && \
    sed -ri \
        -e 's!127\.0\.0\.1!0.0.0.0!g' \
        -e 's!^(file\s*=).*!\1 -!' \
        -e 's!^#\s*uid\s*=.*!uid = arangodb!' \
        -e 's!^#\s*gid\s*=.*!gid = arangodb!' \
        /etc/arangodb3/arangod.conf \
    && \
    rm -f /tmp/arangodb.deb*

# ElasticSearch
ARG ES_MAJOR_VERSION=6
ARG ES_UPDATE_VERSION=2
ARG ES_BUILD_NUMBER=4
ARG KIBANA_MAJOR_VERSION=6
ARG KIBANA_UPDATE_VERSION=2
ARG KIBANA_BUILD_NUMBER=4
RUN curl -L --retry 3 -o /tmp/es.deb https://artifacts.elastic.co/downloads/elasticsearch/elasticsearch-${ES_MAJOR_VERSION}.${ES_UPDATE_VERSION}.${ES_BUILD_NUMBER}.deb
RUN dpkg --force-all -i /tmp/es.deb
RUN curl -L --retry 3 -o /tmp/kibana.deb https://artifacts.elastic.co/downloads/kibana/kibana-${KIBANA_MAJOR_VERSION}.${KIBANA_UPDATE_VERSION}.${KIBANA_BUILD_NUMBER}-amd64.deb
RUN dpkg --force-all -i /tmp/kibana.deb

# Flink
ENV FLINK_VERSION 1.5.1
ENV FLINK_SCALA  2.11
ENV FLINK_PACKAGE flink-${FLINK_VERSION}
ENV FLINK_HOME /usr/flink-${FLINK_VERSION}
ENV PATH $PATH:${FLINK_VERSION}/bin
RUN curl -sL --retry 3 \
   "$APACHEMIRROR/flink/flink-${FLINK_VERSION}/${FLINK_PACKAGE}-bin-scala_${FLINK_SCALA}.tgz" \
  | gunzip \
  | tar x -C /tmp/ \
 && mv /tmp/flink* $FLINK_HOME \
 && chown -R root:root $FLINK_HOME

# Nifi
ENV NIFI_VERSION=1.7.0
ENV NIFI_HOME /usr/nifi-${NIFI_VERSION} 
ENV NIFI_BINARY_URL /nifi/${NIFI_VERSION}/nifi-${NIFI_VERSION}-bin.tar.gz
ENV NIFI_PID_DIR ${NIFI_HOME}/run
ENV NIFI_LOG_DIR ${NIFI_HOME}/logs
RUN curl -sL --retry 3 \
   "$APACHEMIRROR/$NIFI_BINARY_URL" \
  | gunzip \
  | tar x -C /tmp/ \
 && mv /tmp/nifi* $NIFI_HOME \
 && chown -R root:root $NIFI_HOME

# CLEANUP
RUN apt-get clean \
 && rm -rf /var/lib/apt/lists/*
RUN rm -rf /tmp/*
RUN rm -rf /usr/share/doc/*
RUN rm -rf usr/*.whl
RUN apt-get purge -f -y --auto-remove

# Config 
RUN echo "network.host: 0.0.0.0" >> /etc/elasticsearch/elasticsearch.yml
RUN echo "server.host: 0.0.0.0" >> /etc/kibana/kibana.yml
RUN echo "\nnifi.web.http.port=9090" >> /usr/nifi-1.7.0/conf/nifi.properties
RUN cp   /usr/zeppelin/conf/zeppelin-site.xml.template /usr/zeppelin/conf/zeppelin-site.xml
RUN echo "service arangodb3 start\n" >> /service.sh
RUN echo "service elasticsearch start\n" >> /service.sh
RUN echo "service kibana start\n" >> /service.sh
RUN echo "/usr/flink-1.5.1/bin/taskmanager.sh start\n" >> /service.sh
RUN echo "/usr/flink-1.5.1/bin/jobmanager.sh start\n" >> /service.sh
RUN echo "/usr/nifi-1.7.0/bin/nifi.sh start\n" >> /service.sh
RUN echo "/usr/kafka-1.0.0/bin/zookeeper-server-start.sh /usr/kafka-1.0.0/config/zookeeper.properties &" >> /service.sh
RUN echo "/usr/kafka-1.0.0/bin/kafka-server-start.sh /usr/kafka-1.0.0/config/server.properties &" >> /service.sh
RUN echo "/usr/zeppelin/bin/zeppelin-daemon.sh start\n" >> /service.sh
RUN echo "/run_jupyter.sh --allow-root \n" >> /service.sh
RUN chmod +x /service.sh

# TensorBoard
EXPOSE 6006
# IPython
EXPOSE 8888
# Zeppelin
EXPOSE 8080
# ArangoDB
EXPOSE 8529
# Elasticsearch
EXPOSE 9200
# Kibana
EXPOSE 5601
# Zookeeper
EXPOSE 2181
#Flink
EXPOSE 8081 6123
#Nifi 
EXPOSE 9090 8443 10000

WORKDIR "/notebooks"

CMD ["/service.sh",""]
