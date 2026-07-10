package utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;
import objects.AnyException;
import objects.AnyObject;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static utils.UtilityMain.EOL;
import static utils.UtilityMainTest.ASSERT_MSG;

// @TestInstance( TestInstance.Lifecycle.PER_CLASS ) // required for @BeforeAll
class UtilityMockTest {

	static final Logger LOGGER = Logger.getLogger(UtilityMockTest.class.getName());
	static final String SAMPLE = "OMEGA";

	@Mock private AnyObject anyObjectMocked;

	@InjectMocks private UtilityMockTest utilityMockTest;

	// @Mock, @InjectMocks are applied in @BeforeEach MockitoAnnotations!
	@BeforeEach void init( ) { /*MockitoAnnotations.initMocks(this);*/ }

	@Test void testmock_when_thenReturn( ) {

		String results = EOL;

		AnyObject anyObject = new AnyObject();
		AnyObject anyObjectMock = Mockito.mock(AnyObject.class);
		when(anyObjectMock.getAlpha()).thenReturn(SAMPLE);

		results += String.format("\t aob.getAlpha(): %s \n", anyObject.getAlpha());
		results += String.format("\t aom.getAlpha(): %s \n", anyObjectMock.getAlpha());
		System.out.println("results: " + results);
		assertEquals(SAMPLE, anyObjectMock.getAlpha(), ASSERT_MSG);
	}

	@Test void testmock_doReturn_when( ) {

		String results = EOL;

		AnyObject anyObject = new AnyObject();
		AnyObject anyObjectMock = Mockito.mock(AnyObject.class);
		doReturn(SAMPLE).when(anyObjectMock).getAlpha();

		results += String.format("\t aob.getAlpha(): %s \n", anyObject.getAlpha());
		results += String.format("\t aom.getAlpha(): %s \n", anyObjectMock.getAlpha());
		System.out.println("results: " + results);
		assertEquals(SAMPLE, anyObjectMock.getAlpha(), ASSERT_MSG);
	}

	@Test @Disabled( "because" ) void testmock_when_thenThrow( ) {

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

		// JUnit4: @Test(expected = IllegalStateException.class)
		String results = EOL;
		AnyObject anyObject = new AnyObject();
		AnyObject anyObjectMock = Mockito.mock(AnyObject.class);

		doThrow(AnyException.class).when(anyObjectMock).setAlpha(any(String.class));
		anyObject.setAlpha(SAMPLE);
		try { anyObjectMock.setAlpha(SAMPLE); }
		catch (AnyException ex) { LOGGER.info(ex.getMessage()); }

		results += String.format("\t aob.getClassName(): %s \n", anyObject.getAlpha());
		results += String.format("\t aom.getClassName(): %s \n", anyObjectMock.getAlpha());
		System.out.println("results: " + results);
		assertNull(anyObjectMock.getAlpha(), ASSERT_MSG);
	}

	@Test void testmock_when_chained( ) {

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

		String results = EOL;

		AnyObject anyObject = new AnyObject();
		AnyObject anyObjectSpy = Mockito.spy(anyObject);

		doReturn(SAMPLE).when(anyObjectSpy).getAlpha();
		anyObjectSpy.setAlpha("IGNORED");

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

		String results = "";
		String expects = "PRIVATE_TEXT!";

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

		System.out.println("results: " + results);
		assertEquals(expects, results, ASSERT_MSG);
	}

	@Test void testRTU_getPrivateText( ) {

		// for SLF4J multiple bindings used by ReflectionTestUtils, build.gradle needs:
		// exclude group: 'org.springframework.boot', module: 'spring-boot-starter-logging'
		String expects = "PRIVATE_TEXT!";
		AnyObject anyObject = new AnyObject();
		String results = ReflectionTestUtils.invokeMethod(anyObject, "getPrivateText");

		System.out.println("results: " + results);
		assertEquals(expects, results, ASSERT_MSG);
	}
}
