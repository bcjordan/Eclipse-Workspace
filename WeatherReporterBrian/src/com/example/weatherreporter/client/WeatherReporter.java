package com.example.weatherreporter.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.*;
import com.google.gwt.user.client.ui.*;

public class WeatherReporter implements EntryPoint
{
	private HorizontalPanel inputPanel;
	private TextBox zipcode;
	private RadioButton ucRadio;
	private RadioButton ufRadio;
	private Button submit;
	private HTML weatherHTML;
	private final WeatherServiceAsync ws = GWT.create(WeatherService.class); // Making the RPC

	public void onModuleLoad()
	{
		inputPanel = new HorizontalPanel();
		inputPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		inputPanel.setStyleName("weather-input-panel"); // See CSS under "war" folder
		
		// Create textbox for zipcode
		Label lbl = new Label("Enter zipcode (5-digits): ");
		inputPanel.add(lbl);
		zipcode = new TextBox();
		zipcode.addKeyPressHandler(new KeyPressHandler() {
			public void onKeyPress (KeyPressEvent event)
			{
				if (event.getCharCode() == KeyCodes.KEY_ENTER) {
					validateAndSubmit();
				}
			}
		});
		zipcode.setVisibleLength(10);
		inputPanel.add(zipcode);
		
		// Create radio button group to select units in C or F
		Panel radioPanel = new VerticalPanel();
		ucRadio = new RadioButton("units", "Celsius");
		ufRadio = new RadioButton("units", "Fahrenheit");
		ucRadio.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				validateAndSubmit();
			}
		});
		ufRadio.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				validateAndSubmit();
			}
		});
		// Default to Celsius
		ufRadio.setValue(true);
		radioPanel.add(ucRadio);
		radioPanel.add(ufRadio);

		// Add radio buttons panel to inputs
		inputPanel.add(radioPanel);

		// Create Submit button
		submit = new Button("Submit");
		submit.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				validateAndSubmit();
			}
		});
		
		// Add button to inputs
		inputPanel.add(submit);
		inputPanel.setCellVerticalAlignment(submit, HasVerticalAlignment.ALIGN_MIDDLE);

		// Add the input panel to the page
		RootPanel.get("input").add(inputPanel); // input is the div
		
		// Create widget for HTML output
		weatherHTML = new HTML();
		weatherHTML.setVisible(false);
		RootPanel.get("output").add(weatherHTML);
	}
	
	private void validateAndSubmit()
	{
		String zip = zipcode.getText().trim();
		if (!zip.matches("^[0-9]{5}$")) {
			Window.alert("Invalid zipcode entered.  Must be 5-digits long.");
			zipcode.setText("");
			return;
		}
		else {		
			// Get choice of celsius / fahrenheit
			boolean celsius = ucRadio.getValue();
			fetchWeatherHtml(zip, celsius);
		}
	}
	
	private void fetchWeatherHtml(String zip, boolean isCelsius)
	{
		weatherHTML.setVisible(false);

		ws.getWeatherHtml(zip, isCelsius, new AsyncCallback<String>() {
			@Override
			public void onFailure(Throwable caught)
			{
				weatherHTML.setHTML("<h3>Something terribly wrong: " + caught.getMessage() + "</h3>");
			}

			@Override
			public void onSuccess(String result)
			{
				String html = result;
				weatherHTML.setHTML(html);
			}
		});
		weatherHTML.setVisible(true);
	}
}
