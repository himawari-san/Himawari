package jp.ac.ninjal.himawari;

import java.awt.Component;
import java.util.Arrays;
import java.util.Locale;

import javax.swing.JOptionPane;

public class LocaleUtil {
	public static final String DEFAULT_LANGUAGE = "ja";
	public static final String[] LANGUAGES = {"ja", "en"};
	
	public String nextStartupLanguage = "";
	
	public LocaleUtil() {
	}
	
	
	public String getNextStartupLanguage() {
		return nextStartupLanguage;
	}
	
	
	public void setNextStartupLanguage(String language) {
		nextStartupLanguage = language;
	}

	
	static public String confirmLanguage(Component parent){
		if(getLanguage() == DEFAULT_LANGUAGE) {
			return DEFAULT_LANGUAGE;
		}
		
		int response = JOptionPane.showConfirmDialog(
				parent,
				"The UI language is set to English.\n"
					+ "Would you like to switch to Japanese?\n"
					+ "You can also change this setting later from \n[Tools] > [Options] > Language.",
				"Confirmation of UI Language",
				JOptionPane.YES_NO_OPTION);
		
		if(response == JOptionPane.YES_OPTION) {
			setLocale(Locale.JAPAN);
		}

		return getLanguage();
	}
	
	
	static public String[] getLanguages() {
		return LANGUAGES;
	}
	
	
	static public Locale getLocale() {
		return Locale.getDefault();
	}
	
	static public void setLocale(Locale locale) {
		Locale.setDefault(locale);
		Messages.setLocale(locale);
	}	

	
	static public String getLanguage() {
		return getLocale().getLanguage();
	}

	
	static public String getLanguage(String displayLanguage) {
		System.err.println("b:" + displayLanguage + "," + getLocale().getDisplayLanguage());
		return Arrays.stream(LANGUAGES)
			.filter(language -> new Locale(language).getDisplayLanguage().equals(displayLanguage))
			.findFirst()
			.orElse(null);
	}
	

	static public String[] getDisplayLanguages() {
		return Arrays.stream(LANGUAGES)
	            .map(language -> new Locale(language).getDisplayLanguage())
	            .toArray(String[]::new);
	}
	
}
