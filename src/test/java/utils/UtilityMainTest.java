package utils;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.PathResource;
import org.springframework.util.ResourceUtils;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import samples.AnyObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static utils.UtilityMain.EOL;
import static utils.UtilityMain.MAXLEN;
import static utils.UtilityMain.TAB;
import static utils.UtilityMain.getRandomString;

public class UtilityMainTest {

	public static final Logger LOGGER = Logger.getLogger(UtilityMainTest.class.getName());
	public static final String PATHFILE_LOCAL = "src/test/resources/";
	public static final String ASSERT_MSG = "ASSERT_MSG";
	public static final String ISO_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z' EEE"; // 'Z' shows UTC

	//#### basics
	@Test void template( ) {
		//
		//
		//
		String txtLines = "template"
			//
			;
		System.out.println("txtLines: " + txtLines);
		assertTrue(txtLines.split(EOL).length >= 1, ASSERT_MSG);
	}

	@Test void booleans( ) {
		//
		String txtLines = EOL;
		//
		txtLines += String.format("\t true & true \t %s \n", true);
		txtLines += String.format("\t true | false\t %s \n", true);
		txtLines += String.format("\t true | true \t %s \n", true);
		txtLines += String.format("\t false | true\t %s \n", true);
		txtLines += String.format("\t false | false\t %s \n", false);
		txtLines += String.format("\t true & true & false\t %s \n", false);
		txtLines += String.format("\t true && true && false\t %s \n", false);
		//
		System.out.println("txtLines: " + txtLines);
		assertNotNull(txtLines, ASSERT_MSG);
	}

	@Test void showSysEnv( ) {

		String txtLines = UtilityMain.showSysEnv();
		System.out.println(txtLines);
		assertNotNull(txtLines);
	}

	@Test void showSysProp( ) {

		Properties properties = System.getProperties();

		StringBuilder sb = new StringBuilder();
		AtomicInteger aint = new AtomicInteger();
		properties.keySet().stream().sorted().forEach((key) -> {
			String val = System.getProperty(key.toString());
			if (val.length() > MAXLEN) { val = val.substring(0,MAXLEN) + "...";; }
			sb.append(String.format("\t%03d %-30s | %s%n", aint.incrementAndGet(), key, val));
		} );

		System.out.println("size: " + sb.toString().split(EOL).length + EOL + sb);
		assertNotNull(sb);
	}

	@Test void showJvmArgs( ) {

		RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
		List<String> listArgs = runtimeMXBean.getInputArguments();

		StringBuilder sb = new StringBuilder();
		AtomicInteger aint = new AtomicInteger();
		listArgs.stream().sorted().forEach( args -> sb.append(String.format("\t%03d %s%n", aint.incrementAndGet(), args)) );

		System.out.println("size: " + sb.toString().split(EOL).length + EOL + sb);
		assertNotNull(sb);
	}

