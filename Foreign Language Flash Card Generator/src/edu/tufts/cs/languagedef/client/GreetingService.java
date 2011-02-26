package edu.tufts.cs.languagedef.client;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * The client side stub for the RPC service.
 */
@RemoteServiceRelativePath("greet")
public interface GreetingService extends RemoteService {
	public String greetServer(String name) throws IllegalArgumentException;
	public String getPronunciationHTML (String word, String languageCode);

}
