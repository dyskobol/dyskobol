FROM debian
ENV SCALA_VERSION 2.12.6 
ENV SBT_VERSION 1.1.6
# Update repository sources list
RUN apt-get update 

# Install Debian Tools
RUN apt-get install -y git build-essential autotools-dev autoconf automake libtool default-jdk curl ant cmake
RUN touch /usr/lib/jvm/java-8-openjdk-amd64/release

RUN \
  curl -fsL https://downloads.typesafe.com/scala/$SCALA_VERSION/scala-$SCALA_VERSION.tgz | tar xfz - -C /root/ && \
  echo >> /root/.bashrc && \
  echo "export PATH=~/scala-$SCALA_VERSION/bin	:$PATH" >> /root/.bashrc
RUN \
  curl -L -o sbt-$SBT_VERSION.deb https://dl.bintray.com/sbt/debian/sbt-$SBT_VERSION.deb && \
  dpkg -i sbt-$SBT_VERSION.deb && \
  rm sbt-$SBT_VERSION.deb && \
  apt-get update && \
  apt-get install sbt && \
  sbt sbtVersion


# Download The Sleuthkit from GIT
RUN git clone https://github.com/sleuthkit/sleuthkit.git

# Build and install The Sleuthkit
RUN cd /sleuthkit && ./bootstrap && ./configure && make && make install
RUN git clone https://github.com/dyskobol/dyskobol.git && cd dyskobol && git fetch && git checkout docker-test && git pull &&  sbt packageBin

WORKDIR /dyskobol
ENV LD_LIBRARY_PATH /usr/local/lib

CMD ["sbt", "core/run"]

