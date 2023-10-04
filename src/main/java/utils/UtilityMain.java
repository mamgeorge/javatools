package utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.net.HttpURLConnection.HTTP_OK;
import static java.time.format.DateTimeFormatter.ISO_DATE_TIME;
import static org.apache.http.HttpHeaders.USER_AGENT;

public class UtilityMain {

	public static final Logger LOGGER = Logger.getLogger(UtilityMain.class.getName());

	public static final String USER_AGENT_VAL = "Mozilla/5.0";
	public static final String GREEN = "\u001b[32,1m";
	public static final String RESET = "\u001b[0m";
	public static final String TAB = "\t";
	public static final String EOL = "\n";
	public static final String CRLF = "\r\n";

	public static final String PATHFILE_LOCAL = "src/test/resources/"; // "C:/Users/mamge/Kerberos/config/"
	public static final String FLD_SAMPLE = "static/";
	public static final String TXT_SAMPLE = "Genesis_01.txt";
	public static final String ZIP_SAMPLE = "xml_wav_plants_w10.zip";
	public static final String PATH_LOCAL_TEMP = PATHFILE_LOCAL + "/temp/";

	public static final Random RANDOM = new Random();
	public static final String ERR_VAL = "#### ERROR: {0} ";

	public static void main(String[] strings) {

		LOGGER.info("UtilityMain");
		System.out.println(GREEN + "DONE" + RESET);
	}

	//#### basics
	public static String showSys( ) {

		Map<String, String> mapEnv = System.getenv();
		Map<String, String> mapEnvTree = new TreeMap<>(mapEnv);
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("[");
		AtomicInteger aint = new AtomicInteger();
		mapEnvTree.forEach((key, val) -> {

			val = val.replace("\\", "/");
			val = val.replace("\"", "'");
			stringBuilder.append(String.format("\t%03d %-20s : %s%n", aint.incrementAndGet(), key, val));
		});
		stringBuilder.append("]\n");
		stringBuilder.append("\tUSERNAME: ").append(System.getenv("USERNAME")).append(EOL);
		return stringBuilder.toString();
	}

	public static String showTimes( ) {

		String instantNow = Instant.now().toString();

		LocalDateTime localDateTime = LocalDateTime.now();
		String ldtFormat = ISO_DATE_TIME.format(localDateTime);

		String DATE_PATTERN_UTC = "yyyy-MM-dd'T'HH:mm:ss:SSS'Z'";
		SimpleDateFormat SDF = new SimpleDateFormat(DATE_PATTERN_UTC);
		String sdfFormat = SDF.format(new Date());

		String DATE_PATTERN_UTCZ = "yyyy-MM-dd'T'HH:mm:ss:SSS z";
		SimpleDateFormat SDFZ = new SimpleDateFormat(DATE_PATTERN_UTCZ);
		SDFZ.setTimeZone(TimeZone.getTimeZone(ZoneOffset.UTC));
		String stzFormat = SDFZ.format(new Date());

		String txtLines = "showTimes()" + EOL;
		txtLines += String.format("\t instantNow: %s %n", instantNow);
		txtLines += String.format("\t ldtFormat : %s %n", ldtFormat);
		txtLines += String.format("\t sdfFormat : %s %n", sdfFormat);
		txtLines += String.format("\t stzFormat : %s %n", stzFormat);

		return txtLines;
	}

	//#### files
	public static String getFileLines(String pathFile, String eol) {

		String txtLines = "";
		if ( pathFile == null || pathFile.isEmpty() ) {
			pathFile = FLD_SAMPLE + TXT_SAMPLE;
		}
		if ( eol == null || eol.isEmpty() ) {
			eol = EOL;
		}

		List<String> list;
		try ( BufferedReader bReader = Files.newBufferedReader(Paths.get(pathFile)) ) {

			list = bReader.lines().toList();
			txtLines = String.join(EOL, list);
			txtLines = txtLines.replace(EOL, eol);
		}
		catch (IOException ex) {
			LOGGER.warning(ex.getMessage());
		}

		return txtLines;
	}

