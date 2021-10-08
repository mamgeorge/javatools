package utils;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import static java.nio.charset.StandardCharsets.UTF_8;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.logging.Logger;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpMethod;
import org.springframework.util.Assert;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.OK;

public class UtilityMainTest {

	private static final Logger LOGGER = Logger.getLogger(UtilityMainTest.class.getName());
	private static final String PATHFILE_LOCAL = "src/test/resources/";
	private static final String ASSERT_MSG = "ASSERT_MSG";

	@Test public void test_showSys() {

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

	@Test public void test_showTime() {

		String showTime = UtilityMain.showTime();
		String instantNow = Instant.now().toString();
		//
		System.out.println("showTime: " + showTime);
		System.out.println("instantNow: " + instantNow);
		Assert.isTrue(showTime.length() > 20, ASSERT_MSG);
		Assert.isTrue(instantNow.length() > 28, ASSERT_MSG);
	}

	@Test public void test_showTimes() {

		String showTimes = UtilityMain.showTimes();
		//
		int showTimesLen = showTimes.split("\n").length;
		System.out.println("showTimes: " + showTimes);
		System.out.println("showTimesLen: " + showTimesLen);
		Assert.isTrue(showTimesLen > 4, ASSERT_MSG);
	}

	//############
	@Test public void test_getFileLines() {

		String txtLines = "";
		txtLines = UtilityMain.getFileLines("c:/workspace/greetings.txt", "");
		//
		LOGGER.info(txtLines); // System.out.println(txtLines);
		Assert.isTrue(txtLines.length() > 12, ASSERT_MSG);
	}

	@Test public void test_getFileLocal() {

		String txtLines = "";
		txtLines = UtilityMain.getFileLocal(PATHFILE_LOCAL + "booksCatalog.json", "");
		//
		LOGGER.info(txtLines); // System.out.println(txtLines);
		Assert.isTrue(txtLines.length() > 20, ASSERT_MSG);
	}

	@Test public void test_getJsonValue_fromPath() {

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

	@Test public void test_transformXslt() {
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
		try { Files.write(Paths.get(PATHFILE_LOCAL+"booksCatalog.html"), txtLines.getBytes(UTF_8)); }
		catch (IOException ex) { ex.printStackTrace(); }
		Assert.isTrue(txtLines.length() > 20, ASSERT_MSG);
	}

	@Test public void test_sampleServer() {
		//
		System.out.println("HttpStatus.OK.value: " + OK.value() + " / " + OK.getReasonPhrase());
	}

	//############
	@Test public void testmock_when_thenReturn() {
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

	@Test public void testmock_doReturn_when() {
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

	@Test public void testmock_when_thenThrow() {
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

	@Test public void testmock_doThrow_when() {
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

	@Test public void testmock_when_chained() {
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

	@Test public void testspy_doReturn_when() {
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
	public static void sampleServer() {

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

	public AnyObject() {
		stringValue = "ANYSTRING";
		strongValue = "OTHSTRING";
	}

	//
	public String getStringValue() { return stringValue; }

	public String getStrongValue() { return strongValue; }

	//
	public void setStringValue(String stringValue) { this.stringValue = stringValue; }

	public void setStrongValue(String strongValue) { this.strongValue = strongValue; }
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
