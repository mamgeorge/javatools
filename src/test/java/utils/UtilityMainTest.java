package utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import io.jaegertracing.Configuration;
import io.kubernetes.client.extended.controller.Controller;
import io.kubernetes.client.extended.controller.builder.ControllerBuilder;
import io.kubernetes.client.extended.controller.reconciler.Reconciler;
import io.kubernetes.client.extended.controller.reconciler.Request;
import io.kubernetes.client.extended.controller.reconciler.Result;
import io.kubernetes.client.informer.SharedIndexInformer;
import io.kubernetes.client.informer.SharedInformerFactory;
import io.kubernetes.client.informer.cache.Lister;
import io.kubernetes.client.openapi.ApiCallback;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1Node;
import io.kubernetes.client.openapi.models.V1NodeList;
import io.kubernetes.client.util.CallGeneratorParams;
import io.opentracing.Tracer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
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
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections4.list.TreeList;
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
import static utils.UtilityMain.EOL;
import static utils.UtilityMain.TAB;

public class UtilityMainTest {

	static final Logger LOGGER = Logger.getLogger(UtilityMainTest.class.getName());
	static final String PATHFILE_LOCAL = "src/test/resources/";
	static final String ASSERT_MSG = "ASSERT_MSG";

	//#### basics
	@Test void test_template() {
		//
		StringBuilder stringBuilder = new StringBuilder();
		//
		stringBuilder.append("template");
		//
		String txtLines = stringBuilder.toString();
		System.out.println("txtLines: " + txtLines);
		Assert.isTrue(txtLines.split(EOL).length >= 1, ASSERT_MSG);
	}

	@Test void test_booleans() {
		//
		String txtLines = EOL, expected = "";
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
		Assert.isTrue(txtLines.length() >= 1, ASSERT_MSG);
	}

	@Test void test_showSys() {

		String txtLines = "";
		txtLines = UtilityMain.showSys();
		String[] envr = txtLines.split(",");
		//
		StringBuilder stringBuilder = new StringBuilder();
		Arrays.stream(envr).forEach(str -> stringBuilder.append(str + EOL));
		//
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
		int showTimesLen = showTimes.split(EOL).length;
		System.out.println("showTimes: " + showTimes);
		System.out.println("showTimesLen: " + showTimesLen);
		Assert.isTrue(showTimesLen > 4, ASSERT_MSG);
	}

	@Test void test_Stream_sort() throws SocketException {
		//
		Set<String> set = new TreeSet<>();
		Enumeration<NetworkInterface> enums = NetworkInterface.getNetworkInterfaces();
		//
		Collections.list(enums).stream().forEach(nifc -> set.add(TAB + nifc.getDisplayName()));
		//
		System.out.println("Stream & sort" + TAB + "size: " + set.size());
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
		listFilt.forEach(nifc -> set.add(TAB + nifc.getDisplayName()));
		//
		System.out.println("Stream & filter" + TAB + "size: " + set.size());
		set.stream().forEach(System.out::println);
		Assert.isTrue(set.size() >= 5, ASSERT_MSG);
	}

	@Test void test_Stream_Collections() throws SocketException {
		//
		StringBuilder stringBuilder = new StringBuilder();
		Enumeration<NetworkInterface> enums = NetworkInterface.getNetworkInterfaces();
		// stringBuilder.delete(0, stringBuilder.length()).append("Iterator Stream" + EOL);
		//
		stringBuilder.append("Collections Stream" + EOL);
		Collections.list(enums).stream().forEach(nifc -> stringBuilder.append(getNetIface(nifc, 0)));
		//
		System.out.println(stringBuilder);
		Assert.isTrue(stringBuilder.toString().split(EOL).length >= 7, ASSERT_MSG);
	}

	@Test void test_Stream_Iterator() throws SocketException {
		// better for larger numbers
		StringBuilder stringBuilder = new StringBuilder();
		Enumeration<NetworkInterface> enums = NetworkInterface.getNetworkInterfaces();
		Iterator iterator = NetworkInterface.getNetworkInterfaces().asIterator();
		//
		stringBuilder.append("Iterator Stream" + EOL);
		Stream<NetworkInterface> stream = StreamSupport.stream(
			Spliterators.spliteratorUnknownSize(iterator, Spliterator.ORDERED), false);
		stream.forEach(nifc -> stringBuilder.append(getNetIface(nifc, 0)));
		//
		System.out.println(stringBuilder);
		Assert.isTrue(stringBuilder.toString().split(EOL).length >= 7, ASSERT_MSG);
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

	@Test void test_exposeObject() {
		//
		AnyObject anyObject = new AnyObject();
		String txtLines = UtilityMain.exposeObject(anyObject);
		System.out.println(txtLines);
		Assert.isTrue(txtLines.split(EOL).length >= 6, ASSERT_MSG);
	}

	@Test void test_putObject() {
		//
		AnyObject anyObject = new AnyObject();
		UtilityMain.putObject(anyObject, "secretValue", "STUFF");
		String txtLine = UtilityMain.getField(anyObject, "secretValue");
		System.out.println(txtLine);
		Assert.isTrue(txtLine.equals("STUFF"), ASSERT_MSG);
	}

	//#### files
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
		int countFiles = stringBuilder.toString().split(EOL).length;
		System.out.println("countFiles: " + countFiles);
		System.out.println(stringBuilder);
		Assert.isTrue(countFiles == fileNames.length, ASSERT_MSG);
	}

