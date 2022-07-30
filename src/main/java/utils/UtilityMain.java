package utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.time.format.DateTimeFormatter.ISO_DATE_TIME;
import static org.apache.http.HttpHeaders.CONTENT_TYPE;
import static org.apache.http.HttpHeaders.USER_AGENT;

public class UtilityMain {

	public static final Logger LOGGER = Logger.getLogger(UtilityMain.class.getName());

	public static final String USER_AGENT_VAL = "Mozilla/5.0";
	public static final String GREEN = "\u001b[32,1m";
	public static final String RESET = "\u001b[0m";
	public static final String TAB = "\t";
	public static final String EOL = "\n";
	public static final String CRLF = "\r\n";

	public static final String FLD_SAMPLE = "static/";
	public static final String TXT_SAMPLE = "Genesis_01.txt";
	public static final String XML_SAMPLE = "xml/books.xml";
	public static final String ZIP_SAMPLE = "xml_wav_plants_w10.zip";
	public static final String PATH_LOCAL_TEMP = "src/main/resources/temp/";
	public static final String PATH_PREF = "C:/workspace/github/spring_annotations/src/main/resources/";
	//

	public static void main(String[] strings) {
		//
		LOGGER.info("UtilityMain");
		System.out.println(GREEN + "DONE" + RESET);
	}

	//#### basics
	public static String showSys( ) {
		//
		Map<String, String> mapEnv = System.getenv();
		Map<String, String> mapEnvTree = new TreeMap<>(mapEnv);
		StringBuffer stringBuffer = new StringBuffer();
		stringBuffer.append("[");
		AtomicInteger aint = new AtomicInteger();
		mapEnvTree.forEach((key, val) -> {
			//
			val = val.replace("\\", "/");
			val = val.replace("\"", "'");
			// stringBuffer.append("{\"" + key + "\":\"" + val + "\"},");
			stringBuffer.append(String.format("\t%03d %-20s : %s\n", aint.incrementAndGet(), key, val));
		});
		stringBuffer.append("]\n");
		stringBuffer.append("\tUSERNAME: ").append(System.getenv("USERNAME")).append(EOL);
		return stringBuffer.toString();
	}

	public static String showTimes( ) {
		//
		String instantNow = Instant.now().toString();
		//
		LocalDateTime localDateTime = LocalDateTime.now();
		String ldtFormat = ISO_DATE_TIME.format(localDateTime);
		//
		String DATE_PATTERN_UTC = "yyyy-MM-dd'T'HH:mm:ss:SSS'Z'";
		SimpleDateFormat SDF = new SimpleDateFormat(DATE_PATTERN_UTC);
		String sdfFormat = SDF.format(new Date());
		//
		String DATE_PATTERN_UTCZ = "yyyy-MM-dd'T'HH:mm:ss:SSS z";
		SimpleDateFormat SDFZ = new SimpleDateFormat(DATE_PATTERN_UTCZ);
		SDFZ.setTimeZone(TimeZone.getTimeZone(ZoneOffset.UTC));
		String stzFormat = SDFZ.format(new Date());
		//
		String txtLines = "showTimes()" + EOL;
		txtLines += String.format("\t instantNow: %s \n", instantNow);
		txtLines += String.format("\t ldtFormat : %s \n", ldtFormat);
		txtLines += String.format("\t sdfFormat : %s \n", sdfFormat);
		txtLines += String.format("\t stzFormat : %s \n", stzFormat);
		//
		return txtLines;
	}

	//#### files
	public static String getFileLines(String pathFile, String eol) {
		/*
			https://mkyong.com/java8/java-8-stream-read-a-file-line-by-line/

			try { txtLines = Files.readString(Paths.get(pathName), UTF_8); }
			catch (IOException ex) { LOGGER.warning( ex.getMessage( ) ); }
		 */
		String txtLines = "";
		if ( pathFile == null || pathFile.equals("") ) {
			pathFile = FLD_SAMPLE + TXT_SAMPLE;
		}
		if ( eol == null || eol.equals("") ) {
			eol = EOL;
		}
		//
		List<String> list;
		try ( BufferedReader bReader = Files.newBufferedReader(Paths.get(pathFile)) ) {
			//
			list = bReader.lines().collect(Collectors.toList());
			txtLines = String.join(EOL, list);
			txtLines = txtLines.replaceAll(EOL, eol);
		}
		catch (IOException ex) {
			LOGGER.warning(ex.getMessage());
		}
		//
		return txtLines;
	}

