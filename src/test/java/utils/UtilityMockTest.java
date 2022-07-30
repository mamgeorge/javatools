package utils;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.powermock.reflect.Whitebox;
import org.springframework.test.util.ReflectionTestUtils;
import samples.AnyException;
import samples.AnyObject;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class UtilityMockTest {

	static final Logger LOGGER = Logger.getLogger(UtilityMockTest.class.getName());
	static final String ASSERT_MSG = "ASSERT_MSG";
	static final String SAMPLE = "OMEGA";
	static final String EOL = "\n";

	@Test void testmock_when_thenReturn( ) {
		//
		String results = EOL;
		//
		AnyObject anyObject = new AnyObject();
		AnyObject anyObjectMock = Mockito.mock(AnyObject.class);
		when(anyObjectMock.getAlpha()).thenReturn(SAMPLE);
		//
		results += String.format("\t aob.getAlpha(): %s \n", anyObject.getAlpha());
		results += String.format("\t aom.getAlpha(): %s \n", anyObjectMock.getAlpha());
		System.out.println("results: " + results);
		assertEquals(SAMPLE, anyObjectMock.getAlpha(), ASSERT_MSG);
	}

	@Test void testmock_doReturn_when( ) {
		//
		String results = EOL;
		//
		AnyObject anyObject = new AnyObject();
		AnyObject anyObjectMock = Mockito.mock(AnyObject.class);
		doReturn(SAMPLE).when(anyObjectMock).getAlpha();
		//
		results += String.format("\t aob.getAlpha(): %s \n", anyObject.getAlpha());
		results += String.format("\t aom.getAlpha(): %s \n", anyObjectMock.getAlpha());
		System.out.println("results: " + results);
		assertEquals(SAMPLE, anyObjectMock.getAlpha(), ASSERT_MSG);
	}

	@Test void testmock_doNothing_when( ) {
		//
		List<String> listReal = new ArrayList<>();
		listReal.add(0, "alpha");
		//
		List<String> listMock = Mockito.mock(ArrayList.class);
		doNothing().when(listMock).add(isA(Integer.class), isA(String.class));
		listMock.add(0, "beta");
		//
		System.out.println(listReal);
		System.out.println(listMock);
		verify(listMock, times(1)).add(0, "beta");
	}

	@Test void testmock_when_thenThrow( ) {
		//
		// JUnit4: @Test(expected = IllegalStateException.class)
		String results = EOL;
		AnyObject anyObject = new AnyObject();
		AnyObject anyObjectMock = Mockito.mock(AnyObject.class);
		when(anyObjectMock.getAlpha()).thenThrow(AnyException.class);
		results += String.format("\t aob.getStringValue(): %s \n", anyObject.getAlpha());
		try { results += String.format("\t aom.getStringValue(): %s \n", anyObjectMock.getAlpha()); }
		catch (AnyException ex) { LOGGER.info("FORCED AnyException: " + ex.getMessage()); }
		System.out.println("results: " + results);
		assertEquals("ALEPH", anyObject.getAlpha(), ASSERT_MSG);
	}

	@Test void testmock_doThrow_when( ) {
		//
		// JUnit4: @Test(expected = IllegalStateException.class)
		String results = EOL;
		AnyObject anyObject = new AnyObject();
		AnyObject anyObjectMock = Mockito.mock(AnyObject.class);
		//
		doThrow(AnyException.class).when(anyObjectMock).setAlpha(any(String.class));
		anyObject.setAlpha(SAMPLE);
		try { anyObjectMock.setAlpha(SAMPLE); }
		catch (AnyException ex) { LOGGER.info(ex.getMessage()); }
		//
		results += String.format("\t aob.getClassName(): %s \n", anyObject.getAlpha());
		results += String.format("\t aom.getClassName(): %s \n", anyObjectMock.getAlpha());
		System.out.println("results: " + results);
		assertNull(anyObjectMock.getAlpha(), ASSERT_MSG);
	}

	@Test void testmock_when_chained( ) {
		//
		// JUnit4: @Test(expected = IllegalStateException.class)
		String results = EOL;
		AnyObject anyObjectMock = Mockito.mock(AnyObject.class);
		when(anyObjectMock.getAlpha())
			.thenReturn(SAMPLE)
			.thenThrow(AnyException.class);
		try {
			results += String.format("\t aob.getAlpha(): %s \n", anyObjectMock.getAlpha());
			results += String.format("\t aom.getAlpha(): %s \n", anyObjectMock.getAlpha());// throws error
		}
		catch (AnyException ex) { LOGGER.info("FORCED AnyException: " + ex.getMessage()); }
		System.out.println("results: " + results);
		assertNull(anyObjectMock.getBeta(), ASSERT_MSG);
	}

	@Test void testspy_doReturn_when( ) {
		//
		String results = EOL;
		//
		AnyObject anyObject = new AnyObject();
		AnyObject anyObjectSpy = Mockito.spy(anyObject);
		//
		doReturn(SAMPLE).when(anyObjectSpy).getAlpha();
		anyObjectSpy.setAlpha("IGNORED");
		//
		results += String.format("\t aos.getAlpha(): %s \n", anyObjectSpy.getAlpha());
		results += String.format("\t aos.getBeta(): %s \n", anyObjectSpy.getBeta());
		System.out.println("results: " + results);
		assertEquals(SAMPLE, anyObjectSpy.getAlpha(), ASSERT_MSG);
		assertEquals("BETH", anyObjectSpy.getBeta(), ASSERT_MSG);
	}

	// #### PRIVATE MOCKS ####
	/*
		https://roytuts.com/how-to-test-private-methods-using-junit-5/
		Reflection, Spring ReflectionTestUtils, Powermock
	*/
	@Test void testReflection_getPrivateText( ) {
		//
		String results = "";
		String expects = "PRIVATE_TEXT!";
		//
		AnyObject anyObject = new AnyObject();
		try {
			Method method = AnyObject.class.getDeclaredMethod("getPrivateText");
			method.setAccessible(true);
			Object object = method.invoke(anyObject);
			results += object.toString();
		}
		catch (NoSuchMethodException | IllegalAccessException | IllegalArgumentException |
		       InvocationTargetException ex) {
			System.out.println("ERROR: " + ex.getMessage());
		}
		//
		System.out.println("results: " + results);
		assertEquals(expects, results, ASSERT_MSG);
	}

	@Test void testWhitebox_getPrivateText( ) {
		//
		// https://stackoverflow.com/questions/46454995/how-to-hide-warning-illegal-reflective-access-in-java-9-without-jvm-argument
		// configure JDK compiler with flag: --illegal-access=permit
		// System.err.close(); System.setErr(System.out);
		String results = "";
		String expects = "PRIVATE_TEXT!";
		System.err.close();
		System.setErr(System.out);
		//
		AnyObject anyObject = new AnyObject();
		try { results = Whitebox.invokeMethod(anyObject, "getPrivateText").toString(); }
		catch (Exception ex) { System.out.println("ERROR: " + ex.getMessage()); }
		//
		System.out.println("results: " + results);
		assertEquals(expects, results, ASSERT_MSG);
	}

	@Test void testRTU_getPrivateText( ) {
		//
		// for SLF4J multiple bindings used by ReflectionTestUtils, build.gradle needs:
		// exclude group: 'org.springframework.boot', module: 'spring-boot-starter-logging'
		String expects = "PRIVATE_TEXT!";
		AnyObject anyObject = new AnyObject();
		String results = ReflectionTestUtils.invokeMethod(anyObject, "getPrivateText");
		//
		System.out.println("results: " + results);
		assertEquals(expects, results, ASSERT_MSG);
	}
}
