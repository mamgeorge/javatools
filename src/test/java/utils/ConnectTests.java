package utils;

import org.junit.jupiter.api.Test;

import static utils.UtilityMain.urlGet;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static utils.UtilityMain.urlPost;

class ConnectTests {

	private static final String URL_VAL = "https://mamgeorge.altervista.org";

	@Test void test_urlGet( ) {

		String html = urlGet(URL_VAL);
		System.out.println("get: " + html);
		assertTrue(html.contains("DOCTYPE"));
	}

	@Test void test_urlPost( ) {

		String postParms = "";

		String html = urlPost(URL_VAL, postParms);
		System.out.println("post: " + html);
		assertTrue(html.contains("DOCTYPE"));
	}
}
