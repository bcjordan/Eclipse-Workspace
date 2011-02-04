package com.example.weatherreporter.client;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * The client side stub for the RPC service.
 */
@RemoteServiceRelativePath("weather") // This is absolutely necessary
public interface WeatherService extends RemoteService
{
	public String greetServer (String name); // You have to implement this now
	public String getWeatherHtml (String zip, boolean isCelsius);
}
