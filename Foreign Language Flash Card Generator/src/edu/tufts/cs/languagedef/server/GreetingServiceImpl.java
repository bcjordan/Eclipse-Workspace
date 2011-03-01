package edu.tufts.cs.languagedef.server;

import edu.tufts.cs.languagedef.client.GreetingService;
import edu.tufts.cs.languagedef.shared.FieldVerifier;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
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
public class GreetingServiceImpl extends RemoteServiceServlet implements
		GreetingService {

	private static final String forvoBaseURL = "http://apifree.forvo.com/key/1f50c1707786cef29751878786ffec51/format/xml/action/word-pronunciations/word/";
    private static final String middle = "/language/";
	private static DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();

    public String getPronunciationHTML(String word, String languageCode) {
    	Document xml;
    	String result = "";
    	
    	try {
    		xml = getAudioXMLDocument(word, languageCode);
    		Element item = (Element)xml.getElementsByTagName("pathmp3").item(0);
    		if (item != null) {
    			// Add pronunciation audio HTML (using HTML5 audio tag)
    			result = "<audio controls preload=\"auto\" autobuffer size=\"2\"><source src=\"" + item.getFirstChild().getNodeValue() + "\" /></audio>";
    		} else {
    			result = "";
    		}
    		
    		return result;
    	} catch (Exception e) { return ""; }
    }
    
    private Document getAudioXMLDocument (String word, String languageCode) {
    	try {
	    	URL url = new URL(forvoBaseURL + word + middle + languageCode);

			DocumentBuilder builder = builderFactory.newDocumentBuilder();
			return builder.parse(url.openStream());
    	} catch (Exception e) {
			System.out.println("OI!" + e.getMessage() + "end");
    		return null;
    	} finally {
    	}
    }

    
	public String greetServer(String input) throws IllegalArgumentException {
		// Verify that the input is valid. 
		if (!FieldVerifier.isValidName(input)) {
			// If the input is not valid, throw an IllegalArgumentException back to
			// the client.
			throw new IllegalArgumentException(
					"Name must be at least 4 characters long");
		}

		String serverInfo = getServletContext().getServerInfo();
		String userAgent = getThreadLocalRequest().getHeader("User-Agent");

		// Escape data from the client to avoid cross-site script vulnerabilities.
		input = escapeHtml(input);
		userAgent = escapeHtml(userAgent);

		return "Hello, " + input + "!<br><br>I am running " + serverInfo
				+ ".<br><br>It looks like you are using:<br>" + userAgent;
	}

	/**
	 * Escape an html string. Escaping data received from the client helps to
	 * prevent cross-site script vulnerabilities.
	 * 
	 * @param html the html string to escape
	 * @return the escaped string
	 */
	private String escapeHtml(String html) {
		if (html == null) {
			return null;
		}
		return html.replaceAll("&", "&amp;").replaceAll("<", "&lt;")
				.replaceAll(">", "&gt;");
	}
}
