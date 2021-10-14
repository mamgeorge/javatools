package utils;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.TreeSet;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpMethod;
import org.springframework.util.Assert;

import static java.nio.charset.StandardCharsets.UTF_8;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.OK;

public class UtilityMainTest {

	static final Logger LOGGER = Logger.getLogger(UtilityMainTest.class.getName());
	static final String PATHFILE_LOCAL = "src/test/resources/";
	static final String ASSERT_MSG = "ASSERT_MSG";

	private enum DATABASES {sqlite, mysql, cassandra, oracle}

	@Test void test_showSys() {

		String txtLines = "";
		txtLines = UtilityMain.showSys();
		String[] envr = txtLines.split(",");
		//
		StringBuilder stringBuilder = new StringBuilder();
		Arrays.stream(envr).forEach(str -> stringBuilder.append(str + "\n"));
		System.out.println(stringBuilder);
		System.out.println("envr: " + envr.length);
		Assert.isTrue(envr.length > 60, ASSERT_MSG);
	}

	@Test void test_showTime() {

		String showTime = UtilityMain.showTime();
		String instantNow = Instant.now().toString();
		//
		System.out.println(String.format("showTime..: %-30s | %02d", showTime, showTime.length()));
		System.out.println(String.format("instantNow: %-30s | %02d", instantNow, instantNow.length()));
		Assert.isTrue(showTime.length() > 20, ASSERT_MSG);
		Assert.isTrue(instantNow.length() > 20, ASSERT_MSG);
	}

	@Test void test_showTimes() {

		String showTimes = UtilityMain.showTimes();
		//
		int showTimesLen = showTimes.split("\n").length;
		System.out.println("showTimes: " + showTimes);
		System.out.println("showTimesLen: " + showTimesLen);
		Assert.isTrue(showTimesLen > 4, ASSERT_MSG);
	}

	@Test void test_Stream_sort() throws SocketException {
		//
		Set<String> set = new TreeSet<>();
		Enumeration<NetworkInterface> enums = NetworkInterface.getNetworkInterfaces();
		//
		Collections.list(enums).stream().forEach(nifc -> set.add("\t" + nifc.getDisplayName()));
		//
		System.out.println("Stream & sort" + "\t" + "size: " + set.size());
		set.stream().forEach(System.out::println);
		Assert.isTrue(set.size() >= 7, ASSERT_MSG);
	}

	@Test void test_Stream_filter() throws SocketException {
		//
		Set<String> set = new TreeSet<>();
		Enumeration<NetworkInterface> enums = NetworkInterface.getNetworkInterfaces();
		//
		List<NetworkInterface> listMain = Collections.list(enums);
		List<NetworkInterface> listFilt =
			listMain.stream()
				.filter(nifc -> {
					int mtu = 0;
					try { mtu = nifc.getMTU(); }
					catch (Exception ex) { LOGGER.info(ex.getMessage()); }
					return mtu > 1 && !nifc.getDisplayName().startsWith("VMware");
				})
				.collect(Collectors.toList());
		//
		listFilt.forEach(nifc -> set.add("\t" + nifc.getDisplayName()));
		//
		System.out.println("Stream & filter" + "\t" + "size: " + set.size());
		set.stream().forEach(System.out::println);
		Assert.isTrue(set.size() >= 5, ASSERT_MSG);
	}

	@Test void test_Stream_Collections() throws SocketException {
		//
		StringBuilder stringBuilder = new StringBuilder();
		Enumeration<NetworkInterface> enums = NetworkInterface.getNetworkInterfaces();
		// stringBuilder.delete(0, stringBuilder.length()).append("Iterator Stream" + "\n");
		//
		stringBuilder.append("Collections Stream" + "\n");
		Collections.list(enums).stream().forEach(nifc -> stringBuilder.append(getNetIface(nifc, 0)));
		//
		System.out.println(stringBuilder);
		Assert.isTrue(stringBuilder.toString().split("\n").length >= 7, ASSERT_MSG);
	}

	@Test void test_Stream_Iterator() throws SocketException {
		// better for larger numbers
		StringBuilder stringBuilder = new StringBuilder();
		Enumeration<NetworkInterface> enums = NetworkInterface.getNetworkInterfaces();
		Iterator iterator = NetworkInterface.getNetworkInterfaces().asIterator();
		//
		stringBuilder.append("Iterator Stream" + "\n");
		Stream<NetworkInterface> stream = StreamSupport.stream(
			Spliterators.spliteratorUnknownSize(iterator, Spliterator.ORDERED), false);
		stream.forEach(nifc -> stringBuilder.append(getNetIface(nifc, 0)));
		//
		System.out.println(stringBuilder);
		Assert.isTrue(stringBuilder.toString().split("\n").length >= 7, ASSERT_MSG);
	}

