package org.dongargon.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.StringTokenizer;

/**
 * Little helper util that reads a config file and returns the contents as a HashMap
 * @author patrickstrobel
 *
 */
public class WirelessSensorConfigReader {
	
	private String filename;
	private HashMap<String,WirelessSensorConfig> config;
	public WirelessSensorConfigReader(String filename) throws Exception
	{
		this.filename = filename;
		initialize();
	}

	private void initialize() throws Exception{
		config = new HashMap<String,WirelessSensorConfig>();
		BufferedReader in = new BufferedReader(new FileReader(filename));
		String temp = in.readLine();//skip the first line as it contains the headers
		temp = in.readLine();
		StringTokenizer tok;
		while(null != temp){
			
			tok = new StringTokenizer(temp,",");
			WirelessSensorConfig conf = new WirelessSensorConfig(tok.nextToken(), tok.nextToken(), tok.nextToken());
			if(tok.hasMoreTokens())
			{
				conf.setCosmFeed(Integer.parseInt(tok.nextToken()));
				conf.setCosmDataSet(Integer.parseInt(tok.nextToken()));
			}
			config.put(conf.getID(), conf);
			temp = in.readLine();
		}
	}

	public HashMap<String,WirelessSensorConfig> getConfig() {
		return config;
	}

}
