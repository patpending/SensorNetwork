package org.dongargon.util;

import java.math.BigDecimal;
import java.text.DecimalFormat;

import org.apache.log4j.Logger;
import org.dongargon.sensornetwork.BatteryData;
import org.dongargon.sensornetwork.SensorData;
import org.dongargon.sensornetwork.SensorNetworkHeadless;

import Pachube.Data;
import Pachube.Feed;
import Pachube.Pachube;
import Pachube.PachubeException;
import Pachube.httpClient.HttpResponse;

public class CosmSender {
	private static final String API_KEY = "myXcpBVW3z73UsS9eVg8tn2BoceSAKxSYVVTdkZHdHdobz0g";
	private static final int FEED_ID = 101093;
	private static Logger logger = Logger
			.getLogger(SensorNetworkHeadless.class);

	public static void main(String[] args) {
		try {

			// Feed feed = createFeed();
			// printFeedForId(feed.getId());
			addSeries(FEED_ID, "friendlyName", -30.0, 5);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static Feed addSeries(int feed, String friendlyName,
			double minValue, double maxValue) throws Exception {
		// Creates a Pachube object authenicated using the provided API KEY

		Pachube p = new Pachube(API_KEY);
		Feed f = p.getFeed(feed);
		Data a = new Data();
		a.setId(3);
		a.setMaxValue(maxValue);
		a.setMinValue(minValue);
		a.setTag(friendlyName);
		a.setValue(0);
		f.addData(a);
		Feed g = p.createFeed(f);

		// The Feed 'f' is does not represent the feed on pachube any
		// Changes made to this object will not alter the online feed.
		logger.debug("The id of the new feed is:");
		logger.debug(g.getId());

		return g;
	}

	private static Feed createFeed(String friendlyName, double minValue,
			double maxValue) throws Exception {
		// Creates a Pachube object authenicated using the provided API KEY

		Pachube p = new Pachube(API_KEY);
		Feed f = new Feed();
		f.setTitle("Test Feed from JPachube");
		Data a = new Data();
		a.setId(1);
		a.setMaxValue(maxValue);
		a.setMinValue(minValue);
		a.setTag(friendlyName);
		a.setValue(0);
		f.addData(a);
		Feed g = p.createFeed(f);

		// The Feed 'f' is does not represent the feed on pachube any
		// Changes made to this object will not alter the online feed.
		logger.debug("The id of the new feed is:");
		logger.debug(g.getId());

		return g;
	}

	public static void sendToCosm(WirelessSensorConfig config, SensorData data)
			throws Exception {
		// if its a temperature reading
		if (!(data instanceof BatteryData)) {
			updateFeed(config.getCosmFeed(), config.getCosmDataSet(),data.getReading());
		}
	}

	private static void updateFeed(int feed, int series, double value)
			throws Exception {

		Pachube p = new Pachube(API_KEY);
		// HACK ALERT!!!
		Feed f = p.getFeed(feed);
		DecimalFormat format = new DecimalFormat();
		format.setMaximumFractionDigits(1);
		// stinks so bad..
		double roundedValue = Double.parseDouble(format.format(value));
		f.updateDatastream(series, roundedValue);

	}

	static void printFeedForId(int feedId) {
		Pachube pachubeClient = new Pachube(API_KEY);

		try {
			Feed feed = pachubeClient.getFeed(feedId);

			HttpResponse data = pachubeClient.getDatastream(feed.getId(), 1);

			System.out.println(data.getBody());
		} catch (PachubeException e) {
			System.err.println(e.getMessage());
		}
	}
}