	@Test void test_getField() {
		//
		String txtLine = UtilityMain.getField(new AnyObject(), "secretValue");
		System.out.println(txtLine);
		Assert.isTrue(txtLine.equals("SECRETSTR"), ASSERT_MSG);
	}

	@Test void test_getMethod() {
		//
		Object objectParms = null;
		Object object = UtilityMain.getMethod(AnyObject.class, "getSecretValue", objectParms);
		String txtLine = object.toString();
		System.out.println(txtLine);
		Assert.isTrue(txtLine.equals("SECRETSTR"), ASSERT_MSG);
	}

	@Test void test_putObject() {
		//
		AnyObject anyObject = new AnyObject();
		UtilityMain.putObject(anyObject, "secretValue", "STUFF");
		String txtLine = UtilityMain.getField(anyObject,"secretValue");
		System.out.println(txtLine);
		Assert.isTrue(txtLine.equals("STUFF"), ASSERT_MSG);
	}

	//############
	@Test void test_getFileLines() {

		String txtLines = "";
		txtLines = UtilityMain.getFileLines("c:/workspace/greetings.txt", "");
		//
		LOGGER.info(txtLines); // System.out.println(txtLines);
		Assert.isTrue(txtLines.length() > 12, ASSERT_MSG);
	}

	@Test void test_getFileLocal() {

		String txtLines = "";
		txtLines = UtilityMain.getFileLocal(PATHFILE_LOCAL + "booksCatalog.json", "");
		//
		LOGGER.info(txtLines); // System.out.println(txtLines);
		Assert.isTrue(txtLines.length() > 20, ASSERT_MSG);
	}

	@Test void test_getFileLocals() {
		//
		StringBuilder stringBuilder = new StringBuilder();
		String[] fileNames =
			{"booksCatalog.html", "booksCatalog.json", "booksCatalog.xml", "booksXml2Html.xslt"};
		//
		AtomicInteger idx = new AtomicInteger();
		Arrays.stream(fileNames).sequential().forEach(flnm -> {
			String fileName = PATHFILE_LOCAL + flnm;
			String txtLines = UtilityMain.getFileLocal(fileName, "");
			stringBuilder.append(
				String.format("\t%02d %s\tsize: %d \n", idx.incrementAndGet(), flnm, txtLines.length()));
		});
		//
		int countFiles = stringBuilder.toString().split("\n").length;
		System.out.println("countFiles: " + countFiles);
		System.out.println(stringBuilder);
		Assert.isTrue(countFiles == fileNames.length, ASSERT_MSG);
	}

	@Test void test_getJsonValue_fromPath() {

		String txtLine = "";
		String json = UtilityMain.getFileLocal(PATHFILE_LOCAL + "booksCatalog.json", "");
		String jsonPath = "catalog.book[0].price"; // "/catalog/book/0/price";
		//
		// JsonPath.parse(json).read(fieldPath).toString();
		DocumentContext documentContext = JsonPath.parse(json);
		Object object = documentContext.read(jsonPath);
		txtLine = object.toString();
		//
		LOGGER.info("jsonVal: " + txtLine); // System.out.println(txtLines);
		Assert.isTrue(txtLine.equals("44.95"), ASSERT_MSG);
	}

	@Test void test_transformXslt() {
		//
		String txtLines = "";
		String filename_XML = PATHFILE_LOCAL + "booksCatalog" + ".xml";
		String filename_XSL = PATHFILE_LOCAL + "booksXml2Html" + ".xslt";
		String xml = UtilityMain.getFileLocal(filename_XML, "");
		String xsl = UtilityMain.getFileLocal(filename_XSL, "");
		//
		txtLines = UtilityMain.transformXslt(xml, xsl);
		//
		System.out.println(txtLines);
		try { Files.write(Paths.get(PATHFILE_LOCAL + "booksCatalog.html"), txtLines.getBytes(UTF_8)); }
		catch (IOException ex) { ex.printStackTrace(); }
		Assert.isTrue(txtLines.length() > 20, ASSERT_MSG);
	}

	@Test void test_NetworkAddress() throws SocketException { /**/
		//
		// retrieve networkInterface SSIDs
		String txtLines = "";
		Enumeration<NetworkInterface> enums = NetworkInterface.getNetworkInterfaces();
		while (enums.hasMoreElements()) {
			NetworkInterface netIface = enums.nextElement();
			txtLines += getNetIface(netIface, 1);
		}
		System.out.println(txtLines);
		Assert.isTrue(txtLines.split("\n").length >= 7, ASSERT_MSG);
	}

	@Test void test_sampleServer() {
		//
		System.out.println("HttpStatus.OK.value: " + OK.value() + " / " + OK.getReasonPhrase());
	}

