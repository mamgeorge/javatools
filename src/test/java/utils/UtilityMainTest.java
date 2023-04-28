package utils;

import org.junit.jupiter.api.Test;
import samples.AnyObject;

import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static utils.UtilityMain.EOL;
import static utils.UtilityMain.TAB;
import static utils.UtilityMain.getRandomString;

public class UtilityMainTest {

	public static final Logger LOGGER = Logger.getLogger(UtilityMainTest.class.getName());
	public static final String PATHFILE_LOCAL = "src/test/resources/";
	public static final String ASSERT_MSG = "ASSERT_MSG";

	//#### basics
	@Test void template( ) {
		//
		StringBuilder stringBuilder = new StringBuilder();
		//
		stringBuilder.append("template");
		//
		String txtLines = stringBuilder.toString();
		System.out.println("txtLines: " + txtLines);
		assertTrue(txtLines.split(EOL).length >= 1, ASSERT_MSG);
	}

	@Test void booleans( ) {
		//
		String txtLines = EOL;
		//
		txtLines += String.format("\t true & true \t %s \n", true & true);
		txtLines += String.format("\t true | false\t %s \n", true | false);
		txtLines += String.format("\t true | true \t %s \n", true | true);
		txtLines += String.format("\t false | true\t %s \n", false | true);
		txtLines += String.format("\t false | false\t %s \n", false | false);
		txtLines += String.format("\t true & true & false\t %s \n", true & true & false);
		txtLines += String.format("\t true && true && false\t %s \n", true && true && false);
		//
		System.out.println("txtLines: " + txtLines);
		assertTrue(txtLines.length() >= 1, ASSERT_MSG);
	}

	@Test void showSys( ) {

		String txtLines = UtilityMain.showSys();
		String[] envr = txtLines.split(EOL);
		//
		System.out.println(txtLines);
		System.out.println("envr: " + envr.length);
		assertTrue(envr.length > 40, ASSERT_MSG);
	}

	@Test void showTimes( ) {

		String showTimes = UtilityMain.showTimes();
		//
		int showTimesLen = showTimes.split(EOL).length;
		System.out.println("showTimes: " + showTimes);
		System.out.println("showTimesLen: " + showTimesLen);
		assertTrue(showTimesLen > 4, ASSERT_MSG);
	}

	@Test void stream_sort( ) throws SocketException {
		//
		Set<String> set = new TreeSet<>();
		Enumeration<NetworkInterface> enums = NetworkInterface.getNetworkInterfaces();
		//
		Collections.list(enums).forEach(nifc -> set.add(TAB + nifc.getDisplayName()));
		//
		System.out.println("Stream & sort" + TAB + "size: " + set.size());
		set.forEach(System.out::println);
		assertTrue(set.size() >= 7, ASSERT_MSG);
	}

	@Test void stream_filter( ) throws SocketException {
		//
		Set<String> set = new TreeSet<>();
		Enumeration<NetworkInterface> enums = NetworkInterface.getNetworkInterfaces();
		//
		List<NetworkInterface> listMain = Collections.list(enums);
		List<NetworkInterface> listFilt =
			listMain.stream()
				.filter(nifc -> {
					int mtu = 0;
					try {
						mtu = nifc.getMTU();
					}
					catch (Exception ex) {
						LOGGER.info(ex.getMessage());
					}
					return mtu > 1 && !nifc.getDisplayName().startsWith("VMware");
				})
				.collect(Collectors.toList());
		//
		listFilt.forEach(nifc -> set.add(TAB + nifc.getDisplayName()));
		//
		System.out.println("Stream & filter" + TAB + "size: " + set.size());
		set.forEach(System.out::println);
		assertTrue(set.size() >= 5, ASSERT_MSG);
	}

	@Test void stream_Collections( ) throws SocketException {
		//
		StringBuilder stringBuilder = new StringBuilder();
		Enumeration<NetworkInterface> enums = NetworkInterface.getNetworkInterfaces();
		// stringBuilder.delete(0, stringBuilder.length()).append("Iterator Stream" + EOL);
		//
		stringBuilder.append("Collections Stream" + EOL);
		Collections.list(enums).forEach(nifc -> stringBuilder.append(nifc.getDisplayName()).append(EOL));
		//
		System.out.println(stringBuilder);
		assertTrue(stringBuilder.toString().split(EOL).length >= 7, ASSERT_MSG);
	}

