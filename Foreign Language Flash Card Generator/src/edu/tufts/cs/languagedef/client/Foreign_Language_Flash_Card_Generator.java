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
import com.google.gwt.user.client.ui.HasHorizontalAlignment.HorizontalAlignmentConstant;
// Import classes for Google Translate
import com.google.gwt.language.client.LanguageUtils;
import com.google.gwt.language.client.translation.LangDetCallback;
import com.google.gwt.language.client.translation.LangDetResult;
import com.google.gwt.language.client.translation.Language;
import com.google.gwt.language.client.translation.Translation;
import com.google.gwt.language.client.translation.TranslationCallback;
import com.google.gwt.language.client.translation.TranslationResult;
import com.google.gwt.language.client.transliteration.LanguageCode;
import com.google.gwt.layout.client.Layout.Alignment;

// Import classes for string manipulation
import java.util.ArrayList;
import java.util.StringTokenizer;
//import java.util.regex.Pattern;
// Import classes for GWT layout


/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class Foreign_Language_Flash_Card_Generator implements EntryPoint {
	private String[] pronunciation = new String[1];
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
				RootPanel.get().add(new HTML("<h1>Vocab List Generator <font size=3><a href=\"about.html\">(wat?)</a></font></h1>"));
				RootPanel.get().add(createTranslationPanel());
			}
		});
	}

	// Creates translation panel.
	private Panel createTranslationPanel() {
		final FlexTable transTable = new FlexTable();
		final TextArea transArea = new TextArea();
		final Button pronunciationButton = new Button("Get Pronunciations");
		transArea.setPixelSize(300, 100);

		// This is where translation results are put.
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
							pronunciation = new String[terms.length];

							for(int i = 0; i < terms.length; i++) {
								transTable.setText(i, 0, terms[i]);
								transTable.setText(i, 1, "  -  ");
								//vocabList = vocabList.concat(terms[i]).concat(" - ");
								if(termsTranslated.length > i) transTable.setText(i, 2, termsTranslated[i]);
								fetchPronunciationHTML(terms[i], detectedLanguage, i);
								// vocabList = vocabList.concat("<br>");
							}
							
							//fetchPronunciationHTML(terms[0], detectedLanguage);
							//if (pronunciation != null) vocabList = vocabList.concat(pronunciation);
							
							cardsHTML.setHTML(vocabList);
							pronunciationButton.setVisible(true);
						}
					}
				});
			}
		});
		
		pronunciationButton.addClickHandler(new ClickHandler() {

			public void onClick(ClickEvent event) {
				String[] terms = transArea.getText().split("\n");
				for(int i = 0; i < terms.length; i++) {
					transTable.setHTML(i, 3, pronunciation[i]);
				}
			}
		});

		// Add all widgets to translation panel.
		VerticalPanel left = new VerticalPanel();
		VerticalPanel right = new VerticalPanel();
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
		// right.add(cardsHTML);
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
			}
			
		});
	}
}