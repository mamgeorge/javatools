package utils;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import static org.junit.jupiter.api.Assertions.assertTrue;
import samples.AnyException;
import samples.AnyObject;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

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
	String SAMPLE = "OMEGA";

	@Test void testmock_when_thenReturn() {
		//
		String txtLines = "";
		//
		AnyObject anyObject = new AnyObject();
		AnyObject anyObjectMock = Mockito.mock(AnyObject.class);
		when(anyObjectMock.getAlpha()).thenReturn(SAMPLE);
		//
		txtLines += String.format("\t aob.getAlpha(): %s \n", anyObject.getAlpha());
		txtLines += String.format("\t aom.getAlpha(): %s \n", anyObjectMock.getAlpha());
		System.out.println(txtLines);
		assertTrue(anyObjectMock.getAlpha().equals(SAMPLE), ASSERT_MSG);
	}

	@Test void testmock_doReturn_when() {
		//
		String txtLines = "";
		//
		AnyObject anyObject = new AnyObject();
		AnyObject anyObjectMock = Mockito.mock(AnyObject.class);
		doReturn(SAMPLE).when(anyObjectMock).getAlpha();
		//
		txtLines += String.format("\t aob.getAlpha(): %s \n", anyObject.getAlpha());
		txtLines += String.format("\t aom.getAlpha(): %s \n", anyObjectMock.getAlpha());
		System.out.println(txtLines);
		assertTrue(anyObjectMock.getAlpha().equals(SAMPLE), ASSERT_MSG);
	}

	@Test void testmock_doNothing_whenn() {
		//
		List listReal = new ArrayList();
		listReal.add(0, "alpha");
		//
		List listMock = Mockito.mock(ArrayList.class);
		doNothing().when(listMock).add(isA(Integer.class), isA(String.class));
		listMock.add(0, "beta");
		//
		System.out.println(listReal);
		System.out.println(listMock);
		verify(listMock,times(1)).add(0, "beta");
	}

	@Test void testmock_when_thenThrow() {
		//
		// JUnit4: @Test(expected = IllegalStateException.class)
		String txtLines = "";
		AnyObject anyObject = new AnyObject();
		AnyObject anyObjectMock = Mockito.mock(AnyObject.class);
		when(anyObjectMock.getAlpha()).thenThrow(AnyException.class);
		txtLines += String.format("\t aob.getStringValue(): %s \n", anyObject.getAlpha());
		try {
			txtLines += String.format("\t aom.getStringValue(): %s \n", anyObjectMock.getAlpha());
		} catch (AnyException ex) {
			LOGGER.info("FORCED AnyException: " + ex.getMessage());
		}
		System.out.println(txtLines);
		assertTrue(anyObject.getAlpha().equals("ALEPH"), ASSERT_MSG);
	}

	@Test void testmock_doThrow_when() {
		//
		// JUnit4: @Test(expected = IllegalStateException.class)
		String txtLines = "";
		AnyObject anyObject = new AnyObject();
		AnyObject anyObjectMock = Mockito.mock(AnyObject.class);
		doThrow(AnyException.class).when(anyObjectMock).setAlpha(any(String.class));
		anyObject.setAlpha(SAMPLE);
		try {
			anyObjectMock.setAlpha(SAMPLE);
		} catch (AnyException ex) {
			LOGGER.info(ex.getMessage());
		}
		txtLines += String.format("\t aob.getClassName(): %s \n", anyObject.getAlpha());
		txtLines += String.format("\t aom.getClassName(): %s \n", anyObjectMock.getAlpha());
		System.out.println(txtLines);
		assertTrue(anyObjectMock.getAlpha() == null, ASSERT_MSG);
	}

	@Test void testmock_when_chained() {
		//
		// JUnit4: @Test(expected = IllegalStateException.class)
		String txtLines = "";
		AnyObject anyObjectMock = Mockito.mock(AnyObject.class);
		when(anyObjectMock.getAlpha())
				.thenReturn(SAMPLE)
				.thenThrow(AnyException.class);
		try {
			txtLines += String.format("\t aob.getAlpha(): %s \n", anyObjectMock.getAlpha());
			txtLines += String.format("\t aom.getAlpha(): %s \n", anyObjectMock.getAlpha());// throws error
		} catch (AnyException ex) {
			LOGGER.info("FORCED AnyException: " + ex.getMessage());
		}
		System.out.println(txtLines);
		assertTrue(anyObjectMock.getBeta() == null, ASSERT_MSG);
	}

	@Test void testspy_doReturn_when() {
		//
		String txtLines = "";
		//
		AnyObject anyObject = new AnyObject();
		AnyObject anyObjectSpy = Mockito.spy(anyObject);
		doReturn(SAMPLE).when(anyObjectSpy).getAlpha();
		//
		anyObjectSpy.setAlpha("IGNORED");
		txtLines += String.format("\t aos.getAlpha(): %s \n", anyObjectSpy.getAlpha());
		txtLines += String.format("\t aos.getBeta(): %s \n", anyObjectSpy.getBeta());
		System.out.println(txtLines);
		assertTrue(anyObjectSpy.getAlpha().equals(SAMPLE), ASSERT_MSG);
		assertTrue(anyObjectSpy.getBeta().equals("BETH"), ASSERT_MSG);
	}
}
