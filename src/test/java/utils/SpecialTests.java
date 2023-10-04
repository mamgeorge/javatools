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

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static utils.UtilityMainTest.ASSERT_MSG;
import static utils.UtilityMainTest.PATHFILE_LOCAL;

class SpecialTests {

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
		assertTrue(intVal > -1);
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

	@Test void listFiles( ) {

		// C:\Users\mamge\OneDrive\Documents\5Personal\History\images_levant_extra
		String folderPath = "C:/Users/mamge/OneDrive/Documents/"
			+ "5Personal/History/images_levant10";

		File folder = new File(folderPath);
		File[] files = folder.listFiles();

		for ( File file : files ) {
			if ( file.isFile() ) { System.out.println("File " + file.getName()); } else {
				System.out.println("file is: " + file);
			}
		}

		assertNotNull(files);
	}

	//#### statics
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