package samples;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.IntStream;

import static java.time.format.DateTimeFormatter.ISO_DATE_TIME;

public class AnyClass {

	public static final String regexIpv4 = "^(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\."
		+ "(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\."
		+ "(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\."
		+ "(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$";

	public static final String regexXML = "<([a-zA-Z][a-zA-Z0-9:_-]*)(?:\\s+[^>]*)?>([^<]*)</\\1>";

	// private static Scanner scanner = new Scanner(System.in);

	public static void main(String[] args) {

		extras();
		System.out.println("DONE");
	}

	static void extras() {

		System.out.println("time: " + Instant.now().toString() );
	}

	//############
	public static void responseTime( ) {

		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			int responseTimesCount = Integer.parseInt(br.readLine().trim());
			List<Integer> responseTimes = IntStream.range(0, responseTimesCount).mapToObj(i -> {
					try {
						return br.readLine().replaceAll("\\s+$", "");
					}
					catch (IOException ex) {
						throw new RuntimeException(ex);
					}
				})
				.map(String::trim)
				.map(Integer::parseInt)
				.toList();

			int result = AnyClass.countResponseTimeRegressions(responseTimes);
			System.out.println(result);
			br.close();
		}
		catch (IOException ex) { System.out.println("ERROR: " + ex.getMessage()); }
	}

	public static int countResponseTimeRegressions(List<Integer> responseTimes) {

		int avg = 0, acc = 0, ictr = 0, tctr=0;
		if ( responseTimes == null || responseTimes.size() == 0 ) { }
		else {
			for ( int responseTime : responseTimes ) {

				++ictr;
				acc += responseTime;
				avg = acc / ictr;
				if ( ictr == 1 ) { } else {

					if ( responseTime > avg ) { ++tctr; }
				}
			}
		}
		return tctr;
	}

	public static void reversaList( ) {

		String[] strs = "M,A,R,T,I,N, ,G,E,O,R,G,E".split(",");
		List<String> list = Arrays.asList(strs);
		List<String> lost = List.copyOf(list);
		Collections.reverse(list);
		lost.forEach(x -> System.out.print(x));
		System.out.println();
		list.forEach(System.out::print);
		System.out.println();
	}

	public static void regexXMLs( ) {

		String[] xmls = { "<h1>Nayeem loves counseling</h1>",
			"<h1><h1>Sanjay has no watch</h1></h1><par>So wait for a while</par>",
			"<Amee>safat codes like a ninja</amee>",
			"<SA premium>Imtiaz has a secret crush</SA premium>" };

		Arrays.stream(xmls).forEach(xml -> {
			System.out.println(xml + " / " + xml.matches(regexXML));
		});

		if ( xmls[0].matches(regexXML) ) { System.out.println("matcher.matches!"); }
		Pattern pattern = Pattern.compile(regexXML);
		Matcher matcher = pattern.matcher(xmls[0]);
		if ( matcher.matches() ) { System.out.println("matcher.matches!"); }
	}

	public static void regexIpv4( ) {

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
			System.out.println(ip + " / " + ip.matches(regexIpv4));
		});

		if ( ips[0].matches(regexIpv4) ) { System.out.println("matcher.matches!"); }
		Pattern pattern = Pattern.compile(regexIpv4);
		Matcher matcher = pattern.matcher(ips[0]);
		if ( matcher.matches() ) { System.out.println("matcher.matches!"); }
	}

	public static void regexValid( ) {

		String[] regexVals = null;
		// ""new String[]{ "([A-Z])(.+)", "[AZ[a-z](a-z)", "batcatpat(nat" };
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
