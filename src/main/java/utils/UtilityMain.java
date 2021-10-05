package utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.BufferedReader;
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
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.time.format.DateTimeFormatter.ISO_DATE_TIME;

public class UtilityMain {

	public static final Logger LOGGER = Logger.getLogger(UtilityMain.class.getName());

	public static final String USER_AGENT = "Mozilla/5.0";
	public static final String CONTENTTYPE_FORM = "application/x-www-form-urlencoded";
	public static final String CONTENTTYPE_JSON = "application/json; charset=utf-8";
	public static final String CONTENTTYPE_MULTI = "multipart/form-data; boundary=";
	public static final String AUTHORIZATION_JWT = "anyService_Api"; // sourceId
	public static final String AUTH_SECRET = "anyhash"; // client-secret in HMAC SHA256 or RSA

	public static final String GREEN = "\u001b[32,1m";
	public static final String RESET = "\u001b[0m";
	public static final String DLM = "\n";
	public static final String PAR = "\n\t";
	public static final String CRLF = "\r\n";

	public static final String FLD_SAMPLE = "static/";
	public static final String TXT_SAMPLE = "Genesis_01.txt";
	public static final String YML_SAMPLE = "application.yml";
	public static final String XML_SAMPLE = "xml/books.xml";
	public static final String JSN_SAMPLE = "xml/books.json";
	public static final String ZIP_SAMPLE = "xml_wav_plants_w10.zip";

	public static void main(String[] strings) {
		//
		LOGGER.info("UtilityMain");
		System.out.println(showTime());
		System.out.println(GREEN + "DONE" + RESET);
	}

	//############
	public static String showSys() {
		//
		Map<String, String> mapEnv = System.getenv();
		Map<String, String> mapEnvTree = new TreeMap<String, String>(mapEnv);
		StringBuffer stringBuffer = new StringBuffer();
		stringBuffer.append("[");
		// env.forEach( ( key , val ) -> stringBuffer.append( key + ": " + val + dlm ) );
		mapEnvTree.forEach((key, val) -> {
			//
			val = val.replace("\\", "/");
			val = val.replace("\"", "'");
			stringBuffer.append("{\"" + key + "\":\"" + val + "\"},");
		});
		stringBuffer.append("\n{\"" + "USERNAME" + "\":\"" + System.getenv("USERNAME") + "\"}");
		stringBuffer.append("\n]");
		return stringBuffer.toString();
	}

	public static String showTime() {
		//
		String txtLine = "";
		LocalDateTime localDateTime = LocalDateTime.now();
		txtLine = ISO_DATE_TIME.format(localDateTime);
		// txtLine = new Date( ).toString( );
		return txtLine;
	}

	public static String showTimes() {
		//
		String txtLines = "";
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
		txtLines = "showTimes()" + "\n";
		txtLines += String.format("\t instantNow: %s \n", instantNow);
		txtLines += String.format("\t ldtFormat : %s \n", ldtFormat);
		txtLines += String.format("\t sdfFormat : %s \n", sdfFormat);
		txtLines += String.format("\t stzFormat : %s \n", stzFormat);
		//
		return txtLines;
	}

	public static String getFileLines(String pathFile, String delim) {
		/*
			https://mkyong.com/java8/java-8-stream-read-a-file-line-by-line/

			try { txtLines = Files.readString(Paths.get(pathName), UTF_8); }
			catch (IOException ex) { LOGGER.warning( ex.getMessage( ) ); }
		 */
		String txtLines = "";
		if (pathFile == null || pathFile.equals("")) { pathFile = FLD_SAMPLE + TXT_SAMPLE; }
		if (delim == null || delim.equals("")) { delim = DLM; }
		//
		List<String> list = new ArrayList<>();
		try (BufferedReader bReader = Files.newBufferedReader(Paths.get(pathFile))) {
			//
			list = bReader.lines().collect(Collectors.toList());
			txtLines = String.join("\n", list);
			txtLines = txtLines.replaceAll("\n", delim);
		} catch (IOException ex) { LOGGER.warning(ex.getMessage()); }
		//
		return txtLines;
	}

	public static String getFileLocal(String pathFile, String delim) {
		//
		// https://howtodoinjava.com/java/io/read-file-from-resources-folder/
		// File file = ResourceUtils.getFile("classpath:config/sample.txt")
		String txtLines = "";
		String urlFile = "";
		if (pathFile == null || pathFile.equals("")) { pathFile = FLD_SAMPLE + TXT_SAMPLE; }
		if (delim == null || delim.equals("")) { delim = DLM; }
		try {
			/*
			ClassLoader classLoader = ClassLoader.getSystemClassLoader( );
			URL url = classLoader.getResource( fileName );
			urlFile = url.getFile( );
			// fails if run in: mvn exec:java -Dexec.mainClass
			// URL[] urls = ( (URLClassLoader) classLoader ).getURLs();
			// for(URL url: urls){ System.out.println(url.getFile( ) ); }
			*/
			File fileLocal = new File(pathFile);
			File pathFileLocal = new File(fileLocal.getAbsolutePath());
			txtLines = new String(Files.readAllBytes(pathFileLocal.toPath()), UTF_8);
		} catch (IOException | NullPointerException ex) { LOGGER.warning(ex.getMessage()); }
		return txtLines;
	}

