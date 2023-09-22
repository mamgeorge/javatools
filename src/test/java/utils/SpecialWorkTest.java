package utils;

import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class SpecialWorkTest {

	@Test void listFiles( ) {

		// C:\Users\mamge\OneDrive\Documents\5Personal\History\images_levant_extra
		String folderPath = "C:/Users/mamge/OneDrive/Documents/"
			+ "5Personal/History/images_levant10";

		File folder = new File(folderPath);
		File[] files = folder.listFiles();

		for ( File file : files ) {
			if ( file.isFile() )
			{ System.out.println("File " + file.getName()); } else
			{ System.out.println("file is: " + file); }
		}

		assertNotNull(files);
	}
}
