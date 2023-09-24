package utils;

import org.junit.jupiter.api.Test;

import static utils.UtilityMain.urlGet;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static utils.UtilityMain.urlPost;

class ConnectTests {

	@Test void test_urlGet( ) {

		String url = "https://mamgeorge.altervista.org";

		String html = urlGet(url);
		System.out.println("get: " + html);
		assertTrue(html.contains("DOCTYPE"));
	}

	@Test void test_urlPost( ) {

		String url = "https://mamgeorge.altervista.org";
		String postParms = "";

		String html = urlPost(url, postParms);
		System.out.println("podt: " + html);
		assertTrue(html.contains("DOCTYPE"));
	}
}