	//############
	public static String urlGet(String link) {
		//
		String txtLines = "";
		try {
			URL url = new URL(link);
			HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
			httpConn.setRequestMethod("GET");
			httpConn.setRequestProperty("User-Agent", USER_AGENT);
			//	httpConn.setRequestProperty( "Content-Type", CONTENTTYPE_JSON );
			//	httpConn.setRequestProperty( "Authorization", "JWT " + jwtSourceId );
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
			String txtLine = "";
			while ((txtLine = bufferedReader.readLine()) != null) {
				stringBuilder.append(txtLine);
			}
			txtLines = stringBuilder.toString();
		} catch (IOException ex) {
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
			httpConn.setRequestProperty("User-Agent", USER_AGENT);
			httpConn.setRequestProperty("Content-Type", CONTENTTYPE_FORM);
			//	httpConn.setRequestProperty( "Authorization", "JWT " + jwtSourceId );
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
			BufferedReader bufferedReader = null;
			StringBuffer stringBuffer = new StringBuffer();
			String txtLine = "";
			if (responseCode == HttpURLConnection.HTTP_OK) {
				//
				bufferedReader = new BufferedReader(isr);
				while ((txtLine = bufferedReader.readLine()) != null) {
					stringBuffer.append(txtLine);
				}
				bufferedReader.close();
				txtLines = stringBuffer.toString();
			} else {
				LOGGER.info("POST failed to: " + link);
			}
		} catch (IOException ex) {
			txtLines = ex.getMessage();
			LOGGER.log(Level.SEVERE, "#### ERROR: {0} ", txtLines);
		}
		return txtLines;
	}

	public static String urlPostFile(String link, String postParms, String pathTxt, String pathBin) {
		//
		// https://www.baeldung.com/httpclient-multipart-upload
		// https://stackoverflow.com/questions/2469451/upload-files-from-java-client-to-a-http-server
		String txtLines = "";
		String PATH_PREF = "";
		if (PATH_PREF == null || PATH_PREF.equals("")) {
			PATH_PREF = "C:/workspace/github/spring_annotations/src/main/resources/";
		}
		//
		File fileTxt = new File(PATH_PREF + pathTxt);
		File fileBin = new File(PATH_PREF + pathBin);
		String boundary = Long.toHexString(System.currentTimeMillis()); // random boundary
		URLConnection urlConn = null;
		try {
			URL url = new URL(link);
			urlConn = url.openConnection();
			urlConn.setDoOutput(true);
			urlConn.setRequestProperty("Content-Type", CONTENTTYPE_MULTI + boundary);
			//	urlConn.setRequestProperty( "Authorization", "JWT " + jwtSourceId );
			//
			System.out.println("0 urlConn.getOutputStream( )");
			OutputStream outputStream = urlConn.getOutputStream();
			OutputStreamWriter osw = new OutputStreamWriter(outputStream, UTF_8);
			PrintWriter writer = new PrintWriter(osw, true);
			//
			System.out.println("1 Send normal parms");
			writer.append("--" + boundary).append(CRLF);
			writer.append("Content-Disposition: form-data; name=\"param\"").append(CRLF);
			writer.append("Content-Type: text/plain; charset=" + UTF_8).append(CRLF);
			writer.append(CRLF).append(postParms).append(CRLF).flush();
			//
			System.out.println("2 Send text file in charset UTF_8");
			writer.append("--" + boundary).append(CRLF);
			writer.append("Content-Disposition: form-data; name=\"textFile\"; filename=\""
				+ fileTxt.getName() + "\"").append(CRLF);
			writer.append("Content-Type: text/plain; charset=" + UTF_8).append(CRLF);
			writer.append(CRLF).flush();
			Files.copy(fileTxt.toPath(), outputStream);
			outputStream.flush(); // Important before continuing with writer!
			writer.append(CRLF).flush(); // CRLF indicates end of boundary
			//
			System.out.println("3 Send binary file");
			writer.append("--" + boundary).append(CRLF);
			writer.append("Content-Disposition: form-data; name=\"binaryFile\"; filename=\""
				+ fileBin.getName() + "\"").append(CRLF);
			String fileBinContentType = URLConnection.guessContentTypeFromName(fileBin.getName());
			writer.append("Content-Type: " + fileBinContentType).append(CRLF);
			writer.append("Content-Transfer-Encoding: binary").append(CRLF);
			writer.append(CRLF).flush();
			Files.copy(fileBin.toPath(), outputStream);
			outputStream.flush(); // Important before continuing with writer!
			writer.append(CRLF).flush(); // CRLF indicates end of boundary
			//
			System.out.println("4 end of multipart/form-data");
			writer.append("--" + boundary + "--").append(CRLF).flush();
			//
			System.out.println("5 request lazily fired to get response info");
			int responseCode = ((HttpURLConnection) urlConn).getResponseCode();
			txtLines = "responseCode: " + responseCode; // should be 200
		} catch (IOException ex) {
			txtLines = ex.getMessage();
			LOGGER.log(Level.SEVERE, "#### ERROR: {0} ", txtLines);
		}
		//
		return txtLines;
	}

