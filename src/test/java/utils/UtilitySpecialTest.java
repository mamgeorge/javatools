package utils;

import edu.cmu.sphinx.api.Configuration;
import edu.cmu.sphinx.api.LiveSpeechRecognizer;
import edu.cmu.sphinx.api.SpeechResult;
import edu.cmu.sphinx.api.StreamSpeechRecognizer;
import edu.cmu.sphinx.result.WordResult;
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
import org.apache.commons.collections4.list.TreeList;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.Enumeration;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static utils.UtilityMain.EOL;

public class UtilitySpecialTest {

	static final String PATHFILE_LOCAL = "src/test/resources/";
	static final String ASSERT_MSG = "ASSERT_MSG";

	@Test void test_transformXslt( ) {
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
		catch (IOException ex) { System.out.println("ERROR: " + ex.getMessage()); }
		assertTrue(txtLines.length() > 20, ASSERT_MSG);
	}

	@Test void test_NetworkAddress( ) { /**/
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
		catch (IOException ex) { System.out.println("ERROR: " + ex.getMessage()); }
		//
		txtLines += "NetworkInterface SSIDs" + EOL;
		List<String> list = new TreeList<>();
		Enumeration<NetworkInterface> enums = null;
		try { enums = NetworkInterface.getNetworkInterfaces(); }
		catch (SocketException ex) { System.out.println("ERROR: " + ex.getMessage()); }
		while ( enums.hasMoreElements() ) {
			NetworkInterface netIface = enums.nextElement();
			list.add(getNetIface(netIface, 1));
		}
		//
		StringBuilder stringBuilder = new StringBuilder();
		for ( String txt : list ) stringBuilder.append(txt);
		txtLines += stringBuilder.toString();
		System.out.println(txtLines);
		assertTrue(list.size() >= 7, ASSERT_MSG);
	}

	@Test void test_sphinx4_STT( ) {
		//
		// https://cmusphinx.github.io/wiki/tutorialsphinx4/
		String pathFile = PATHFILE_LOCAL + "hal9000.wav";
		//
		Configuration configuration = getSphinxConfig();
		StreamSpeechRecognizer ssRecognizer;
		SpeechResult speechResult = null;
		try {
			ssRecognizer = new StreamSpeechRecognizer(configuration);
			File file = new File(pathFile);
			InputStream FIS = new FileInputStream(file);
			ssRecognizer.startRecognition(FIS);
			while ( ( speechResult = ssRecognizer.getResult() ) != null ) {
				System.out.format("Hypothesis: %s\n", speechResult.getHypothesis());
			}
			ssRecognizer.stopRecognition();
		}
		catch (IOException ex) { System.out.println("ERROR: " + ex.getMessage()); }
		assertNotNull(speechResult, ASSERT_MSG);
	}

	@Test void test_sphinx4_TTS_SS( ) {
		//
		// https://cmusphinx.github.io/wiki/tutorialsphinx4/
		String pathFile = PATHFILE_LOCAL + "hal9000.wav";
		//
		Configuration configuration = getSphinxConfig();
		StreamSpeechRecognizer ssRecognizer ;
		SpeechResult speechResultSS = null;
		try {
			FileInputStream FIS = new FileInputStream(pathFile);
			ssRecognizer = new StreamSpeechRecognizer(configuration);
			ssRecognizer.startRecognition(FIS);
			//
			speechResultSS = ssRecognizer.getResult();
			ssRecognizer.stopRecognition();
			System.out.println(speechResultSS.getHypothesis());
			for ( WordResult wordResult : speechResultSS.getWords()) {
				System.out.println(wordResult);
			}
		}
		catch (IOException ex) { System.out.println("ERROR: " + ex.getMessage()); }
		assertNotNull(speechResultSS, ASSERT_MSG);
	}

	@Test void test_sphinx4_TTS_LS( ) {
		//
		// https://cmusphinx.github.io/wiki/tutorialsphinx4/
		// String pathFile = PATHFILE_LOCAL + "hal9000.wav";
		//
		Configuration configuration = getSphinxConfig();
		LiveSpeechRecognizer lsRecognizer ;
		SpeechResult speechResultLS = null;
		try {
			// InputStream FIS = new FileInputStream(pathFile);
			lsRecognizer = new LiveSpeechRecognizer(configuration);
			lsRecognizer.startRecognition(true);
			//
			speechResultLS = lsRecognizer.getResult();
			lsRecognizer.stopRecognition();
			System.out.println(speechResultLS.getHypothesis());
			for ( WordResult wordResult : speechResultLS.getWords()) {
				System.out.println(wordResult);
			}
		}
		catch (IOException ex) { System.out.println("ERROR: " + ex.getMessage()); }
		assertNotNull(speechResultLS, ASSERT_MSG);
	}

	@Test void test_JaegerClient_tracing( ) {
		//
		String[] ENVVAR = { "FOO", "USERNAME" };
		io.jaegertracing.Configuration configuration = new io.jaegertracing.Configuration(ENVVAR[0]);
		Tracer tracer = configuration.getTracer();
		String txtLines = UtilityMain.exposeObject(tracer);
		System.out.println(txtLines);
		assertTrue(txtLines.split(EOL).length >= 20, ASSERT_MSG);
	}

	@Test void test_K8Controller( ) {
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
					(workQueue) -> ControllerBuilder.controllerWatchBuilder(V1Node.class,
							workQueue)
						.build())
				.withReconciler(NPR) // required, set the actual reconciler
				.withName(
					"node-printing-controller") // optional, set name for logging, thread-tracing
				.withWorkerCount(4) // optional, set worker thread count
				.withReadyFunc(SII::hasSynced) // optional, only starts when cache synced up
				.build();
		//
		System.out.println(controller.toString());
		// controller.run();
	}

	//#### statics
	private static String getNetIface(NetworkInterface netIface, int mode) {
		//
		String txtLines = "", address;
		try {
			byte[] bytes = netIface.getHardwareAddress();
			if ( bytes == null ) {
				address = "[empty!]";
			} else {
				address = Base64.getEncoder().encodeToString(bytes);
			}
			// else{ address = bytes.length + " / " + new String(bytes, UTF_8);}
			if ( mode == 0 ) {
				txtLines += String.format("\t%02d %s\n", netIface.getIndex(), netIface.getDisplayName());
			} else {
				if ( bytes != null ) {
					txtLines += String.format("\t%6s %2d %04d %s %s\n",
						netIface.getName(),
						netIface.getIndex(),
						netIface.getMTU(), // Maximum Transmission Unit (MTU) of this interface
						address,
						netIface.getDisplayName());
				}
			}
		}
		catch (IOException ex) { System.out.println("ERROR: " + ex.getMessage()); }
		return txtLines;
	}

	private static Configuration getSphinxConfig( ) {
		//
		String pathAcousticModel = "resource:/edu/cmu/sphinx/models/en-us/en-us";
		String pathDictionary = "resource:/edu/cmu/sphinx/models/en-us/cmudict-en-us.dict";
		String pathLanguageModel = "resource:/edu/cmu/sphinx/models/en-us/en-us.lm.bin";
		//
		Configuration configuration = new Configuration();
		configuration.setAcousticModelPath(pathAcousticModel);
		configuration.setDictionaryPath(pathDictionary);
		configuration.setLanguageModelPath(pathLanguageModel);
		return configuration;
	}
}