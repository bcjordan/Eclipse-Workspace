package edu.tufts.cs.languagedef.client;

import edu.tufts.cs.languagedef.shared.FieldVerifier;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;
// Import classes for Google Translate
import com.google.gwt.language.client.LanguageUtils;
import com.google.gwt.language.client.translation.LangDetCallback;
import com.google.gwt.language.client.translation.LangDetResult;
import com.google.gwt.language.client.translation.Language;
import com.google.gwt.language.client.translation.Translation;
import com.google.gwt.language.client.translation.TranslationCallback;
import com.google.gwt.language.client.translation.TranslationResult;
import com.google.gwt.language.client.transliteration.LanguageCode;
// Import classes for string manipulation
import java.util.StringTokenizer;
//import java.util.regex.Pattern;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class Foreign_Language_Flash_Card_Generator implements EntryPoint {
	private String pronunciation;
	private String detectedLanguage;
	private final GreetingServiceAsync ws = GWT.create(GreetingService.class); // Make the RPC
	
	/**
	 * The message displayed to the user when the server cannot be reached or
	 * returns an error.
	 */
	private static final String SERVER_ERROR = "An error occurred while "
		+ "attempting to contact the server. Please check your network "
		+ "connection and try again.";

	/**
	 * Create a remote service proxy to talk to the server-side Greeting service.
	 */
	private final GreetingServiceAsync greetingService = GWT
	.create(GreetingService.class);

	/**
	 * This is the entry point method.
	 */
	public void onModuleLoad() {
		// Loads the translation API
		//
		// It is not safe to make calls into the translation API until the run()
		// method is invoked. Use LanguageUtils.loadTransliteration() for the
		// transliteration API.
		LanguageUtils.loadTranslation(new Runnable() {
			public void run() {
				RootPanel.get().add(new HTML("<h1>SimpleLanguage</h1><p>"));
				RootPanel.get().add(createTranslationPanel());
			}
		});
	}

	// Creates translation panel.
	private Panel createTranslationPanel() {
		final TextArea transArea = new TextArea();
		transArea.setPixelSize(200, 100);

		// This is where translation results are put.
		final HTML cardsHTML = new HTML();
		final HTML detectionHTML = new HTML();

		Button translateButton = new Button("Translate");
		translateButton.addClickHandler(new ClickHandler() {

			public void onClick(ClickEvent event) {
				Translation.detect(transArea.getText(), new LangDetCallback() {
					@Override
					protected void onCallback(LangDetResult result) {
						detectedLanguage = result.getLanguage();
						detectionHTML.setHTML("Detected language: " + detectedLanguage);
					}
				});
				Translation.translate(transArea.getText().replaceAll("\n", "\\("), Language.FRENCH,
						Language.ENGLISH, new TranslationCallback() {

					@Override
					protected void onCallback(TranslationResult result) {
						if (result.getError() != null) {
							cardsHTML.setHTML(result.getError().getMessage());
						} else {
							String splitChar = "(";
							String[] terms = transArea.getText().split("\n");
							String[] termsTranslated = result.getTranslatedText().split("\\(");
							
							String vocabList = "";
							
							for(int i = 0; i < terms.length; i++) {
								vocabList = vocabList.concat(terms[i]).concat(" - ");
								if(termsTranslated.length > i) vocabList = vocabList.concat(termsTranslated[i]);
								vocabList = vocabList.concat("<br>");
							}
							
							fetchPronunciationHTML(terms[0], detectedLanguage);
							if (pronunciation != null) vocabList = vocabList.concat(pronunciation);
							
							cardsHTML.setHTML(vocabList);
						}
					}
				});
			}
		});

		// Add all widgets to translation panel.
		VerticalPanel left = new VerticalPanel();
		VerticalPanel right = new VerticalPanel();
		HorizontalPanel main = new HorizontalPanel();
		left.add(transArea);
		left.add(translateButton);
		right.add(detectionHTML);
		right.add(new Label("Translation result: "));
		right.add(cardsHTML);
		main.add(left);
		main.add(right);
		return main;
	}
	
	private void fetchPronunciationHTML(String word, String languageCode) {
		ws.getPronunciationHTML(word, languageCode, new AsyncCallback<String>() {
			@Override
			public void onFailure(Throwable caught)
			{
				// TODO: Update some sort of error box
			}
			
			@Override
			public void onSuccess(String result)
			{
				String html = result;
				System.out.println("Yo, we got an " + result);
				pronunciation = html;
			}
			
		});
	}
}