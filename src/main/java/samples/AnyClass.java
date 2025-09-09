package samples;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import static java.time.format.DateTimeFormatter.ISO_DATE_TIME;

public class AnyClass {

	public static final String ipv4Regex = "^(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\."
		+ "(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\."
		+ "(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\."
		+ "(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$";
	// private static Scanner scanner = new Scanner(System.in);

	public static void main(String[] args) {

		reversaList();
		System.out.println("DONE");
	}

	public static void extra( ) {}

	public static void reversaList( ) {

		String[] strs = "M,A,R,T,I,N, ,G,E,O,R,G,E".split(",");
		List<String> list = Arrays.asList(strs);
		List<String> lost = List.copyOf(list);
		Collections.reverse(list);
		lost.forEach(x -> System.out.print(x));
		System.out.println("");
		list.forEach(System.out::print);
		System.out.println("");
	}

	public static void regexMatch( ) {

		/* "https://cs.lmu.edu/~ray/notes/regex/"
			\b - is a word delimiter
			\w+ - any word, letter, digit or underscore
			\s+ - blank spaces
			\1 - back reference (anything captured by (\w+))
		 */
		String[] ips = { "000.12.12.034",
			"121.234.12.12",
			"23.45.12.56",
			"00.12.123.123123.123",
			"122.23",
			"Hello.IP" };

		Arrays.stream(ips).forEach(ip -> {
			System.out.println(ip + " / " + ip.matches(AnyClass.ipv4Regex));
		});

		if ( ips[0].matches(AnyClass.ipv4Regex) ) { System.out.println("matcher.matches!"); }
		Pattern pattern = Pattern.compile(AnyClass.ipv4Regex);
		Matcher matcher = pattern.matcher(ips[0]);
		if ( matcher.matches() ) { System.out.println("matcher.matches!"); }
	}

	public static void regexValid( ) {

		String[] regexVals = new String[]{ "([A-Z])(.+)", "[AZ[a-z](a-z)", "batcatpat(nat" };
		//
		int lena = regexVals.length;
		int ictr = 0;
		String message = "";
		String regex = "";
		List<String> list = new ArrayList<>();
		while ( lena > 0 ) {
			message = "Valid";
			regex = regexVals[ictr++];
			try {
				Pattern pattern = Pattern.compile(regex);
				Matcher matcher = pattern.matcher(regex);
			}
			catch (PatternSyntaxException ex) { message = "Invalid"; }
			//System.out.println("regex: " + regex);
			list.add(message);
			lena--;
		}
		String txtLine = String.join("\n", list);
		System.out.println(txtLine);
	}

	public static String showTime( ) {

		LocalDateTime localDateTime = LocalDateTime.now();
		String txtLine = ISO_DATE_TIME.format(localDateTime);
		return txtLine;
	}
}