	//############
	public static String getField(Class clazz, String nameField) {
		//
		String txtLine = "";
		try {
			Field field = clazz.getDeclaredField(nameField);
			field.setAccessible(true);
			txtLine = (String) field.get(clazz);
		} catch (NoSuchFieldException | IllegalAccessException ex) {
			LOGGER.severe(ex.getMessage());
		}
		return txtLine;
	}

	public static Object getMethod(Class clazz, String nameMethod, Object... parms) {
		//
		Object objectReturn = "";
		try {
			int parmsCount = parms.length;
			Class<?>[] classArray = new Class<?>[parmsCount];
			for (int ictr = 0; ictr < parmsCount; ictr++) {
				classArray[ictr] = parms[ictr].getClass();
			}
			//
			Object objectInstance = clazz.getDeclaredConstructor().newInstance();
			Method method = clazz.getDeclaredMethod(nameMethod, classArray);
			method.setAccessible(true);
			objectReturn = method.invoke(objectInstance, parms);
		} catch (NoSuchMethodException | IllegalArgumentException | IllegalAccessException | InvocationTargetException | InstantiationException ex) {
			LOGGER.severe(ex.getMessage());
		}
		return objectReturn;
	}

	public static void putObject(Object clazz, Object object, String objectName) {
		//
		String txtLine = "";
		try {
			Field field = clazz.getClass().getDeclaredField(objectName);
			field.setAccessible(true);
			field.set(clazz, object);
		} catch (NoSuchFieldException | IllegalAccessException ex) {
			LOGGER.severe(ex.getMessage());
		}
	}

	//############
	public static List<File> getFilesFromZip(String fileName) {
		//
		// https://www.baeldung.com/java-compress-and-uncompress
		List<File> list = new ArrayList<>();
		if (fileName == null || fileName.equals("")) { fileName = FLD_SAMPLE + ZIP_SAMPLE; }
		int BUFFER_SIZE = 4096;
		String DIR_TEMP = "src/main/resources/temp/";
		String fileItem = "";
		try {
			//
			ClassLoader classLoader = ClassLoader.getSystemClassLoader();
			File fileZip = new File(classLoader.getResource(fileName).getFile());
			FileInputStream fis = new FileInputStream(fileZip);
			ZipInputStream zis = new ZipInputStream(fis);
			ZipEntry zipEntry = zis.getNextEntry();
			//
			FileOutputStream fos = null;
			byte[] bytes = null;
			int intReadLen = 0;
			File file = null;
			while (zipEntry != null) {
				//
				fileItem = zipEntry.getName();
				file = new File(DIR_TEMP + fileItem);
				intReadLen = 0;
				bytes = new byte[BUFFER_SIZE];
				fos = new FileOutputStream(file);
				while ((intReadLen = zis.read(bytes)) > 0) {
					fos.write(bytes, 0, intReadLen);
				}
				fos.close();
				zipEntry = zis.getNextEntry();
				list.add(file);
			}
		} catch (IOException ex) { LOGGER.warning(ex.getMessage()); }
		return list;
	}

	public static String getZipFileList(String fileName, String delim) {
		//
		String txtLines = "";
		if (delim == null || delim.equals("")) { delim = DLM; }
		//
		List<File> list = UtilityMain.getFilesFromZip(fileName);
		for (File file : list) { txtLines += file.getName() + delim; }
		return txtLines;
	}

	public static File putFilesIntoZip(List<File> list) { /* ??? */
		//
		// https://www.baeldung.com/java-compress-and-uncompress
		File file = null;
		return null;
	}