	//############
	@Test void testmock_when_thenReturn() {
		//
		String txtLines = "";
		String SAMPLE = "NEWSTRING";
		//
		AnyObject anyObject = new AnyObject();
		AnyObject anyObjectMock = Mockito.mock(AnyObject.class);
		txtLines += String.format("\t aom.getStringValue(): %s \n", anyObjectMock.getStringValue());
		when(anyObjectMock.getStringValue()).thenReturn(SAMPLE);
		//
		txtLines += String.format("\t aob.getStringValue(): %s \n", anyObject.getStringValue());
		txtLines += String.format("\t aom.getStringValue(): %s \n", anyObjectMock.getStringValue());
		System.out.println(txtLines);
		Assert.isTrue(anyObjectMock.getStringValue().equals(SAMPLE), ASSERT_MSG);
	}

	@Test void testmock_doReturn_when() {
		//
		String txtLines = "";
		String SAMPLE = "NEWSTRING";
		//
		AnyObject anyObject = new AnyObject();
		AnyObject anyObjectMock = Mockito.mock(AnyObject.class);
		doReturn(SAMPLE).when(anyObjectMock).getStringValue();
		//
		txtLines += String.format("\t aob.getStringValue(): %s \n", anyObject.getStringValue());
		txtLines += String.format("\t aom.getStringValue(): %s \n", anyObjectMock.getStringValue());
		System.out.println(txtLines);
		Assert.isTrue(anyObjectMock.getStringValue().equals(SAMPLE), ASSERT_MSG);
	}

	@Test void testmock_when_thenThrow() {
		//
		// JUnit4: @Test(expected = IllegalStateException.class)
		String txtLines = "";
		String txtLine = "ORIGINAL";
		String SAMPLE = "ORIGINAL";
		AnyObject anyObject = new AnyObject();
		AnyObject anyObjectMock = Mockito.mock(AnyObject.class);
		try {
			when(anyObjectMock.getStringValue()).thenThrow(AnyException.class);
			txtLine = anyObjectMock.getStringValue(); // should be "ANYSTRING"
		}
		catch (AnyException ex) { LOGGER.info("FORCED AnyException: " + ex.getMessage()); }
		txtLines += String.format("\t aob.getStringValue(): %s \n", anyObject.getStringValue());
		txtLines += String.format("\t aom.getStringValue(): %s \n", txtLine);
		System.out.println(txtLines);
		Assert.isTrue(txtLine.equals(SAMPLE), ASSERT_MSG);
	}

	@Test void testmock_doThrow_when() {
		//
		// JUnit4: @Test(expected = IllegalStateException.class)
		String txtLines = "";
		String SAMPLE = "ANYSTRING";
		AnyObject anyObject = new AnyObject();
		AnyObject anyObjectMock = Mockito.mock(AnyObject.class);
		try {
			doThrow(AnyException.class).when(anyObjectMock).setStringValue(any(String.class));
			anyObjectMock.setStringValue("ANY");
		}
		catch (AnyException ex) { LOGGER.info("FORCED AnyException: " + ex.getMessage()); }
		txtLines += String.format("\t aob.getStringValue(): %s \n", anyObject.getStringValue());
		System.out.println(txtLines);
		Assert.isTrue(anyObject.getStringValue().equals(SAMPLE), ASSERT_MSG);
	}

	@Test void testmock_when_chained() {
		//
		// JUnit4: @Test(expected = IllegalStateException.class)
		String txtLines = "";
		String txtLine = "";
		String SAMPLE = "ORIGINAL";
		AnyObject anyObject = new AnyObject();
		AnyObject anyObjectMock = Mockito.mock(AnyObject.class);
		try {
			when(anyObjectMock.getStringValue())
				.thenReturn(SAMPLE)
				.thenThrow(AnyException.class);
			txtLine += anyObjectMock.getStringValue();
			txtLine += anyObjectMock.getStringValue(); // throws error
		}
		catch (AnyException ex) { LOGGER.info("FORCED AnyException: " + ex.getMessage()); }
		txtLines += String.format("\t aob.getStringValue(): %s \n", anyObject.getStringValue());
		txtLines += String.format("\t aom.getStringValue(): %s \n", txtLine);
		System.out.println(txtLines);
		Assert.isTrue(txtLine.equals(SAMPLE), ASSERT_MSG);
	}

