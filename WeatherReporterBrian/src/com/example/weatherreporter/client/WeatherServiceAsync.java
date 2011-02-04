package com.example.weatherreporter.client;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * The async counterpart of <code>GreetingService</code>.
 */
public interface WeatherServiceAsync
{
	void greetServer(String input, AsyncCallback<String> callback);
	void getWeatherHtml(String zip, boolean isCelsius, AsyncCallback <String> callback);
}
