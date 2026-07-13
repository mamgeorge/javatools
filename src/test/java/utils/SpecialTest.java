package utils;

import static utils.UtilityMain.EOL;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.lang.ExceptionInInitializerError;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;

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
	private static final List<String> EXCLUSIONS = Arrays.asList("MLG", "BPOD");

	private static final String PATH_BPOD = "C:/Users/mamge/OneDrive/Documents/5Personal/Technology/mamgeorge/3service/";
	private static final String PATH_EXCL = "C:/workspace/github/javaSamples/javatools/src/main/resources/";
	private static final String JS_BINDING = "js";
	private static final String JS_TXTITEM = "txtItem";
	private static int ictr = 0;

	@Test void test_checkText() {

		StringBuilder sb = getSpellCheck(TEXTFILE, EXCLUSIONS, false);
		System.out.println(sb.toString());
		assert (sb.toString().contains("ERROR"));
	}

	@Test void test_checkJsFile() { // 2026/07/12 exclusions at 4335!

		Instant timeBeg = Instant.now();
		String jsCode = getJsCodeFile(PATH_BPOD + "bibleTxt.js");
		List<String> exclusionList = getExclusionFileList(PATH_EXCL + "exclusions.txt");

		Value values = getBpodValues(jsCode);
		int bpods = (int) values.getArraySize();
		System.out.println("bpods: " + bpods);

		TreeSet<String> treeSet = getErrors(exclusionList, values, bpods, true);
		System.out.println(treeSet.toString());

		// time
		Instant timeEnd = Instant.now();
		double seconds = Duration.between(timeBeg, timeEnd).toMillis() / 1000.0;
		System.out.println("timeBeg: " + timeBeg);
		System.out.println("timeEnd: " + timeEnd);
		System.out.println("time: " + seconds + " seconds");
	}
	// statics
	private static String getJsCodeFile(String pathFile) {

		Path filePath = Path.of(pathFile);
		String jsCode = "";
		try {
			jsCode = Files.readString(filePath);
		} catch (IOException ex) {
			System.out.println("ERROR: " + ex);
		}
		return jsCode;
	}	

	private static List<String> getExclusionFileList(String pathFile) {
	
		Path fileExcl = Path.of(pathFile);
		Stream<String> lines = null;
		try { lines = Files.lines(fileExcl); } 
		catch (IOException ex) { System.out.println("ERROR: " + ex); }

		List<String> exclusionList = lines
				.filter(line -> !line.contains("//")) // Exclude lines with //
				.flatMap(line -> Stream.of(line.split("\\s+"))) // Split by spaces/whitespace
				.filter(word -> !word.isEmpty()) // Remove empty strings from extra spaces
				.collect(Collectors.toList());
		return exclusionList;
	}

	private static void getExclusions(JLanguageTool jLanguageTool, List<String> exclusions) {

		for (Rule rule : jLanguageTool.getAllActiveRules()) {
			if (rule instanceof SpellingCheckRule) {
				((SpellingCheckRule) rule).addIgnoreTokens(exclusions);
			}
		}
	}

	private static Value getBpodValues(String jsCode) {

		Context context = Context.create(JS_BINDING);
		context.eval(JS_BINDING, jsCode);
		Value values = context.getBindings(JS_BINDING).getMember(JS_TXTITEM);
		return values;
	}

	private static StringBuilder getSpellCheck(String textFile, 
		List<String> exclusions, boolean isSingle) {

		StringBuilder sb = new StringBuilder();
		AmericanEnglish language = new AmericanEnglish();
		try {
			JLanguageTool jLanguageTool = new JLanguageTool(language);
			getExclusions(jLanguageTool, exclusions);
			sb = getRuleMatches(jLanguageTool, textFile, isSingle);

		} catch (ExceptionInInitializerError | IOException ex) {
			System.out.println("ERROR: " + ex);
		}
		return sb;
	}

	private static StringBuilder getRuleMatches(JLanguageTool jLanguageTool, 
		String textFile, boolean isSingle) throws IOException {

		StringBuilder sb = new StringBuilder();
		List<RuleMatch> ruleMatchList = jLanguageTool.check(textFile);
		for (RuleMatch ruleMatch : ruleMatchList) {
			if (isSingle) {
				sb.append(EOL + textFile.substring(ruleMatch.getFromPos(), ruleMatch.getToPos()));
			} else {
				sb.append(EOL + ++ictr + ": " + "ERROR: ["
					+ ruleMatch.getFromPos() + ":"
					+ ruleMatch.getToPos() + "] "
					+ textFile.substring(ruleMatch.getFromPos(), ruleMatch.getToPos()));
				sb.append("\t| Suggested: " + ruleMatch.getSuggestedReplacements());
			}
		}
		return sb;
	}

	private static TreeSet<String> getErrors(List<String> exclusionList, 
			Value values, int bpods, boolean isSingle) {

		TreeSet<String> treeSet = new TreeSet<>();
		Value value = null;
		String txt = "";
		for (int ictr = 0; ictr < bpods; ictr++) {
			value = values.getArrayElement(ictr).getArrayElement(4);
			txt = String.valueOf(value).replaceAll("\\s+", " ");
			// System.out.println(ictr + ": " + txt + EOL);
			treeSet.add(getSpellCheck(txt, exclusionList, isSingle).toString());
		}
		return treeSet;
	}

	@SuppressWarnings("unused")
	private static void mapToBpod(Context context) {

		String jsons = "JSON.stringify(" + JS_TXTITEM + ");";
		String jsonString = context.eval(JS_BINDING, jsons).toString();
		try {
			ObjectMapper mapper = new ObjectMapper();
			List<Bpod> bpods = mapper.readValue(jsonString, new TypeReference<List<Bpod>>() {
			});
			System.out.println("Bpods: " + bpods.size());
			System.out.println("bpod 1: " + bpods.get(0).toString());
		} catch (IOException ex) {
			System.out.println("INFO: " + ex.getMessage().substring(0, 40));
		}
	}
}
