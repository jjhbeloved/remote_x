#export JAVA_HOME=/data/adi/jdk1.8.0_45
#export PATH=$JAVA_HOME/bin:$PATH
rundir=`dirname "$0"`
cd $rundir
nohup java -Xmx128M -Dvertx.logger-delegate-factory-class-name=io.vertx.core.logging.SLF4JLogDelegateFactory \
-Dproxycfg=./proxy.json -Dvertx.cwd=./ -DAPP_NAME=server \
-Dvertx.disableFileCaching=true -Dvertx.disableFileCPResolving=true -Dpwd=`pwd` \
-cp ./:remote_x-fat.jar \
cd.blog.humbird.vertx.vx.VxChannelServer &