java -verbose:gc -Xmx6m -classpath ./bin:./conf -Djava.ext.dirs=./lib:. org.dongargon.sensornetwork.SensorNetworkHeadless /dev/ttyAMA0 /var/www/cc/data/temp batt.txt ./conf/example_conf.csv false XRF > xrf.log 2>&1&