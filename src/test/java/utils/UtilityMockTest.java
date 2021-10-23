package utils;

import java.util.logging.Logger;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.util.Assert;
import samples.AnyException;
import samples.AnyObject;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

public class UtilityMockTest {

	static final Logger LOGGER = Logger.getLogger(UtilityMockTest.class.getName());
	static final String ASSERT_MSG = "ASSERT_MSG";

	@Test void testmock_when_thenReturn() {
		//
		String txtLines = "";
		String SAMPLE = "NEWSTRING";
		//
		AnyObject anyObject = new AnyObject();
		AnyObject anyObjectMock = Mockito.mock(AnyObject.class);
		txtLines += String.format("\t aob.getStringValue(): %s \n", anyObjectMock.getStringValue());
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
		txtLines += String.format("\t aon.getStringValue(): %s \n", txtLine);
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
		txtLines += String.format("\t aob.getClassName(): %s \n", anyObject.getStringValue());
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
}