	public static String getFileLocal(String pathFile) {
		//
		// https://howtodoinjava.com/java/io/read-file-from-resources-folder/
		// File file = ResourceUtils.getFile("classpath:config/sample.txt")
		String txtLines = "";
		if ( pathFile == null || pathFile.equals("") ) {
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

	public static List<File> getFilesFromZip(String fileName) {
		//
		// https://www.baeldung.com/java-compress-and-uncompress
		List<File> list = new ArrayList<>();
		if ( fileName == null || fileName.equals("") ) {
			fileName = FLD_SAMPLE + ZIP_SAMPLE;
		}
		int BUFFER_SIZE = 4096;
		String fileItem;
		try {
			//
			ClassLoader classLoader = ClassLoader.getSystemClassLoader();
			File fileZip = new File(classLoader.getResource(fileName).getFile());
			FileInputStream fis = new FileInputStream(fileZip);
			ZipInputStream zis = new ZipInputStream(fis);
			ZipEntry zipEntry = zis.getNextEntry();
			//
			FileOutputStream fos;
			byte[] bytes;
			int intReadLen;
			File file;
			while ( zipEntry != null ) {
				//
				fileItem = zipEntry.getName();
				file = new File(PATH_LOCAL_TEMP + fileItem);
				intReadLen = 0;
				bytes = new byte[BUFFER_SIZE];
				fos = new FileOutputStream(file);
				while ( ( intReadLen = zis.read(bytes) ) > 0 ) {
					fos.write(bytes, 0, intReadLen);
				}
				fos.close();
				zipEntry = zis.getNextEntry();
				list.add(file);
			}
		}
		catch (IOException ex) {
			LOGGER.warning(ex.getMessage());
		}
		return list;
	}

	public static String getZipFileList(String fileName, String eol) {
		//
		// https://www.baeldung.com/java-compress-and-uncompress
		StringBuilder stringBuilder = new StringBuilder();
		if ( eol == null || eol.equals("") ) {
			eol = EOL;
		}
		//
		List<File> list = UtilityMain.getFilesFromZip(fileName);
		for ( File file : list ) {
			stringBuilder.append(file.getName()).append(eol);
		}
		return stringBuilder.toString();
	}

	public static File putFilesIntoZip(List<File> list) { /* ??? */
		//
		// https://www.baeldung.com/java-compress-and-uncompress
		File file = null;
		return file;
	}

	//#### url
	public static String urlGet(String link) {
		//
		String txtLines;
		try {
			URL url = new URL(link);
			HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
			httpConn.setRequestMethod("GET");
			httpConn.setRequestProperty(USER_AGENT, USER_AGENT_VAL);
			// httpConn.setRequestProperty( HttpHeaders.CONTENT_TYPE, CONTENTTYPE_JSON );
			// httpConn.setRequestProperty( HttpHeaders.AUTHORIZATION, "JWT " + jwtSourceId );
			httpConn.setConnectTimeout(5000);
			httpConn.setReadTimeout(5000);
			//
			int responseCode = httpConn.getResponseCode();
			LOGGER.info("sends GET to: " + url);
			LOGGER.info("responseCode: " + responseCode);
			//
			InputStream inputStream = httpConn.getInputStream();
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
			LOGGER.log(Level.SEVERE, "#### ERROR: {0} ", txtLines);
		}
		return txtLines;
	}

	public static String urlPost(String link, String postParms) {
		//
		// http://zetcode.com/java/getpostrequest/
		String txtLines = "";
		try {
			URL url = new URL(link);
			HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
			httpConn.setDoOutput(true);
			httpConn.setRequestMethod("POST");
			// httpConn.setRequestProperty(USER_AGENT, USER_AGENT);
			// httpConn.setRequestProperty(CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE);
			// httpConn.setRequestProperty( AUTHORIZATION, "JWT " + jwtSourceId );
			//
			OutputStream outputStream = httpConn.getOutputStream();
			outputStream.write(postParms.getBytes());
			outputStream.flush();
			outputStream.close();
			//
			int responseCode = httpConn.getResponseCode();
			LOGGER.info("Sending POST : " + url);
			LOGGER.info("Response code: " + responseCode);
			//
			InputStream inputStream = httpConn.getInputStream();
			InputStreamReader isr = new InputStreamReader(inputStream);
			BufferedReader bufferedReader;
			StringBuilder stringBuilder = new StringBuilder();
			String txtLine;
			if ( responseCode == HttpURLConnection.HTTP_OK ) {
				//
				bufferedReader = new BufferedReader(isr);
				while ( ( txtLine = bufferedReader.readLine() ) != null ) {
					stringBuilder.append(txtLine);
				}
				bufferedReader.close();
				txtLines = stringBuilder.toString();
			} else {
				LOGGER.info("POST failed to: " + link);
			}
		}
		catch (IOException ex) {
			txtLines = ex.getMessage();
			LOGGER.log(Level.SEVERE, "#### ERROR: {0} ", txtLines);
		}
		return txtLines;
	}

	public static String urlPostFile(String link, String postParms, String pathTxt, String pathBin) {
		//
		// https://www.baeldung.com/httpclient-multipart-upload
		// https://stackoverflow.com/questions/2469451/upload-files-from-java-client-to-a-http-server
		String txtLines;
		File fileTxt = new File(PATH_PREF + pathTxt);
		File fileBin = new File(PATH_PREF + pathBin);
		String boundary = Long.toHexString(System.currentTimeMillis()); // random boundary
		URLConnection urlConn;
		try {
			URL url = new URL(link);
			urlConn = url.openConnection();
			urlConn.setDoOutput(true);
			urlConn.setRequestProperty(CONTENT_TYPE, "multipart/form-data; boundary=" + boundary);
			//	urlConn.setRequestProperty( "Authorization", "JWT " + jwtSourceId );
			//
			System.out.println("0 urlConn.getOutputStream( )");
			OutputStream outputStream = urlConn.getOutputStream();
			OutputStreamWriter osw = new OutputStreamWriter(outputStream, UTF_8);
			PrintWriter writer = new PrintWriter(osw, true);
			//
			System.out.println("1 Send normal parms");
			writer.append("--").append(boundary).append(CRLF)
				.append("Content-Disposition: form-data; name=\"param\"").append(CRLF)
				.append("Content-Type: text/plain; charset=").append(UTF_8.toString()).append(CRLF)
				.append(CRLF).append(postParms).append(CRLF)
				.flush();
			//
			System.out.println("2 Send text file in charset UTF_8");
			writer.append("--").append(boundary).append(CRLF)
				.append("Content-Disposition: form-data; name=\"textFile\"; filename=\"")
				.append(fileTxt.getName()).append("\"").append(CRLF)
				.append("Content-Type: text/plain; charset=").append(UTF_8.toString()).append(CRLF)
				.append(CRLF)
				.flush();
			Files.copy(fileTxt.toPath(), outputStream);
			outputStream.flush(); // Important before continuing with writer!
			writer.append(CRLF).flush(); // CRLF indicates end of boundary
			//
			System.out.println("3 Send binary file");
			writer.append("--").append(boundary).append(CRLF)
				.append("Content-Disposition: form-data; name=\"binaryFile\"; filename=\"")
				.append(fileBin.getName()).append("\"").append(CRLF);
			String fileBinContentType = URLConnection.guessContentTypeFromName(fileBin.getName());
			writer.append("Content-Type: ").append(fileBinContentType).append(CRLF)
				.append("Content-Transfer-Encoding: binary").append(CRLF)
				.append(CRLF)
				.flush();
			Files.copy(fileBin.toPath(), outputStream);
			outputStream.flush(); // Important before continuing with writer!
			writer.append(CRLF).flush(); // CRLF indicates end of boundary
			//
			System.out.println("4 end of multipart/form-data");
			writer.append("--").append(boundary).append("--").append(CRLF).flush();
			//
			System.out.println("5 request lazily fired to get response info");
			int responseCode = ( (HttpURLConnection) urlConn ).getResponseCode();
			txtLines = "responseCode: " + responseCode; // should be 200
		}
		catch (IOException ex) {
			txtLines = ex.getMessage();
			LOGGER.log(Level.SEVERE, "#### ERROR: {0} ", txtLines);
		}
		//
		return txtLines;
	}

	//#### reflection
	public static String getField(Object object, String nameField) {
		//
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
		//
		Object objectReturn = "";
		try {
			int parmsCount = 0;
			Object objectItem = null;
			if ( objectParms == null || objectParms.length == 0 ) {
			} else {
				parmsCount = objectParms.length;
			}
			Class<?>[] classArray = new Class<?>[parmsCount];
			for ( int ictr = 0; ictr < parmsCount; ictr++ ) {
				try {
					objectItem = objectParms[ictr];
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
			//
			// Class clazz = object.getClass();
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
		//
		StringBuilder stringBuilder = new StringBuilder();
		Set set = new TreeSet();
		Method[] methods = object.getClass().getDeclaredMethods();
		//
		Object[] args = null;
		int MAXLEN = 35;
		String FRMT = "\t%02d %-25s | %-35s | %02d | %s \n";
		AtomicInteger atomicInteger = new AtomicInteger();
		Arrays.stream(methods).forEach(mthd -> {
			//
			Object objectVal = "";
			String returnType = mthd.getReturnType().toString();
			if ( returnType.length() > MAXLEN ) {
				returnType = returnType.substring(returnType.length() - MAXLEN);
			}
			mthd.setAccessible(true);
			boolean boolClass = mthd.getReturnType().toString().startsWith("class");
			boolean boolCount = mthd.getParameterCount() == 0;
			if ( boolClass & boolCount ) {
				try {
					objectVal = mthd.invoke(object, args);
				}
				catch (IllegalAccessException | InvocationTargetException ex) {
					LOGGER.info(ex.getMessage());
				}
				if ( objectVal == null ) {
					objectVal = "NULL or EMPTY";
				}
			}
			boolean boolAccess = mthd.getName().startsWith("access$");
			if ( !boolAccess ) {
				set.add(String.format(FRMT, atomicInteger.incrementAndGet(),
					mthd.getName(), returnType, mthd.getParameterCount(), objectVal));
			}
		});
		//

		stringBuilder.append(object.getClass().getName()).append(" has: [").append(methods.length)
			.append("] methods\n\n");
		set.stream().sorted().forEach(val -> stringBuilder.append(val));
		return stringBuilder + "\n";
	}

	public static void putObject(Object object, String objectName, Object objectValue) {
		//
		try {
			Class<?> clazz = object.getClass();
			Field field = clazz.getDeclaredField(objectName);
			field.setAccessible(true);
			field.set(object, objectValue);
		}
		catch (NoSuchFieldException | IllegalAccessException ex) {
			LOGGER.severe(ex.getMessage());
		}
	}

	//#### xml/yml/json
	public static String getXmlNode(String xml, String xpathTxt) {
		//
		// https://howtodoinjava.com/xml/evaluate-xpath-on-xml-string/
		String txtLines = "";
		try {
			StringReader stringReader = new StringReader(xml);
			InputSource inputSource = new InputSource(stringReader);
			XPath xPath = XPathFactory.newInstance().newXPath();
			txtLines = xPath.evaluate(xpathTxt, inputSource);
		}
		catch (XPathExpressionException ex) {
			LOGGER.warning(ex.getMessage());
		}
		return txtLines;
	}

	public static String formatXml(String xmlOld) {
		//
		String xml = "";
		if ( xmlOld == null || xmlOld.equals("") ) {
			xmlOld = getFileLocal(FLD_SAMPLE + XML_SAMPLE);
		}
		//
		Document document = null;
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder documentBuilder = dbf.newDocumentBuilder();
			StringReader stringReader = new StringReader(xmlOld);
			InputSource inputSource = new InputSource(stringReader);
			document = documentBuilder.parse(inputSource);
		}
		catch (ParserConfigurationException | SAXException | IOException ex) {
			LOGGER.warning(ex.getMessage());
		}
		try {
			StringWriter stringWriter = new StringWriter();
			StreamResult streamResultXML = new StreamResult(stringWriter);
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			// tf.setAttribute( "indent-number", 4 );
			Transformer transformer = transformerFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.METHOD, "xml");
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty(OutputKeys.ENCODING, UTF_8.toString());
			DOMSource domSource = new DOMSource(document);
			transformer.transform(domSource, streamResultXML);
			xml = streamResultXML.getWriter().toString();
		}
		catch (TransformerException ex) {
			LOGGER.severe(ex.getMessage());
		}
		return xml;
	}

	public static String transformXslt(String xml, String xsl) {
		//
		String txtLines = "";
		try {
			// bring in data in a way that can be processed
			StreamSource streamSourceXML = new StreamSource(new StringReader(xml));
			StreamSource streamSourceXSL = new StreamSource(new StringReader(xsl));
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			Result result = new StreamResult(baos);
			//
			// setup transformers
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer(streamSourceXSL);
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty(OutputKeys.MEDIA_TYPE, "MediaType.TEXT_XML_VALUE");
			transformer.setOutputProperty(OutputKeys.ENCODING, UTF_8.toString());
			//
			// transform it
			transformer.transform(streamSourceXML, result);
			txtLines = baos.toString();
		}
		catch (TransformerException ex) {
			LOGGER.severe(ex.getMessage());
		}
		//
		return txtLines;
	}

	public static String parseYaml2JsonNode(String yamlFileName, String applicationNode) {
		//
		String txtLine = "";
		try {
			ClassLoader classLoader = ClassLoader.getSystemClassLoader();
			InputStream inputStream = classLoader.getResourceAsStream(yamlFileName);
			//
			// create yaml from file
			YAMLFactory yamlFactory = new YAMLFactory();
			ObjectMapper objectMapperYaml = new ObjectMapper(yamlFactory);
			Object objectYaml = objectMapperYaml.readValue(inputStream, Map.class);
			// System.out.println( "yaml: " + objectYaml );
			//
			// convert yaml to json
			ObjectMapper objectMapperJson = new ObjectMapper();
			String json = objectMapperJson.writeValueAsString(objectYaml);
			// System.out.println( "json: " + json );
			//
			// convert json to node object; "path" also works, "at" does not
			ObjectMapper objectMapperNode = new ObjectMapper();
			JsonNode jsonNode = objectMapperNode.readTree(json);
			txtLine = ( jsonNode.get(applicationNode) ).asText();
		}
		catch (IOException ex) {
			LOGGER.warning(ex.getMessage());
		}
		//
		return txtLine;
	}

	public static String parseJsonList2List(String jsonArr, int listFormat) {
		//
		String txtLines = "";
		ObjectMapper objectMapperHtml = new ObjectMapper();
		TypeReference<ArrayList<LinkedHashMap<String, String>>> typeReference = new TypeReference<>() {
		};
		ArrayList<LinkedHashMap<String, String>> arrayList;
		Set<?> set;
		String txtKey, txtVal;
		Object objVal;
		String PFX = TAB;
		String MID = " : ";
		String SFX = "";
		if ( listFormat > 0 ) {
			PFX = "<tr><th>";
			MID = "</th<td>";
			SFX = "</td></tr>";
		}
		try {
			arrayList = objectMapperHtml.readValue(jsonArr, typeReference);
			for ( LinkedHashMap<String, String> linkedHashMap : arrayList ) {
				//
				set = linkedHashMap.keySet();
				txtKey = set.toString().substring(1, set.toString().length() - 1);
				objVal = linkedHashMap.get(txtKey);
				txtVal = objVal.toString();
				txtLines += PFX + txtKey + MID + txtVal + SFX;
			}
			System.out.println("txtLines: " + txtLines);
		}
		catch (JsonProcessingException ex) {
			LOGGER.warning(ex.getMessage());
		}
		return txtLines;
	}
}
//----
