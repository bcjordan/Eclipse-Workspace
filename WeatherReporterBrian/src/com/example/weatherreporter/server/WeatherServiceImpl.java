package com.example.weatherreporter.server;

import com.example.weatherreporter.client.WeatherService;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import java.io.IOException;
import java.io.InputStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 * The server side implementation of the RPC service.
 */
@SuppressWarnings("serial")
public class WeatherServiceImpl extends RemoteServiceServlet implements WeatherService
{
	private static final String WEATHER_URL = "http://xml.weather.yahoo.com/forecastrss";
	private static DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
	
	public String greetServer(String input)
	{
		String serverInfo = getServletContext().getServerInfo();
		String userAgent = getThreadLocalRequest().getHeader("User-Agent");
		return "I am running " + serverInfo + ".<br><br>It looks like you are using:<br>" + userAgent;
	}

	public String getWeatherHtml(String zip, boolean isCelsius)
	{
		Document rss;
		String result = "";

		// I only want to get the description (element) which is inside the item element
		try {
			rss = getWeatherRssDocument(zip, isCelsius);
			Element title = (Element)rss.getElementsByTagName("title").item(0);
			if (title != null) {
				result = "<h2>" + title.getFirstChild().getNodeValue() + "</h2>";
			}
			else {
				result = "<h2>Unknown City Entered</h2>";
			}
			Element item = (Element)rss.getElementsByTagName("item").item(0);
			Element desc = (Element)item.getElementsByTagName("description").item(0);
			return result + desc.getFirstChild().getNodeValue();
		}
		catch (Exception e) {
			return e.toString();
		}	    
	}
	
	private Document getWeatherRssDocument (String zip, boolean isCelsius) throws IOException, ParserConfigurationException, SAXException
	{
		String url = WEATHER_URL;
		if (zip.matches("^[0-9]{5}$")) {
			url = url + "?p=" + zip + "&u=" + (isCelsius?"c":"f");
		}
		
		// See http://hc.apache.org/httpcomponents-client-ga/httpclient/apidocs/org/apache/http/client/package-summary.html
		HttpClient client = new DefaultHttpClient();
		HttpGet get = new HttpGet(url);
		try {
			HttpResponse response = client.execute(get);
			HttpEntity entity = response.getEntity();
			if (response.getStatusLine().getStatusCode() == 200) {
				InputStream input = entity.getContent();
				DocumentBuilder builder = builderFactory.newDocumentBuilder();
				return builder.parse(input);
			}
			else {
				throw new IOException("HTTP Communication problem, response code: " + response.getStatusLine());
			}
		}
		finally {
		}
	}
}
