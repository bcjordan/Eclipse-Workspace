package edu.tufts.cs.languagedef.client;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * The async counterpart of <code>GreetingService</code>.
 */
public interface GreetingServiceAsync {
	void greetServer(String input, AsyncCallback<String> callback)
			throws IllegalArgumentException;
	void getPronunciationHTML(String word, String languageCode, AsyncCallback<String> callback);
}
