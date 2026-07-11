package utils;

import static utils.UtilityMain.EOL;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.lang.ExceptionInInitializerError;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.language.AmericanEnglish;
import org.languagetool.rules.Rule;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.spelling.SpellingCheckRule;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import objects.Bpod;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;
class SpecialTest {
	
	private static final String TEXTFILE = "This is a simpele MLG test to see if it catches the mispelled word.";
	private static final List<String> EXCLUSIONS = Arrays.asList("MLG", "BPOD", "APOD",
		"bibleHelp", "images_bible","Gen", "Exo", "Lev", "Num", "Deu", "Jos", "Jdg", "Rth", "1Sa", "2Sa",
		"1Kin", "2Kin", "1Chr", "2Chr", "Ezr", "Neh", "Est", "Job", "Psa", "Pro", "Ecc", "Sng", 
		"Isa", "Jer", "Lam", "Eze", "Dan", "Hos", "Joe", "Amo", "Oba", "Jon",
		
		"fieldset", "boxpad", "br", "smllink", "_blank", "href", "ora", "jpg", "td", "th", "tr", "tbm",
		"colspan", "html", "_parent", "onclick", "www", "http", "https", "com", "org", "gif",
		"margin-left", "margin-right", "margin-top", "margin-bottom", "padding-left", "padding-right",
		"padding-top", "padding-bottom", "font-family", "font-size", 
		
		"Deep_Time", "Speiser", "timeframe", "Occams", "Halaf", "Ubaid", "Uruk"
	);

	private static final String PATH_BPOD = "C:/Users/mamge/OneDrive/Documents/5Personal/Technology/mamgeorge/3service/";
	private static final String JS_BINDING = "js";
	private static final String JS_TXTITEM = "txtItem";
	private static int ictr = 0;

	@Test void test_checkText() {

		StringBuilder sb = getSpellCheck(TEXTFILE, EXCLUSIONS);
		System.out.println(sb.toString());
		assert(sb.toString().contains("ERROR"));
	}
	
	@Test void test_checkJsFile() {

		Path filePath = Path.of(PATH_BPOD + "bibleTxt.js");
		String jsCode = "";
		try { jsCode = Files.readString(filePath); } 
		catch (IOException ex) { System.out.println("ERROR: " + ex); }

		Context context = Context.create(JS_BINDING);
		context.eval(JS_BINDING, jsCode);
		Value values = context.getBindings(JS_BINDING).getMember(JS_TXTITEM);
		System.out.println("jsArray: " + values.getArraySize() );

		StringBuilder sb = new StringBuilder();
		Value value = null;
		String txt = "";
		for (int ictr = 0; ictr < 10; ictr++) {
			value = values.getArrayElement(ictr).getArrayElement(4);
			txt = String.valueOf(value).replaceAll("\\s+", " ");
			//System.out.println(ictr + ": " + txt + EOL);
			sb.append(getSpellCheck(txt, EXCLUSIONS));
		}
		System.out.println(sb.toString());
	}

	// statics
	@SuppressWarnings("unused")
	private static void mapToBpod(Context context) {

		String jsons = "JSON.stringify(" + JS_TXTITEM + ");";
		String jsonString = context.eval(JS_BINDING, jsons).toString();
		try { 
			ObjectMapper mapper = new ObjectMapper();
			List<Bpod> bpods = mapper.readValue(jsonString, new TypeReference<List<Bpod>>() {}); 
			System.out.println("Bpods: " + bpods.size());
			System.out.println("bpod 1: " + bpods.get(0).toString());			
		}
		catch (IOException ex) { System.out.println("INFO: " + ex.getMessage().substring(0,40)); }
	}

	private static StringBuilder getSpellCheck(String textFile, List<String> exclusions) {

		StringBuilder sb = new StringBuilder();
		AmericanEnglish language = new AmericanEnglish();
		try {
			JLanguageTool jLanguageTool = new JLanguageTool(language);
			getExclusions(jLanguageTool, exclusions);
			sb = getRuleMatches(jLanguageTool, textFile);

		} catch (ExceptionInInitializerError | IOException ex) {
			System.out.println("ERROR: " + ex);
		}
		return sb;
	}

	private static StringBuilder getRuleMatches(JLanguageTool jLanguageTool, String textFile)
		throws IOException {

		StringBuilder sb = new StringBuilder();
		List<RuleMatch> ruleMatchList = jLanguageTool.check(textFile);
		for (RuleMatch ruleMatch : ruleMatchList) {
			sb.append(EOL + ++ictr + ": " + "ERROR: ["
					+ ruleMatch.getFromPos() + ":"
					+ ruleMatch.getToPos() + "] "
					+ textFile.substring(ruleMatch.getFromPos(), ruleMatch.getToPos()));
			sb.append("\t| Suggested: " + ruleMatch.getSuggestedReplacements());
		}
		return sb;
	}

	private static void getExclusions(JLanguageTool jLanguageTool, List<String> exclusions) {

		for (Rule rule : jLanguageTool.getAllActiveRules()) {
			if (rule instanceof SpellingCheckRule) {
				((SpellingCheckRule) rule).addIgnoreTokens(exclusions);
			}
		}
	}
}
