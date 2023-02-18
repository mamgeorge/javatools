package utils;

import edu.cmu.sphinx.api.Configuration;
import edu.cmu.sphinx.api.LiveSpeechRecognizer;
import edu.cmu.sphinx.api.SpeechResult;
import edu.cmu.sphinx.api.StreamSpeechRecognizer;
import edu.cmu.sphinx.result.WordResult;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Enumeration;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static utils.UtilityMain.EOL;

public class UtilitySpecialTest {

	static final String PATHFILE_LOCAL = "src/test/resources/";
	static final String ASSERT_MSG = "ASSERT_MSG";

	@Test void test_error( ) {
		//
		int intVal = 0;
		try { intVal = 1 / 0; }
		catch (ArithmeticException ex) {
			System.out.println("ERROR ex.toString(): " + ex);
			System.out.println("ERROR ex.getMessage(): " + ex.getMessage());
			System.out.println("ERROR ex.getLocalizedMessage(): " + ex.getLocalizedMessage());
			//
			String txtLines = "";
			int ictr = 0;
			StackTraceElement[] STEs = ex.getStackTrace();
			System.out.println("ERROR ex.getStackTrace(): " + STEs.length);
			for ( StackTraceElement STE : STEs ) {
				txtLines += String.format("\t %02d %s\n", ++ictr, STE.toString());
			}
			System.out.println(txtLines);
			// ex.printStackTrace();
		}
		System.out.println("intVal: " + intVal);
		assertNotNull(intVal);
	}

	@Test void test_UUID( ) {
		//
		String txtLines = "";
		byte[] bytes = "1234".getBytes();
		txtLines += "base64 getEncoder: " + Base64.getEncoder().encodeToString(bytes) + EOL;
		txtLines += "base64 getUrlEncoder: " + Base64.getUrlEncoder().encodeToString(bytes) + EOL;
		txtLines += "base64 getMimeEncoder: " + Base64.getMimeEncoder().encodeToString(bytes) + EOL;
		txtLines += new String(new char[20]).replace('\u0000', '-') + EOL;
		//
		UUID uuid = UUID.randomUUID();
		txtLines += "UUID randomUUID(): " + uuid + EOL;
		txtLines += "UUID replace toUpperCase: " + uuid.toString().replace("-", "").toUpperCase() + EOL;
		try {
			txtLines += "UUID fromString(): " + UUID.fromString("3bf121bc-14e9-45fa-9b38-b264759eb233") + EOL;
		}
		catch (IllegalArgumentException ex) { System.out.println("ERROR: " + ex.getMessage()); }
		//
		System.out.println(txtLines);
		assertNotNull(txtLines);
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
		List<String> list = new ArrayList<>();
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

	@Test @Disabled( "too slow" ) void test_sphinx4_STT( ) {
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

	@Test @Disabled( "too slow" ) void test_sphinx4_TTS_SS( ) {
		//
		// https://cmusphinx.github.io/wiki/tutorialsphinx4/
		String pathFile = PATHFILE_LOCAL + "hal9000.wav";
		//
		Configuration configuration = getSphinxConfig();
		StreamSpeechRecognizer ssRecognizer;
		SpeechResult speechResultSS = null;
		try {
			FileInputStream FIS = new FileInputStream(pathFile);
			ssRecognizer = new StreamSpeechRecognizer(configuration);
			ssRecognizer.startRecognition(FIS);
			//
			speechResultSS = ssRecognizer.getResult();
			ssRecognizer.stopRecognition();
			System.out.println(speechResultSS.getHypothesis());
			for ( WordResult wordResult : speechResultSS.getWords() ) {
				System.out.println(wordResult);
			}
		}
		catch (IOException ex) { System.out.println("ERROR: " + ex.getMessage()); }
		assertNotNull(speechResultSS, ASSERT_MSG);
	}

	@Test @Disabled( "too slow" ) void test_sphinx4_TTS_LS( ) {
		//
		// https://cmusphinx.github.io/wiki/tutorialsphinx4/
		// String pathFile = PATHFILE_LOCAL + "hal9000.wav";
		//
		Configuration configuration = getSphinxConfig();
		LiveSpeechRecognizer lsRecognizer;
		SpeechResult speechResultLS = null;
		try {
			// InputStream FIS = new FileInputStream(pathFile);
			lsRecognizer = new LiveSpeechRecognizer(configuration);
			lsRecognizer.startRecognition(true);
			//
			speechResultLS = lsRecognizer.getResult();
			lsRecognizer.stopRecognition();
			System.out.println(speechResultLS.getHypothesis());
			for ( WordResult wordResult : speechResultLS.getWords() ) {
				System.out.println(wordResult);
			}
		}
		catch (IOException ex) { System.out.println("ERROR: " + ex.getMessage()); }
		assertNotNull(speechResultLS, ASSERT_MSG);
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