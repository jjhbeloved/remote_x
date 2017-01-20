#export JAVA_HOME=/data/adi/jdk1.8.0_45
#export PATH=$JAVA_HOME/bin:$PATH
rundir=`dirname "$0"`
cd $rundir
nohup java -Xmx128M -Dvertx.logger-delegate-factory-class-name=io.vertx.core.logging.SLF4JLogDelegateFactory \
-Dhmcfg=./hm.json -Dvertx.cwd=./ -DAPP_NAME=vx \
-Dvertx.disableFileCaching=true -Dvertx.disableFileCPResolving=true -Dpwd=`pwd` \
-Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.port=23334 -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.authenticate=false -Djava.rmi.server.hostname=0.0.0.0 \
-cp ./:remote_x-fat.jar \
cd.blog.humbird.vertx.hm.Proxy &