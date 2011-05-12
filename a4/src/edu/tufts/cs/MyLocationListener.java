package edu.tufts.cs;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.json.JSONArray;
import org.json.JSONObject;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;

public class MyLocationListener implements LocationListener {
	private StationFinder copyOfStationFinder;
	private static final String PREFIX = "http://mbta-api.heroku.com/mapper/find_closest_stations.json?lat=";
	private static final String MIDDLE = "&lon=";

	public MyLocationListener(StationFinder f) {
		copyOfStationFinder = f;
	}

	@Override
	public void onLocationChanged(Location arg0) {

		String stationJSON   =  getContent(PREFIX + arg0.getLatitude() + MIDDLE + arg0.getLongitude());
		String stationString = "There is no MBTA subway station within 5 miles of where you are.";
		String trainString   = "";
		String line = "NO LINE";
		String direction = "NO DIRECTION";	
		String trainJSON = "";
		
		copyOfStationFinder.tv.setText("Charlie says: \"Loading station and train data!\"");

		try {
			// Attempt to find station within 5 miles of the location given by arg0.
			// If there is no such station, we return a string stating as much.
			JSONArray stationArray = new JSONArray(stationJSON);
			if(!stationArray.isNull(0)) {
				JSONObject stationObject = stationArray.getJSONObject(0).getJSONObject("station");
				stationString = "The closest MBTA subway station to you is " + stationObject.getString("station_name") +
								" which is approximately " + stationObject.getString("distance") + " miles away from your location.\n\n";
				
				trainString = "There is no information on upcoming trains in this station.";
				
				// Attempt to find all trains' predicted arrivals at the closest station.
				trainJSON = getContent("http://mbtamap.heroku.com/mapper/station_schedule.json?id=" + stationObject.getString("stop_id"));
				JSONArray trainArray = new JSONArray(trainJSON);
				
				if (trainString.length() != 0) trainString = "";
				
				for (int i = 0; i < trainArray.length(); i++) {
					Log.d("place", "trainloop");
					JSONObject trainObject = trainArray.getJSONObject(i);
					Log.d("jsonobject", trainObject.toString());
					
					if (trainObject.getString("information_type").equals("Predicted")) {
						Log.d("predicted", "yes");
						String key = trainObject.getString("platform_key");
						
						Log.d("key", key);
						switch (key.charAt(0)) {
							case 'R': line = "RED"; break;
							case 'B': line = "BLUE"; break;
							case 'G': line = "GREEN"; break;
							case 'O': line = "ORANGE"; break;
						}
						
						switch (key.charAt(key.length() - 1)) {
							case 'S': direction ="SOUTHBOUND"; break;
							case 'N': direction ="NORTHBOUND"; break;
							case 'E': direction ="EASTBOUND"; break;
							case 'W': direction ="WESTBOUND"; break;
						}
						
						trainString = trainString.concat("\nThe next " + line + " Line train to " + trainObject.getString("stop_name").toUpperCase() +
										   " heading " + direction + " is predicted to arrive at " + trainObject.getString("time") + "; time remaining "+ 
										   trainObject.getString("time_remaining"));
					}
				}
				
			}

			String newDispLabel = stationString.concat("\n").concat(trainString);

			// Set string in text view to the new lat, lng pair
			copyOfStationFinder.tv.setText(newDispLabel);
			Log.d("trainjson", trainJSON);

		} catch (Exception e) {
			Log.d("exception", e.toString());
			Log.d(trainString, trainString);
			copyOfStationFinder.tv.setText("Noo! An exception!");
		}

	}

	private String getContent(String url) {
		try {
			URL api = new URL(url);
			HttpURLConnection conn = (HttpURLConnection) api.openConnection();
			InputStream is = conn.getInputStream();
			BufferedReader r = new BufferedReader(new InputStreamReader(is));
			StringBuilder total = new StringBuilder();
			String line;

			while ((line = r.readLine()) != null) {
				total.append(line);
			}

			r.close();
			is.close();
			return total.toString();
		} catch (MalformedURLException e) {
			return null;
		} catch (IOException e) {
			return null;
		}
	}

	@Override
	public void onProviderDisabled(String arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onProviderEnabled(String arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
		// TODO Auto-generated method stub

	}
}
