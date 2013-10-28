java -verbose:gc -Xmx6m -classpath ./bin -Djava.ext.dirs=./lib:. org.dongargon.test.xrf.XRFTempSensorHeadless /dev/ttyAMA0 /var/www/cc/data/Outside.csv batt.txt > xrf.log 2>&1&
