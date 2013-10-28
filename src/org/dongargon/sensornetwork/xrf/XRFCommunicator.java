package org.dongargon.sensornetwork.xrf;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;

import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.dongargon.sensornetwork.BatteryData;
import org.dongargon.sensornetwork.SensorData;
import org.dongargon.sensornetwork.SensorNetworkHeadless;
import org.dongargon.sensornetwork.SwitchEvent;
import org.dongargon.sensornetwork.TempData;
import org.dongargon.util.CosmSender;
import org.dongargon.util.DataWriter;
import org.dongargon.util.WirelessSensorConfig;

/**
 * The processing thread
 * */
public class XRFCommunicator implements Runnable {

	InputStream in;
	OutputStream out;
	boolean debugMode;
	private HashMap<String, WirelessSensorConfig> config;
	private String battFileDirectory;
	private String tempFileDirectory;
	private String portName;
	private static Logger logger = Logger
			.getLogger(SensorNetworkHeadless.class);

	public XRFCommunicator(HashMap<String, WirelessSensorConfig> config,
			String tempFileDirectory, String battFileDirectory,
			boolean debugMode, String portName) throws Exception {

		this.debugMode = debugMode;
		this.config = config;

		this.battFileDirectory = battFileDirectory;
		this.tempFileDirectory = tempFileDirectory;
		this.portName = portName;
	}

	public void run() {
		try {
			if(!debugMode)
			{
				connect(portName);
			}
			while (true) {
				
				SensorData data=null;
				try {
					data = readXRFSensorData();
				} catch (Exception e) {
					logger.error("could not read or parse data. Carry on.... HACK!",e);
					//@TODO refactor the reading code. Something wrong with the framing
					Thread.sleep(10000);
				}
				if (null != data) {
					DataWriter.getInstance(config, tempFileDirectory,
							battFileDirectory).writeDataToFile(data);
					WirelessSensorConfig cfg = config.get(data.getSensorID());
					if (null != cfg) {
						try {
							CosmSender.sendToCosm(cfg, data);
						} catch (Exception e) {

							logger.error(
									"couldnt send data to pachube. Probably a nework problem. Carry on...",
									e);
						}
					} else {
						logger.debug("No confg data found for this sensor. Not sending anything to Cosm");
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
		System.setProperty("gnu.io.rxtx.SerialPorts", portName);
		CommPortIdentifier portIdentifier = CommPortIdentifier
				.getPortIdentifier(portName);
		if (portIdentifier.isCurrentlyOwned()) {
			logger.fatal("Error: Port is currently in use");
		} else {
			CommPort commPort = portIdentifier.open(this.getClass().getName(),
					2000);

			if (commPort instanceof SerialPort) {
				SerialPort serialPort = (SerialPort) commPort;
				serialPort.setSerialPortParams(9600, SerialPort.DATABITS_8,
						SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);

				in = serialPort.getInputStream();
				out = serialPort.getOutputStream();

			} else {
				logger.fatal("Error: Only serial ports are handled.");
			}
		}
	}

	/**
	 * reads the serial data from the socket and calls a method to get it parsed
	 * fakeTemp
	 * 
	 * @return
	 * @throws Exception
	 */
	private SensorData readXRFSensorData() throws Exception {
		if (debugMode) {
			double fakeTemp = Math.random() + 20;
			String fakeData = new String("aDDGARAGEBOF");
			Thread.sleep(5000);
			return parseXRFData(fakeData);
		} else {
			byte[] buffer = new byte[12];
			int len = -1;

			if ((len = this.in.read(buffer)) > -1) {
				Thread.sleep(500);// pop in a little sleep to allow the XRF
									// data
									// to complete coming in
				String data = new String(buffer, 0, len);

				return parseXRFData(data);
			}
			return null;
		}
	}

	/**
	 * Method parses the serial data passed in via the argument and returns an
	 * XRFData object representing the reading
	 * 
	 * @param rawData
	 * @return
	 * @throws Exception
	 */
	private SensorData parseXRFData(String rawData) throws Exception {

		SensorData data = null;
		// have we actually got real data?
		if (rawData.length() > 1) {
			logger.debug("rawdata:" + rawData);
			// is it a temp reading?
			int start = rawData.indexOf("TMPA");
			if (start > -1) {
				start += 4;
				// data will be in the format AATEMPA2.9. Chop out the
				// sensor ID which is the 'AA' bit
				String id = rawData.substring(start - 6, start - 4);
				double temp = Double.parseDouble(rawData.substring(start,
						start + 5));
				data = new TempData(temp, id);
			}
			// its a switch off event
			start = rawData.indexOf("OF");
			if (start > -1) {
				
				// data will be in the format AAGARAGEBON. Chop out the
				// sensor ID which is the 'AA' bit
				String id = rawData.substring(start - 9, start - 7);
				
				data = new SwitchEvent(false, id);
			}
			start = rawData.indexOf("ON");
			if (start > -1) {
				
				// data will be in the format AAGARAGEBON. Chop out the
				// sensor ID which is the 'AA' bit
				String id = rawData.substring(start - 9, start - 7);
				
				data = new SwitchEvent(true, id);
			}
			// if its a battery reading
			start = rawData.indexOf("BATT");
			if (start > -1) {
				start += 4;
				// data will be in the format AABATTA2.9. Chop out the
				// sensor ID which is the 'AA' bit
				String id = rawData.substring(start - 6, start - 4);

				double batt = Double.parseDouble(rawData.substring(start,start + 4));
				data = new BatteryData(batt, id);
			}
		}
		if (null != data) {
			logger.debug(data);
		}

		return data;
	}
}