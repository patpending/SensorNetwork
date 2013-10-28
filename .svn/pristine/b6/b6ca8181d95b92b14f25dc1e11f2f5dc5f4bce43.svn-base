/**
 * Sensor network for xbee/zigbee/xrf.
 */
package org.dongargon.sensornetwork;

import java.util.HashMap;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.dongargon.sensornetwork.xbee.XbeeCommunicator;
import org.dongargon.sensornetwork.xrf.XRFCommunicator;
import org.dongargon.util.WirelessSensorConfig;
import org.dongargon.util.WirelessSensorConfigReader;


public class SensorNetworkHeadless {

	private static String comport;
	private static String battFileDir;
	static String tempFileDirectory;
	private static String sensorConfiFile;
	static boolean debugMode;
	private static String sensorType;
	HashMap<String, WirelessSensorConfig> config;

	private static Logger logger = Logger.getLogger(SensorNetworkHeadless.class);
	
	public static boolean isDebugMode() {
		return debugMode;
	}

	/**
	 * default constructor, kicks off the processing thread
	 */
	public SensorNetworkHeadless() {
		try {
			initialize();
			if(sensorType.equals("XRF"))
			{
				logger.info("Sensor type is XRF");
				(new Thread(new XRFCommunicator(config,tempFileDirectory,battFileDir,debugMode,comport))).start();
			}
			else if (sensorType.equals("XBEE"))
			{
				logger.info("Sensor type is XBEE");
				(new Thread(new XbeeCommunicator(config,tempFileDirectory,battFileDir,debugMode,comport))).start();
			}
			else
			{
				logger.error("Unknown sensor type: "+sensorType);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	

	/**
	 * Entry point for the application.
	 */
	public static void main(String[] args) {
		if (args.length < 6) {
			System.out.println("usage: [comport] [tempfile directory] [batt file directory] [config file] [debugMode true/false] [XRF/XBEE]");
			System.exit(-1);
		}
		comport = args[0];
		tempFileDirectory = args[1];
		battFileDir = args[2];
		sensorConfiFile = args[3];
		debugMode = Boolean.parseBoolean(args[4]);
		sensorType = args[5];

		SensorNetworkHeadless xrf = new SensorNetworkHeadless();

	}

	public void initialize() throws Exception {
		logger.debug("initializing");
		// read in the wireless sensor config file
		readConfig();
	

	}

	
	private void readConfig() throws Exception {

		WirelessSensorConfigReader reader = new WirelessSensorConfigReader(sensorConfiFile);
		config = reader.getConfig();
		Iterator<String> i = config.keySet().iterator();
		while (i.hasNext()) {
			WirelessSensorConfig conf = (WirelessSensorConfig) config.get(i.next());
			logger.debug(conf);
		}
	}

}