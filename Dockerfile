FROM tensorflow/tensorflow:1.8.0-py3
MAINTAINER Zhizhou Li <lizhizhou1983@gmail.com>
RUN apt-get update

# PYTHON
RUN pip3 install pandas

# DEEPLEANING
RUN pip3 install keras
RUN pip3 install tensorlayer

# JAVA
ARG JAVA_MAJOR_VERSION=8
ARG JAVA_UPDATE_VERSION=131
ARG JAVA_BUILD_NUMBER=11
ENV JAVA_HOME /usr/jdk1.${JAVA_MAJOR_VERSION}.0_${JAVA_UPDATE_VERSION}

ENV PATH $PATH:$JAVA_HOME/bin
RUN curl -sL --retry 3 --insecure \
  --header "Cookie: oraclelicense=accept-securebackup-cookie;" \
  "http://download.oracle.com/otn-pub/java/jdk/${JAVA_MAJOR_VERSION}u${JAVA_UPDATE_VERSION}-b${JAVA_BUILD_NUMBER}/d54c1d3a095b4ff2b6607d096fa80163/server-jre-${JAVA_MAJOR_VERSION}u${JAVA_UPDATE_VERSION}-linux-x64.tar.gz" \
  | gunzip \
  | tar x -C /usr/ \
  && ln -s $JAVA_HOME /usr/java \
  && rm -rf $JAVA_HOME/man

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
RUN curl -L -o /tmp/scala.deb www.scala-lang.org/files/archive/scala-${SCALA_MAJOR_VERSION}.${SCALA_UPDATE_VERSION}.${SCALA_BUILD_NUMBER}.deb
RUN dpkg --force-all -i /tmp/scala.deb
RUN curl -L -o sbt.deb http://dl.bintray.com/sbt/debian/sbt-0.13.15.deb
RUN dpkg --force-all -i /tmp/sbt.deb
RUN curl -L -o maven.deb http://ftp.us.debian.org/debian/pool/main/m/maven/maven_${MAVEN_MAJOR_VERSION}.${{MAVEN_UPDATE_VERSION}.${{MAVEN_BUILD_NUMBER}.-{MAVEN_PATCH_NUMBER}_all.deb
RUN dpkg --force-all -i /tmp/maven.deb

# HADOOP
ENV HADOOP_VERSION 2.8.3
ENV HADOOP_HOME /usr/hadoop-$HADOOP_VERSION
ENV HADOOP_CONF_DIR=$HADOOP_HOME/etc/hadoop
ENV PATH $PATH:$HADOOP_HOME/bin
RUN curl -sL --retry 3 \
  "http://archive.apache.org/dist/hadoop/common/hadoop-$HADOOP_VERSION/hadoop-$HADOOP_VERSION.tar.gz" \
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
  "https://www.apache.org/dyn/mirrors/mirrors.cgi?action=download&filename=spark/spark-${SPARK_VERSION}/${SPARK_PACKAGE}.tgz" \
  | gunzip \
  | tar x -C /usr/ \
 && mv /usr/$SPARK_PACKAGE $SPARK_HOME \
 && chown -R root:root $SPARK_HOME

# Zeppelin
ENV ZEPPELIN_PORT 8080
ENV ZEPPELIN_HOME /usr/zeppelin
ENV ZEPPELIN_CONF_DIR $ZEPPELIN_HOME/conf
ENV ZEPPELIN_NOTEBOOK_DIR $ZEPPELIN_HOME/notebook
ARG SBT_MAJOR_VERSION=0
ARG SBT_UPDATE_VERSION=7
ARG SBT_BUILD_NUMBER=3
RUN curl -sL --retry 3 \
  "http://mirror.bit.edu.cn/apache/zeppelin/zeppelin-${ZEPPELIN_MAJOR_VERSION}.${{ZEPPELIN_UPDATE_VERSION}.${{ZEPPELIN_BUILD_NUMBER}/zeppelin-${ZEPPELIN_MAJOR_VERSION}.${{ZEPPELIN_UPDATE_VERSION}.${{ZEPPELIN_BUILD_NUMBER}-bin-netinst.tgz" \
  | gunzip \
  | tar x -C /usr/ \
 && mv /usr/zeppelin* $ZEPPELIN_HOME \
 && mkdir -p $ZEPPELIN_HOME/logs \
 && mkdir -p $ZEPPELIN_HOME/run


#CLEANUP
RUN apt-get clean \
 && rm -rf /var/lib/apt/lists/*
RUN rm -rf /tmp/*
RUN rm -rf /usr/share/doc/*
RUN rm -rf usr/*.whl
RUN apt-get purge -y --auto-remove $buildDeps

# TensorBoard
EXPOSE 6006
# IPython
EXPOSE 8888

WORKDIR "/notebooks"

CMD ["/run_jupyter.sh", "--allow-root"]