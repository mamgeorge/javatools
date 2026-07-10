package utils;

import static utils.UtilityMain.EOL;

import java.io.IOException;
import java.util.List;
import java.lang.ExceptionInInitializerError;

import org.junit.jupiter.api.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.language.AmericanEnglish;
import org.languagetool.rules.RuleMatch;

class SpecialTest {

	@Test void test_spellCheck () {

		String txt = "This is a simpele test to see if it catches the mispelled word.";
		StringBuilder sb = new StringBuilder();

		List<RuleMatch> matches = null;
		try {
			AmericanEnglish americanEnglish = new AmericanEnglish();
			JLanguageTool langTool = new JLanguageTool(americanEnglish);
			matches = langTool.check(txt);

			for (RuleMatch match : matches) {	
    	        sb.append(EOL + "Error: [" + match.getFromPos() + ":" + match.getToPos() + "] " 
					+ txt.substring(match.getFromPos(), match.getToPos()) );
        	    sb.append("\t| Suggested: " + match.getSuggestedReplacements());
        	}
		} catch (ExceptionInInitializerError | IOException ex) {
			System.out.println("ERROR: " + ex);
		}
		System.out.println(sb.toString());
		assert(matches.size() > 0);
	}
}