	@Test void stream_Iterator( ) throws SocketException {

		// better for larger numbers
		StringBuilder stringBuilder = new StringBuilder();
		Iterator<NetworkInterface> iterator = NetworkInterface.getNetworkInterfaces().asIterator();
		//
		stringBuilder.append("Iterator Stream" + EOL);
		Stream<NetworkInterface> stream = StreamSupport.stream(
			Spliterators.spliteratorUnknownSize(iterator, Spliterator.ORDERED), false);
		stream.forEach(nifc -> stringBuilder.append(nifc.getDisplayName()).append(EOL));
		//
		System.out.println(stringBuilder);
		assertTrue(stringBuilder.toString().split(EOL).length >= 7, ASSERT_MSG);
	}

	@Test void lineChunker() {

		String txtLines = "";
		String txtLine = "123456789012345678901234567890123456789012345678901234567890";
		int intChunk = 10;

		txtLines = lineChunker(txtLine, intChunk, EOL);
		System.out.println(txtLine + EOL + EOL + txtLines);
		assertNotNull(txtLines);

		txtLine = getRandomString(1000);
		txtLines = lineChunker(txtLine, 80, EOL);
		System.out.println(txtLine + EOL + EOL + txtLines);
		assertNotNull(txtLines);
	}

	//#### files
	@Test void getFileLines( ) {

		String txtLines = UtilityMain.getFileLines("c:/workspace/greetings.txt", "");
		//
		LOGGER.info(txtLines); // System.out.println(txtLines);
		assertTrue(txtLines.length() > 12, ASSERT_MSG);
	}

	@Test void getFileLocal( ) {

		String txtLines = UtilityMain.getFileLocal(PATHFILE_LOCAL + "booksCatalog.json");
		//
		LOGGER.info(txtLines); // System.out.println(txtLines);
		assertTrue(txtLines.length() > 20, ASSERT_MSG);
	}

	@Test void getFileLocals( ) {
		//
		StringBuilder stringBuilder = new StringBuilder();
		String[] fileNames =
			{ "booksCatalog.html", "booksCatalog.json", "booksCatalog.xml", "booksXml2Html.xslt" };
		//
		AtomicInteger idx = new AtomicInteger();
		Arrays.stream(fileNames).sequential().forEach(flnm -> {
			String fileName = PATHFILE_LOCAL + flnm;
			String txtLines = UtilityMain.getFileLocal(fileName);
			stringBuilder.append(
				String.format("\t%02d %s\tsize: %d \n", idx.incrementAndGet(), flnm, txtLines.length()));
		});
		//
		int countFiles = stringBuilder.toString().split(EOL).length;
		System.out.println("countFiles: " + countFiles);
		System.out.println(stringBuilder);
		assertEquals(countFiles, fileNames.length, ASSERT_MSG);
	}

	//#### url
	@Test void urlGet( ) {
		//
		String html = UtilityMain.urlGet("https://mamgeorge.altervista.org");
		System.out.println("html: " + html);
		assertTrue(html.contains("DOCTYPE"), ASSERT_MSG);
	}

	//#### reflection
	@Test void getField( ) {
		//
		String results = UtilityMain.getField(new AnyObject(), "gamma");
		System.out.println("results: " + results);
		assertEquals("GIMMEL", results, ASSERT_MSG);
	}

	@Test void getMethod( ) {
		//
		Object objectParms = null;
		Object object = UtilityMain.getMethod(AnyObject.class, "getGamma", objectParms);
		String results = object.toString();
		System.out.println(results);
		assertEquals("GIMMEL", results, ASSERT_MSG);
	}

	@Test void exposeObject( ) {
		//
		AnyObject anyObject = new AnyObject();
		String txtLines = UtilityMain.exposeObject(anyObject);
		System.out.println(txtLines);
		assertNotNull(txtLines, ASSERT_MSG);
	}

	@Test void putObject( ) {
		//
		AnyObject anyObject = new AnyObject();
		UtilityMain.putObject(anyObject, "gamma", "STUFF");
		String results = UtilityMain.getField(anyObject, "gamma");
		System.out.println(results);
		assertEquals("STUFF", results, ASSERT_MSG);
	}

	//#### proposed statics
	public static String lineChunker(String txtLine, int intChunk, String DLM) {

		String txtLines;
		String[] lineChunks =  txtLine.split("(?<=\\G.{" + intChunk + "})");

		StringBuilder stringBuilder = new StringBuilder();
		Arrays.stream(lineChunks).forEach( lineChunk -> stringBuilder.append(lineChunk + DLM));
		txtLines = stringBuilder.toString();
		return txtLines;
	}
}
//----