	@Test void showAppYml( ) {

		String pathYml = "classpath:application.yml";
		String propName = "spring.application.id";
		String propValue = "";
		try {
			File file = ResourceUtils.getFile(pathYml);
			PathResource pathResource = new PathResource(file.toPath());

			YamlPropertiesFactoryBean YPFB = new YamlPropertiesFactoryBean();
			YPFB.setResources(pathResource);
			YPFB.afterPropertiesSet();

			Properties properties = YPFB.getObject();
			assert properties != null;
			propValue = properties.getProperty(propName);
		}
		catch (FileNotFoundException ex) {
			System.out.println("ERROR: " + ex.getMessage());
		}
		System.out.println(EOL + propName + ": " + propValue);
		assertNotNull(propValue);
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
				.toList();
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

	//#### files
	@Test void getFileLines( ) {

		String txtLines = UtilityMain.getFileLines(PATHFILE_LOCAL + "battles.csv", "");
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

	//#### reflection
	@Test void getField( ) {
		//
		String results = UtilityMain.getField(new AnyObject(), "gamma");
		System.out.println("results: " + results);
		assertEquals("GIMMEL", results, ASSERT_MSG);
	}

	@Test void getMethod( ) {

		Object object = UtilityMain.getMethod(AnyObject.class, "getGamma");
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

	// special
	@Test void lineChunker( ) {

		String txtLines;
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

	@Test void checkDates( ) {

		StringBuilder sb = new StringBuilder();
		SimpleDateFormat SDF = new SimpleDateFormat(ISO_FORMAT);
		String dtfISO = DateTimeFormatter.ISO_DATE_TIME.toString();
		String FRMT = "\t%-20s: %s \n";
		sb.append(String.format(FRMT, "dtfISO", dtfISO));

		// date
		Date date = new Date();
		Date dateInstant = Date.from(Instant.now());
		java.sql.Date dateSQL = new java.sql.Date(dateInstant.getTime());
		String dateFormat = SDF.format(date);
		String dateInstantFormat = SDF.format(dateInstant);
		sb.append(String.format(FRMT, "date", date));
		sb.append(String.format(FRMT, "dateInstant", dateInstant));
		sb.append(String.format(FRMT, "dateSQL", dateSQL));
		sb.append(String.format(FRMT, "dateFormat", dateFormat));
		sb.append(String.format(FRMT, "dateInstantFormat", dateInstantFormat));
		sb.append(StringUtils.repeat("-", 40)).append(EOL);

		// calendar
		Calendar calendar = Calendar.getInstance();
		GregorianCalendar gregorianCalendar = (GregorianCalendar) calendar;
		ZonedDateTime ZDT = gregorianCalendar.toZonedDateTime();
		OffsetDateTime ODT = OffsetDateTime.now();
		OffsetDateTime ODTZO = OffsetDateTime.now(ZoneOffset.UTC);
		sb.append(String.format(FRMT, "calendar DATE", calendar.get(Calendar.DATE)));
		sb.append(String.format(FRMT, "gregorianCalendar", gregorianCalendar.get(Calendar.DATE)));
		sb.append(String.format(FRMT, "ZonedDateTime", ZDT));
		sb.append(String.format(FRMT, "OffsetDateTime", ODT));
		sb.append(String.format(FRMT, "ODT ZoneOffset", ODTZO));
		sb.append(StringUtils.repeat("-", 40)).append(EOL);

		// local
		LocalDate localDate = LocalDate.of(2023, Month.SEPTEMBER, 1);
		LocalDate localDateNow = LocalDate.now();
		LocalDate localDateZID = LocalDate.now(ZoneId.systemDefault());
		LocalDateTime localDateTime = LocalDateTime.of(localDateNow, LocalTime.of(0, 0));
		Date dateLocalNow = Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
		String dateLocalFormat = SDF.format(dateLocalNow);
		sb.append(String.format(FRMT, "localDate", localDate));
		sb.append(String.format(FRMT, "localDateNow", localDateNow));
		sb.append(String.format(FRMT, "localDateZID", localDateZID));
		sb.append(String.format(FRMT, "localDateTime", localDateTime));
		sb.append(String.format(FRMT, "dateLocalNow", dateLocalNow));
		sb.append(String.format(FRMT, "dateLocalFormat", dateLocalFormat));

		System.out.println(sb);
		assertNotNull(sb);
	}

	//#### proposed statics
	public static String lineChunker(String txtLine, int intChunk, String DLM) {

		String txtLines;
		String[] lineChunks = txtLine.split("(?<=\\G.{" + intChunk + "})");

		StringBuilder stringBuilder = new StringBuilder();
		AtomicInteger aint = new AtomicInteger();
		Arrays.stream(lineChunks).forEach(lineChunk -> stringBuilder.append(
			String.format("%02d %s" + DLM,aint.incrementAndGet(),lineChunk)));
		txtLines = stringBuilder.toString();
		return txtLines;
	}
}
//----
