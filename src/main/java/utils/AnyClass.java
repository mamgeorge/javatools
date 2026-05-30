package utils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import static java.time.format.DateTimeFormatter.ISO_DATE_TIME;

public class AnyClass {

	public static final String TAB = "\t";

	public static final String regexIpv4 = "^(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\."
			+ "(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\."
			+ "(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\."
			+ "(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$";

	public static final String regexXML = "<([a-zA-Z][a-zA-Z0-9:_-]*)(?:\\s+[^>]*)?>([^<]*)</\\1>";

	// private static Scanner scanner = new Scanner(System.in);

	public static void main(String[] args) {

		regexXMLs();
		regexIpv4();
		regexValid();
		System.out.println("Current time: " + showTime());	
		System.out.println("DONE");
	}
	
	private static void regexXMLs() {

		String[] xmls = {"<h1>They loves counseling</h1>",
				"<h1><h1>Sanjay has no watch</h1></h1><par>So wait for a while</par>",
				"<Amee>safat codes like a ninja</amee>",
				"<SA premium>Imtiaz has a secret crush</SA premium>"};

		Arrays.stream(xmls).forEach(xml -> {
			System.out.println(xml + " / " + xml.matches(regexXML));
		});

		if (xmls[0].matches(regexXML)) {
			System.out.println("matcher.matches!");
		}
		Pattern pattern = Pattern.compile(regexXML);
		Matcher matcher = pattern.matcher(xmls[0]);
		if (matcher.matches()) {
			System.out.println("matcher.matches!");
		}
	}

	private static void regexIpv4() {

		/* "https://cs.lmu.edu/~ray/notes/regex/"
			\b - is a word delimiter
			\w+ - any word, letter, digit or underscore
			\s+ - blank spaces
			\1 - back reference (anything captured by (\w+))
		 */
		String[] ips = {"000.12.12.034",
				"121.234.12.12",
				"23.45.12.56",
				"00.12.123.123123.123",
				"122.23",
				"Hello.IP"};

		Arrays.stream(ips).forEach(ip -> {
			System.out.println(ip + " / " + ip.matches(regexIpv4));
		});

		if (ips[0].matches(regexIpv4)) {
			System.out.println("matcher.matches!");
		}
		Pattern pattern = Pattern.compile(regexIpv4);
		Matcher matcher = pattern.matcher(ips[0]);
		if (matcher.matches()) {
			System.out.println("matcher.matches!");
		}
	}

	private static void regexValid() {

		String[] regexVals = { "([A-Z])(.+)", "[AZ[a-z](a-z)", "batcatpat(nat" };
		int lena = regexVals.length;
		int ictr = 0;
		String message = "";
		String regex = "";
		List<String> list = new ArrayList<>();
		while (lena > 0) {
			message = "Valid";
			regex = regexVals[ictr++];
			try {
				Pattern pattern = Pattern.compile(regex);
				Matcher matcher = pattern.matcher(regex);
				System.out.println(regex + " / " + matcher.matches());
			} catch (PatternSyntaxException ex) {
				message = "Invalid";
			}
			list.add(message);
			lena--;
		}
		String txtLine = String.join("\n", list);
		System.out.println(txtLine);
	}

	private static String showTime() {

		StringBuilder sb = new StringBuilder();
		LocalDateTime localDateTime = LocalDateTime.now();
		sb.append(ISO_DATE_TIME.format(localDateTime));
		return sb.toString();
	}
}
