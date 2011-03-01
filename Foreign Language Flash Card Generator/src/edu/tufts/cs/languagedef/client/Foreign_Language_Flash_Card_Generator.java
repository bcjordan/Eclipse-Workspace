package edu.tufts.cs.languagedef.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
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

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class Foreign_Language_Flash_Card_Generator implements EntryPoint {
	private String[] pronunciation = new String[1];
	private String detectedLanguage = "fr";
	final FlexTable transTable = new FlexTable();
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
				RootPanel.get().add(new HTML("<h1>Vocab List Generator <font size=3><a href=\"about.html\">(que?)</a></font></h1>"));
				RootPanel.get().add(createTranslationPanel());
			}
		});
	}

	// Creates translation panel.
	private Panel createTranslationPanel() {
		final TextArea transArea = new TextArea();
		final Button pronunciationButton = new Button("Get Pronunciations");
		transArea.setPixelSize(300, 100);
		transArea.setText("bonjour\nmonsieur\ncomment\nvas\ntu");
		final HTML cardsHTML = new HTML();
		final HTML detectionHTML = new HTML();

		Button translateButton = new Button("Translations");
		translateButton.addClickHandler(new ClickHandler() {

			public void onClick(ClickEvent event) {
				Translation.detect(transArea.getText().replaceAll("\n", " "), new LangDetCallback() {
					@Override
					protected void onCallback(LangDetResult result) {
						detectedLanguage = result.getLanguage();
						detectionHTML.setHTML("Detected language: " + detectedLanguage);
					}
				});
				
				// To perform the translation, we add a '(' character in place of each new line. This keeps our terms
				// separate and forces Google Translate to consider them separately.
				Translation.translate(transArea.getText().replaceAll("\n", "\\("), getLanguage(detectedLanguage),
						Language.ENGLISH, new TranslationCallback() {

					@Override
					protected void onCallback(TranslationResult result) {
						if (result.getError() != null) {
							cardsHTML.setHTML(result.getError().getMessage());
						} else {
							String splitChar = "(";
							String[] terms = transArea.getText().split("\n");
							// Remove and split on the '(' characters we sent to Google Translate as term separators.
							String[] termsTranslated = result.getTranslatedText().split("\\(");
							
							String vocabList = "";
							pronunciation = new String[terms.length];
							transTable.clear(true);

							for(int i = 0; i < terms.length; i++) {
								
								// Populate pronunciation table
								transTable.setText(i, 0, terms[i]);
								transTable.setText(i, 1, "  -  ");

								if(termsTranslated.length > i) transTable.setText(i, 2, termsTranslated[i]);
							}

							pronunciationButton.setVisible(true); // Activate pronunciation button
						}
					}
				});
			}

			private Language getLanguage(String d) {
				// TODO Auto-generated method stub
				for (int i = 0; i < Language.values().length; i++) {
					if(Language.values()[i].getLangCode().equalsIgnoreCase(d))
							return Language.values()[i];
				}
				return null;
			}
		});
		
		pronunciationButton.addClickHandler(new ClickHandler() {

			public void onClick(ClickEvent event) {
				String[] terms = transArea.getText().split("\n");
				for(int i = 0; i < terms.length; i++) {
					fetchPronunciationHTML(terms[i], detectedLanguage, i);
				}
			}
		});

		// Add all widgets to translation panel.
		VerticalPanel left = new VerticalPanel();
		VerticalPanel right = new VerticalPanel();
		// Depreciated, but can't find replacement.
		HorizontalSplitPanel main = new HorizontalSplitPanel();
		
		left.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
		main.setWidth("100%");
		main.setStyleName("mainpanel");
		HorizontalPanel buttons = new HorizontalPanel();
		left.add(transArea);
		buttons.add(translateButton);
		buttons.add(pronunciationButton);
		left.add(buttons);
		
		pronunciationButton.setVisible(false);
		
		right.add(detectionHTML);
		right.add(new Label("Translation result: "));
		right.add(transTable);
		main.setLeftWidget(left);
		main.setRightWidget(right);
		main.setSize("620px", "300px");
		main.setSplitPosition("50%");
		DecoratorPanel decPanel = new DecoratorPanel();
		decPanel.setWidget(main);
		
		return decPanel;
	}
	
	private void fetchPronunciationHTML(String word, String languageCode, int i) {
		final int number = i;
		ws.getPronunciationHTML(word, languageCode, new AsyncCallback<String>() {
			@Override
			public void onFailure(Throwable caught)
			{
				// TODO: Update some sort of error box
			}
			
			@Override
			public void onSuccess(String result)
			{
				String html = "";
				if (!html.equals("java.lang.IllegalArgumentException")) html = result;
				pronunciation[number] = html;
				transTable.setHTML(number, 3, html);
			}
			
		});
	}
}