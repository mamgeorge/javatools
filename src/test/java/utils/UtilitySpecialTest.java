package utils;

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
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.logging.Logger;
import org.apache.commons.collections4.list.TreeList;
import org.junit.jupiter.api.Test;
import org.springframework.util.Assert;
import samples.AnyHttpHandler;

import static java.nio.charset.StandardCharsets.UTF_8;

import static org.springframework.http.HttpStatus.OK;
import static utils.UtilityMain.EOL;

public class UtilitySpecialTest {

	static final Logger LOGGER = Logger.getLogger(UtilitySpecialTest.class.getName());
	static final String PATHFILE_LOCAL = "src/test/resources/";
	static final String ASSERT_MSG = "ASSERT_MSG";

	@Test void test_transformXslt() {
		//
		String filename_XML = PATHFILE_LOCAL + "booksCatalog" + ".xml";
		String filename_XSL = PATHFILE_LOCAL + "booksXml2Html" + ".xslt";
		String xml = UtilityMain.getFileLocal(filename_XML);
		String xsl = UtilityMain.getFileLocal(filename_XSL);
		//
		String txtLines = UtilityMain.transformXslt(xml, xsl);
		//
		System.out.println(txtLines);
		try { Files.write(Paths.get(PATHFILE_LOCAL + "booksCatalog.html"), txtLines.getBytes(UTF_8)); }
		catch (IOException ex) { ex.printStackTrace(); }
		Assert.isTrue(txtLines.length() > 20, ASSERT_MSG);
	}

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
		for (String txt : list) stringBuilder.append(txt);
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
		ApiCallback<?> apiCallback = null;
		//
		SharedIndexInformer<V1Node> SII =
			SIF.sharedIndexInformerFor(
				(CallGeneratorParams params) -> coreV1Api.listNodeCall(
					pretty, allowWatchBookmarks, continues, fieldSelector, labelSelector, limit,
					params.resourceVersion, resourceVersionMatch,
					timeoutSeconds, watch, apiCallback),
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

	//#### statics
	private static String getNetIface(NetworkInterface netIface, int mode) {
		//
		String txtLines = "", address;
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