	public static String getFileLocal(String pathFile) {

		// https://howtodoinjava.com/java/io/read-file-from-resources-folder/
		// File file = ResourceUtils.getFile("classpath:config/sample.txt")
		String txtLines = "";
		if ( pathFile == null || pathFile.isEmpty() ) {
			pathFile = FLD_SAMPLE + TXT_SAMPLE;
		}
		try {
			File fileLocal = new File(pathFile);
			File pathFileLocal = new File(fileLocal.getAbsolutePath());
			txtLines = Files.readString(pathFileLocal.toPath());
		}
		catch (IOException | NullPointerException ex) {
			LOGGER.warning(ex.getMessage());
		}
		return txtLines;
	}

	//#### url
	public static String urlGet(String link) {

		String txtLines;
		try {
			URL url = new URL(link);
			HttpURLConnection HUC = (HttpURLConnection) url.openConnection();
			HUC.setRequestMethod("GET");
			HUC.setRequestProperty(USER_AGENT, USER_AGENT_VAL);
			// HttpHeaders CONTENT_TYPE, CONTENTTYPE_JSON
			// HttpHeaders AUTHORIZATION, "JWT " + jwtSourceId
			HUC.setConnectTimeout(5000);
			HUC.setReadTimeout(5000);

			int responseCode = HUC.getResponseCode();
			LOGGER.info("sends GET to: " + url);
			LOGGER.info("responseCode: " + responseCode);

			InputStream inputStream = HUC.getInputStream();
			InputStreamReader isr = new InputStreamReader(inputStream);
			BufferedReader bufferedReader = new BufferedReader(isr);
			StringBuilder stringBuilder = new StringBuilder();
			String txtLine;
			while ( ( txtLine = bufferedReader.readLine() ) != null ) {
				stringBuilder.append(txtLine + EOL);
			}
			txtLines = stringBuilder.toString();
		}
		catch (IOException ex) {
			txtLines = ex.getMessage();
			LOGGER.log(Level.SEVERE, ERR_VAL, txtLines);
		}
		return txtLines;
	}

	public static String urlPost(String link, String postParms) {

		// http://zetcode.com/java/getpostrequest/
		String txtLines = "";
		try {
			URL url = new URL(link);
			HttpURLConnection HUC = (HttpURLConnection) url.openConnection();
			HUC.setDoOutput(true);
			HUC.setRequestMethod("POST");
			// USER_AGENT, USER_AGENT
			// CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE
			// AUTHORIZATION, "JWT " + jwtSourceId
			//
			OutputStream outputStream = HUC.getOutputStream();
			outputStream.write(postParms.getBytes());
			outputStream.flush();
			outputStream.close();

			int responseCode = HUC.getResponseCode();
			LOGGER.info("Sending POST : " + url);
			LOGGER.info("Response code: " + responseCode);

			InputStream inputStream = HUC.getInputStream();
			InputStreamReader ISR = new InputStreamReader(inputStream);
			BufferedReader bufferedReader;
			StringBuilder stringBuilder = new StringBuilder();
			String txtLine;
			if ( responseCode == HTTP_OK ) {

				bufferedReader = new BufferedReader(ISR);
				while ( ( txtLine = bufferedReader.readLine() ) != null ) {
					stringBuilder.append(txtLine);
				}
				bufferedReader.close();
				txtLines = stringBuilder.toString();
			} else {
				LOGGER.info(String.format("POST failed to: %s", link));
			}
		}
		catch (IOException ex) {
			txtLines = ex.getMessage();
			LOGGER.log(Level.SEVERE, ERR_VAL, txtLines);
		}
		return txtLines;
	}

	//#### reflection
	public static String getField(Object object, String nameField) {

		String txtLine = "";
		try {
			Class<?> clazz = object.getClass();
			Field field = clazz.getDeclaredField(nameField);
			field.setAccessible(true);
			Object objectField = field.get(object);
			txtLine = objectField.toString();
		}
		catch (NoSuchFieldException | IllegalAccessException ex) {
			LOGGER.severe(ex.getMessage());
		}
		return txtLine;
	}