	//############
	public static String getXmlFileNode(String xmlfile, String xpathTxt, String delim) {
		//
		// https://howtodoinjava.com/xml/evaluate-xpath-on-xml-string/
		String txtLines = "";
		if (xmlfile == null || xmlfile.equals("")) {
			xmlfile = "src/main/resources/" + FLD_SAMPLE + XML_SAMPLE;
		}
		if (xpathTxt == null || xpathTxt.equals("")) { xpathTxt = "/catalog/book/title"; }
		if (delim == null || delim.equals("")) { delim = DLM; }
		//
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder documentBuilder = dbf.newDocumentBuilder(); // PCE
			Document document = documentBuilder.parse(xmlfile); // SAXException
			XPathFactory xpf = XPathFactory.newInstance();
			XPath xPath = xpf.newXPath();
			txtLines = (String) xPath.evaluate(xpathTxt, document, XPathConstants.STRING);
			//
			String xpathNode = "/catalog/book/@id";
			NodeList nodeList = (NodeList) xPath.evaluate(xpathNode, document, XPathConstants.NODESET);
			for (int ictr = 0; ictr < nodeList.getLength(); ictr++) {
				txtLines += delim + nodeList.item(ictr).getNodeValue();
			}
		} catch (ParserConfigurationException ex) {
			LOGGER.warning(ex.getMessage());
		} catch (SAXException ex) { LOGGER.warning(ex.getMessage()); } catch (XPathExpressionException ex) {
			LOGGER.warning(ex.getMessage());
		} catch (IOException ex) { LOGGER.warning(ex.getMessage()); }
		return txtLines;
	}

	public static String getXmlNode(String xml, String xpathTxt, String delim) {
		//
		// https://howtodoinjava.com/xml/evaluate-xpath-on-xml-string/
		String txtLines = "";
		if (delim == null || delim.equals("")) { delim = DLM; }
		//
		try {
			StringReader stringReader = new StringReader(xml);
			InputSource inputSource = new InputSource(stringReader);
			XPath xPath = XPathFactory.newInstance().newXPath();
			txtLines = xPath.evaluate(xpathTxt, inputSource);
		} catch (XPathExpressionException ex) { LOGGER.warning(ex.getMessage()); }
		return txtLines;
	}

	public static String formatXml(String xmlOld) {
		//
		String xml = "";
		if (xmlOld == null || xmlOld.equals("")) { xmlOld = getFileLocal(FLD_SAMPLE + XML_SAMPLE, ""); }
		//
		Document document = null;
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder documentBuilder = dbf.newDocumentBuilder();
			StringReader stringReader = new StringReader(xmlOld);
			InputSource inputSource = new InputSource(stringReader);
			document = documentBuilder.parse(inputSource);
		} catch (ParserConfigurationException ex) {
			LOGGER.warning(ex.getMessage());
		} catch (SAXException ex) { LOGGER.warning(ex.getMessage()); } catch (IOException ex) {
			LOGGER.warning(ex.getMessage());
		}
		try {
			StringWriter stringWriter = new StringWriter();
			StreamResult xmlOutput = new StreamResult(stringWriter);
			TransformerFactory tf = TransformerFactory.newInstance();
			// tf.setAttribute( "indent-number", 4 );
			Transformer transformer = tf.newTransformer();
			transformer.setOutputProperty(OutputKeys.METHOD, "xml");
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			DOMSource domSource = new DOMSource(document);
			transformer.transform(domSource, xmlOutput);
			xml = xmlOutput.getWriter().toString();
		} catch (TransformerConfigurationException ex) {
			LOGGER.warning(ex.getMessage());
		} catch (TransformerException ex) { LOGGER.warning(ex.getMessage()); }
		return xml;
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
			txtLine = (jsonNode.get(applicationNode)).asText();
		} catch (IOException ex) { LOGGER.warning(ex.getMessage()); }
		//
		return txtLine;
	}

	public static String parseJsonList2List(String jsonArr, int listFormat) {
		//
		String txtLines = "";
		ObjectMapper objectMapperHtml = new ObjectMapper();
		TypeReference<ArrayList<LinkedHashMap<String, String>>> typeReference
			= new TypeReference<ArrayList<LinkedHashMap<String, String>>>() {};
		ArrayList<LinkedHashMap<String, String>> arrayList = null;
		Set set = null;
		String txtKey = "";
		String txtVal = "";
		Object objVal = null;
		String PFX = PAR;
		String MID = " : ";
		String SFX = "";
		if (listFormat > 0) {
			PFX = "<tr><th>";
			MID = "</th<td>";
			SFX = "</td></tr>";
		}
		try {
			arrayList = objectMapperHtml.readValue(jsonArr, typeReference);
			for (LinkedHashMap<String, String> linkedHashMap : arrayList) {
				//
				set = linkedHashMap.keySet();
				txtKey = set.toString().substring(1, set.toString().length() - 1);
				objVal = linkedHashMap.get(txtKey);
				txtVal = objVal.toString();
				txtLines += PFX + txtKey + MID + txtVal + SFX;
			}
			System.out.println("txtLines: " + txtLines);
		} catch (JsonProcessingException ex) { LOGGER.warning(ex.getMessage()); }
		return txtLines;
	}
}
//----