	@Test void testspy_doReturn_when() {
		//
		String txtLines = "";
		String SAMPLE1 = "ONESTRING";
		String SAMPLE2 = "TWOSTRING";
		//
		AnyObject anyObject = new AnyObject();
		AnyObject anyObjectSpy = Mockito.spy(anyObject);
		doReturn(SAMPLE1).when(anyObjectSpy).getStringValue();
		//
		anyObjectSpy.setStringValue("IGNORED");
		txtLines += String.format("\t aos.getStringValue(): %s \n", anyObjectSpy.getStringValue());
		anyObjectSpy.setStrongValue(SAMPLE2);
		txtLines += String.format("\t aos.getStrongValue(): %s \n", anyObjectSpy.getStrongValue());
		System.out.println(txtLines);
		Assert.isTrue(anyObjectSpy.getStringValue().equals(SAMPLE1), ASSERT_MSG);
		Assert.isTrue(anyObjectSpy.getStrongValue().equals(SAMPLE2), ASSERT_MSG);
	}

	//############
	private static String getNetIface(NetworkInterface netIface, int mode) {
		//
		String txtLines = "", address = "";
		try {
			byte[] bytes = netIface.getHardwareAddress();
			if (bytes == null) {address = "[empty!]"; }
			else { address = Base64.getEncoder().encodeToString(bytes);}
			// else{ address = bytes.length + " / " + new String(bytes, UTF_8);}
			if (mode == 0) {
				txtLines += String.format("\t%02d %s\n", netIface.getIndex(), netIface.getDisplayName());
			}
			else {
				txtLines += String.format("\t%02d %04d %6s %s %s\n",
					netIface.getIndex(),
					netIface.getMTU(), // Maximum Transmission Unit (MTU) of this interface
					netIface.getName(),
					address,
					netIface.getDisplayName());
			}
		}
		catch (Exception ex) { LOGGER.info(ex.getMessage()); }
		return txtLines;
	}

	private static void sampleServer() {

		String HOST = "localhost", CONTEXT = "/";
		int PORT = 3000, backlog = 0, threads = 10;
		try {
			InetSocketAddress inetSocketAddress = new InetSocketAddress(HOST, PORT);
			HttpServer httpServer = HttpServer.create(inetSocketAddress, backlog);
			ThreadPoolExecutor TPE = (ThreadPoolExecutor) Executors.newFixedThreadPool(threads);
			AnyHttpHandler anyHttpHandler = new AnyHttpHandler();
			//
			httpServer.createContext(CONTEXT, anyHttpHandler);
			httpServer.setExecutor(TPE);
			httpServer.start();
			LOGGER.info("Server started on port: " + PORT);
		}
		catch (IOException ex) { LOGGER.severe(ex.getMessage());}
	}
}

class AnyException extends IllegalStateException {}

class AnyObject {

	private String stringValue = "";
	private String strongValue = "";
	private String secretValue = "";

	public AnyObject() {
		stringValue = "ANYSTRING";
		strongValue = "OTHSTRING";
		secretValue = "SECRETSTR";
	}

	//
	public String getStringValue() { return stringValue; }

	public String getStrongValue() { return strongValue; }

	private String getSecretValue() { return secretValue; }

	//
	public void setStringValue(String stringValue) { this.stringValue = stringValue; }

	public void setStrongValue(String strongValue) { this.strongValue = strongValue; }

	private void setSecretValue(String secretValue) { this.secretValue = secretValue; }
}

class AnyHttpHandler implements HttpHandler {

	// https://dzone.com/articles/simple-http-server-in-java
	private static final Logger LOGGER = Logger.getLogger(AnyHttpHandler.class.getName());

	@Override public void handle(HttpExchange httpExchange) throws IOException {
		//
		String parm = "";
		String method = httpExchange.getRequestMethod();
		if (HttpMethod.GET.toString().equals(method)) { parm = handleGets(httpExchange); }
		if (HttpMethod.POST.toString().equals(method)) { parm = handlePost(httpExchange); }
		handleResponse(httpExchange, parm);
	}

	private String handleGets(HttpExchange httpExchange) {
		//
		String parm = "";
		String uri = httpExchange.getRequestURI().toString();
		if (uri.contains("?")) {parm = uri.split("\\?")[1];}
		if (uri.contains("=")) {parm = uri.split("=")[1];}
		return parm;
	}

	private String handlePost(HttpExchange httpExchange) {
		return "";
	}

	private void handleResponse(HttpExchange httpExchange, String parm) throws IOException {
		//
		OutputStream outputStream = httpExchange.getResponseBody();
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("<html><style>body { font-family: verdana; }</style><body><center>");
		stringBuilder.append("<h3>Greetings, " + parm + "!</h3>");
		stringBuilder.append("<h5>" + Instant.now() + "</h5>");
		stringBuilder.append("</center></body></html>");
		try {
			// encode HTML content
			String htmlResponse = stringBuilder.toString(); // StringEscapeUtils.escapeHtml4(...)
			httpExchange.sendResponseHeaders(OK.value(), htmlResponse.length());
			outputStream.write(htmlResponse.getBytes());
			outputStream.flush();
			outputStream.close();
		}
		catch (IOException ex) { LOGGER.severe(ex.getMessage());}
	}
}
//----