	public static Object getMethod(Class<?> clazz, String nameMethod, Object... objectParms) {

		Object objectReturn = "";
		try {
			int parmsCount = 0;
			Object objectItem = null;
			if ( objectParms == null || objectParms.length == 0 ) {
				System.out.println("...no args...");
			} else {
				parmsCount = objectParms.length;
			}
			Class<?>[] classArray = new Class<?>[parmsCount];
			for ( int ictr = 0; ictr < parmsCount; ictr++ ) {
				try {
					if ( objectParms == null ) { System.out.println("objectParms == null"); } else {
						objectItem = objectParms[ictr];
					}
				}
				catch (NullPointerException ex) {
					LOGGER.info(ex.getMessage());
				}
				if ( objectItem == null ) {
					classArray = new Class<?>[0];
					objectParms = null;
				} else {
					classArray[ictr] = objectItem.getClass();
				}
			}

			Object objectInstance = clazz.getDeclaredConstructor().newInstance();
			Method method = clazz.getDeclaredMethod(nameMethod, classArray);
			method.setAccessible(true);
			objectReturn = method.invoke(objectInstance, objectParms);
		}
		catch (NoSuchMethodException | IllegalArgumentException | IllegalAccessException |
		       InvocationTargetException | InstantiationException ex) {
			LOGGER.severe(ex.getMessage());
		}
		return objectReturn;
	}

	public static String exposeObject(Object object) {

		StringBuilder stringBuilder = new StringBuilder();
		Set<String> setLines = new TreeSet<>();
		Method[] methods = object.getClass().getMethods(); // getDeclaredMethods()
		List<Method> listMethods = new ArrayList<>(Arrays.asList(methods));
		listMethods.sort(Comparator.comparing(Method::getName));

		int MAXLEN = 35;
		AtomicInteger usedMethods = new AtomicInteger();
		String FRMT = "%-30s | %-35s | %02d | %s \n";
		listMethods.forEach(method -> {

			String methodName = method.getName();
			boolean boolAccess = methodName.startsWith("access$") || methodName.startsWith("$$$");
			if ( !boolAccess ) {
				usedMethods.incrementAndGet();
				Object objectVal = "";
				String returnType = method.getReturnType().toString();
				if ( returnType.length() > MAXLEN ) {
					returnType = returnType.substring(returnType.length() - MAXLEN);
				}
				method.setAccessible(true);
				Object[] args;
				if ( method.getParameterCount() > 0 ) {
					if ( method.getParameterTypes()[0].getName().contains("String") ) {
						args = new Object[]{ "RANDOM: " + getRandomString(8) };
					} else if ( method.getParameterTypes()[0].getName().contains("Date") ) {
						args = new Object[]{ new Date() };
					} else if ( method.getParameterTypes()[0].getName().contains("int") ) {
						args = new Object[]{ RANDOM.nextInt(4000) };
					} else {
						String parmname = method.getParameterTypes()[0].getName();
						args = new Object[]{ parmname };
					}
				} else { args = null; }
				try {
					objectVal = method.invoke(object, args);
					if ( objectVal == null && ( method.getParameterCount() != 0 ) ) {
						assert args != null;
						objectVal = args[0];
					}
				}
				catch (IllegalAccessException | InvocationTargetException ex) {
					LOGGER.info(methodName + " | " + ex.getMessage());
				}
				catch (IllegalArgumentException IAE) {
					objectVal = "REQUIRES: " + Objects.requireNonNull(
						args)[0];
				}
				setLines.add(
					String.format(FRMT, methodName, returnType, method.getParameterCount(), objectVal));
			}
		});

		stringBuilder.append(object.getClass().getName()).append(" has: [").append(usedMethods)
			.append("] methods\n\n");

		AtomicInteger atomicInteger = new AtomicInteger();
		setLines.forEach(val -> stringBuilder.append(String.format("\t %02d %s",
			atomicInteger.incrementAndGet(), val)));
		return stringBuilder + EOL;
	}

	public static void putObject(Object object, String objectName, Object objectValue) {

		try {
			Class<?> clazz = object.getClass();
			Field field;
			try { field = clazz.getDeclaredField(objectName); }
			catch (NoSuchFieldException ex) {
				Class<?> superClazz = clazz.getSuperclass();
				field = superClazz.getDeclaredField(objectName);
			}
			field.setAccessible(true);
			field.set(object, objectValue);
		}
		catch (NoSuchFieldException | IllegalAccessException ex) {
			LOGGER.severe(ex.getMessage());
		}
	}

	public static String getRandomString(int num) {

		StringBuilder txtRandom = new StringBuilder();
		char[] chars =
			( "1234567890abcdefghijklmnopqrstuvwxyz" + "ABCDEFGHIJKLMNOPQRSTUVWZYZ" ).toCharArray();
		for ( int ictr = 0; ictr < num; ictr++ ) {
			txtRandom.append(chars[RANDOM.nextInt(chars.length)]);
		}
		return txtRandom.toString();
	}
}
//----