	@Test void test_objectMapper() {
		//
		String txtLine = "";
		String JSON_PATH = "/catalog/book/0/author";
		String PATHFILE_LOCALJSON = PATHFILE_LOCAL + "booksCatalog.json";
		String body = UtilityMain.getFileLocal(PATHFILE_LOCALJSON, "");
		try {
			ObjectMapper objectMapper = new ObjectMapper();
			JsonNode jsonNodeRoot = objectMapper.readTree(body);
			JsonNode jsonNodeAt = jsonNodeRoot.at(JSON_PATH);
			txtLine = jsonNodeAt.asText();
			// txtLine = jsonNodeRoot.at(JsonPointer.compile(jsonPath)).asText();
		}
		catch (JsonProcessingException ex) { LOGGER.severe(ex.getMessage()); }
		//
		System.out.println("jsonNodeAt.asText(): " + txtLine);
		Assert.isTrue(txtLine.equals("Gambardella , Matthew"), ASSERT_MSG);
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

	//#### special
	@Test void test_NetworkAddress() throws SocketException { /**/
		//
		String txtLines = EOL + "InetAddress" + EOL;
		try {
			InetAddress INA = InetAddress.getLocalHost();
			txtLines += String.format(
				"\tlocalhost: %s\n\t" + "hostName : %s\n\t" + "hostAddr : %s\n\t" + "canonical: %s\n\n",
				INA.toString(),
				INA.getHostName(),
				INA.getHostAddress(),
				INA.getCanonicalHostName());
		}
		catch (Exception ex) { LOGGER.info(ex.getMessage()); }
		//
		txtLines += "NetworkInterface SSIDs" + EOL;
		List<String> list = new TreeList<>();
		Enumeration<NetworkInterface> enums = NetworkInterface.getNetworkInterfaces();
		while (enums.hasMoreElements()) {
			NetworkInterface netIface = enums.nextElement();
			list.add(getNetIface(netIface, 1));
		}
		//
		StringBuilder stringBuilder = new StringBuilder();
		list.stream().sorted()
			.forEach(txt -> stringBuilder.append(txt));
		txtLines += stringBuilder.toString();
		System.out.println(txtLines);
		Assert.isTrue(list.size() >= 7, ASSERT_MSG);
	}

	@Test void testDB_read() {
		//
		DbProfile DPB = DbProfile.init(DbProfile.DATABASES.sqlite);
		DPB.setSql("SELECT FirstName as FIRST, LastName as LAST, Email FROM  customers " +
			"WHERE State = 'CA' ORDER BY LastName ASC");
		//
		String txtLines = DPB.dbRead();
		//
		System.out.println("txtLines: " + txtLines);
		Assert.isTrue(txtLines.split(EOL).length >= 1, ASSERT_MSG);
	}

	@Test void test_JaegerClient_tracing() {
		//
		String[] ENVVAR = {"FOO", "USERNAME"};
		Configuration configuration = new Configuration(ENVVAR[0]);
		Tracer tracer = configuration.getTracer();
		String txtLines = UtilityMain.exposeObject(tracer);
		System.out.println(txtLines);
		Assert.isTrue(txtLines.split(EOL).length >= 20, ASSERT_MSG);
	}

	@Test void test_K8Controller() {
		//
		// JIB, Dekorate, JKube
		// io.kubernetes:client-java-extended:6.0.1
		SharedInformerFactory SIF = new SharedInformerFactory();
		CoreV1Api coreV1Api = new CoreV1Api();
		String pretty = "", continues = "", fieldSelector = "", labelSelector = "",
			resourceVersionMatch =
				"";
		int limit = 0;
		boolean allowWatchBookmarks = true, watch = true;
		Integer timeoutSeconds = null;
		ApiCallback apiCallback = null;
		//
		SharedIndexInformer<V1Node> SII =
			SIF.sharedIndexInformerFor(
				(CallGeneratorParams params) -> {
					return coreV1Api.listNodeCall(
						pretty, allowWatchBookmarks, continues, fieldSelector, labelSelector, limit,
						params.resourceVersion, resourceVersionMatch,
						timeoutSeconds, watch, apiCallback);
				},
				V1Node.class, V1NodeList.class);
		SIF.startAllRegisteredInformers();
		//
		class NodePrintingReconciler implements Reconciler {
			//
			final Lister<V1Node> listerV1Node;

			//
			public NodePrintingReconciler(SharedIndexInformer<V1Node> nodeInformer) {
				this.listerV1Node = new Lister<>(nodeInformer.getIndexer());
			}

			//
			@Override public Result reconcile(Request request) {
				V1Node v1Node = this.listerV1Node.get(request.getName());
				System.out.println("triggered reconciling " + v1Node.getMetadata().getName());
				return new Result(false);
			}
		}
		//
		NodePrintingReconciler NPR = new NodePrintingReconciler(SII);
		//
		Controller controller =
			ControllerBuilder.defaultBuilder(SIF)
				.watch(
					(workQueue) -> ControllerBuilder.controllerWatchBuilder(V1Node.class, workQueue)
						.build())
				.withReconciler(NPR) // required, set the actual reconciler
				.withName("node-printing-controller") // optional, set name for logging, thread-tracing
				.withWorkerCount(4) // optional, set worker thread count
				.withReadyFunc(SII::hasSynced) // optional, only starts when cache synced up
				.build();

		// controller.run();
	}

	@Test void test_sampleServer() {
		//
		System.out.println("HttpStatus.OK.value: " + OK.value() + " / " + OK.getReasonPhrase());
	}

	//#### mock tests
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

	//#### statics
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
				if (bytes != null) {
					txtLines += String.format("\t%6s %2d %04d %s %s\n",
						netIface.getName(),
						netIface.getIndex(),
						netIface.getMTU(), // Maximum Transmission Unit (MTU) of this interface
						address,
						netIface.getDisplayName());
				}
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

/*
	add 4 "lombok" refs & plugin to build?
	add "lombok.config" with "lombok.addLombokGeneratedAnnotation=true"?
	add @Getter @Setter @EqualsAndHashCode @NoArgsConstructor to POJO
*/
@Getter @Setter @EqualsAndHashCode
class DbProfile {
	//
	public enum DATABASES {sqlite, derby, mysql, oracle, cassandra}

	private static final Logger LOGGER = Logger.getLogger(DbProfile.class.getName());
	private String className;
	private String connection;
	public String sql;

	private DbProfile dbProfile;

	private DbProfile(String className, String connection, String sql) {
		this.className = className;
		this.connection = connection;
		this.sql = sql;
	}

	public static DbProfile init(DATABASES dbtype) {
		//
		String className = "";
		String connection = "";
		String sql = "";
		switch (dbtype) {
			case sqlite:
				className = "org.sqlite.JDBC";
				connection = "jdbc:sqlite:C:/dbase/sqlite/chinook.db";
				sql = "SELECT * FROM customers WHERE Country = 'USA' ORDER BY LastName ASC";
				break;
			case derby:
				className = "org.apache.derby.jdbc.ClientDriver";
				connection = "jdbc:derby://localhost:1527/testDb";
				sql = "";
				break;
			case mysql:
				className = "com.mysql.jdbc.Driver";
				connection = "jdbc:mysql://ohit014:3306/db";
				sql = "";
				break;
			case oracle:
				className = "oracle.jdbc.driver.OracleDriver";
				connection = "jdbc:oracle:thin:@localhost:1521:db";
				sql = "";
				break;
			case cassandra:
				className = "";
				connection = "";
				sql = "";
				break;
		}
		DbProfile dbProfile = new DbProfile(className, connection, sql);
		return dbProfile;
	}

	public String dbRead() {
		//
		StringBuilder stringBuilder = new StringBuilder();
		String DLM = ",\t";
		try {
			Class.forName(getClassName());
			Connection connection = DriverManager.getConnection(getConnection());
			Statement statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery(getSql());
			ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
			int intColumnCount = resultSetMetaData.getColumnCount();
			// get column titles
			stringBuilder.append(EOL);
			for (int ictr = 1; ictr < intColumnCount + 1; ictr++) {
				stringBuilder.append(resultSetMetaData.getColumnName(ictr) + TAB);
			}
			stringBuilder.append(EOL);
			// get rows
			Object object = null;
			while (resultSet.next()) {
				//
				stringBuilder.append(TAB);
				for (int ictr = 1; ictr < intColumnCount + 1; ictr++) {
					object = resultSet.getObject(ictr);
					if (object instanceof Clob) { object = object.getClass().getName(); }
					if (object == null) { object = "NULL"; }
					if (ictr < intColumnCount) { stringBuilder.append(object + DLM); }
					else {stringBuilder.append(object);}
				}
				stringBuilder.append(EOL);
			}
		}
		catch (ClassNotFoundException | SQLException ex) { LOGGER.info(ex.getMessage());}
		return stringBuilder.toString();
	}
}

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

class AnyException extends IllegalStateException {}

//----
