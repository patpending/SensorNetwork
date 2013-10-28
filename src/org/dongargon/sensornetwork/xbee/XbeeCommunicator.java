package org.dongargon.sensornetwork.xbee;


import java.text.SimpleDateFormat;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.dongargon.sensornetwork.SensorData;
import org.dongargon.sensornetwork.SensorNetworkHeadless;
import org.dongargon.sensornetwork.TempData;
import org.dongargon.util.CosmSender;
import org.dongargon.util.DataWriter;
import org.dongargon.util.WirelessSensorConfig;

import com.rapplogic.xbee.api.ApiId;
import com.rapplogic.xbee.api.XBee;
import com.rapplogic.xbee.api.XBeeResponse;
import com.rapplogic.xbee.api.zigbee.ZNetRxIoSampleResponse;

/**
 * The processing thread
 * */
public class XbeeCommunicator implements Runnable {

	boolean debugMode;
	private HashMap<String, WirelessSensorConfig> config;
	private SimpleDateFormat df;
	private String battFileDirectory;
	private String tempFileDirectory;
	private String portName;
	private XBee xbee = new XBee();
	private static final int BAUD_RATE = 9600;
	private static Logger logger = Logger.getLogger(SensorNetworkHeadless.class);
	public XbeeCommunicator(HashMap<String, WirelessSensorConfig> config, String tempFileDirectory, String battFileDirectory, boolean debugMode, String portName) throws Exception {

		this.debugMode = debugMode;
		this.config = config;
		this.battFileDirectory = battFileDirectory;
		this.tempFileDirectory = tempFileDirectory;
		this.portName = portName;
	}

	public void run() {
		try {
			connect(portName);
			while (true) {
				SensorData data = readXbeeSensorData();
				if (null != data) {
					DataWriter.getInstance(config, tempFileDirectory, battFileDirectory).writeDataToFile(data);
					WirelessSensorConfig cfg = config.get(data.getSensorID());
					if(null != cfg)
					{
						CosmSender.sendToCosm(cfg,data);
					}
					else
					{
						logger.info("No confg data found for sensor ["+data.getSensorID()+"] Not sending anything to Cosm");
					}
				}
				Thread.sleep(1000);

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Connect to the Com port
	 * 
	 * @param portName
	 * @throws Exception
	 */
	void connect(String portName) throws Exception {
		// replace with port and baud rate of your XBee
		xbee.open(portName, BAUD_RATE);
		logger.info("connected to xbee on port:" + portName);
	}

	/**
	 * reads the serial data from the socket and calls a method to get it parsed
	 * 
	 * @return
	 * @throws Exception
	 */
	private SensorData readXbeeSensorData() throws Exception {
		if (debugMode) {
			double fakeTemp = Math.random() + 20;
			return new TempData(fakeTemp, "A8600007755");
		} else {
			XBeeResponse response = xbee.getResponse();
			// is this a sample we have got back from the XBEE or something else
			if ((response.getApiId() == ApiId.ZNET_IO_SAMPLE_RESPONSE) && !response.isError()) {
				// get the sample data
				ZNetRxIoSampleResponse ioSample = (ZNetRxIoSampleResponse) response;

				// get the sample reading from the XBEE pin in milivolts
				double mv = ioSample.getAnalog0().doubleValue();

				/**
				 * convert it to a temperature value. The LM335 uses 10mv per
				 * Kelvin. 273.15 Kelvin is 0 centigrade The xbee analog pin is
				 * 10bit (i think) giving 1023 possible voltage readings. The
				 * xbee analog pin range is from 0-1.2v by default. This is the
				 * basis of the below calculation.
				 * http://www.ti.com/lit/ds/symlink/lm335.pdf
				 */

				double temp = (mv / 1023.0 * 1.2 * 3.0 * 100) - 273.15;

				logger.debug("Reading received:"+ Math.round(temp));

				SensorData data = new TempData(temp, ioSample.getRemoteAddress64().toString().replace(',','_'));

				logger.debug(data);
				return data;
			}

		}
		return null;
	}

}