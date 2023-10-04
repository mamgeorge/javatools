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
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import static java.nio.charset.StandardCharsets.UTF_8;
import static javax.xml.transform.OutputKeys.ENCODING;
import static javax.xml.transform.OutputKeys.INDENT;
import static javax.xml.transform.OutputKeys.METHOD;
import static javax.xml.transform.OutputKeys.OMIT_XML_DECLARATION;
import static utils.UtilityMain.LOGGER;
import static utils.UtilityMain.TAB;

// note: xml xPathupdates required javax DocumentBuilders, w3c Documents, jaxen xPaths, javax Transformers
public class UtilityFormats {

	public static final String INDENT_AMOUNT = "{http://xml.apache.org/xslt}indent-amount";

	public static String getXmlNodeFromXPath(String xml, String xpathTxt) {
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

		String xml = "";
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
			//
			Transformer transformer = transformerFactory.newTransformer();
			transformer.setOutputProperty(METHOD, "xml");
			transformer.setOutputProperty(INDENT_AMOUNT, "4");
			transformer.setOutputProperty(OMIT_XML_DECLARATION, "no");
			transformer.setOutputProperty(INDENT, "yes");
			transformer.setOutputProperty(ENCODING, UTF_8.toString());
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
			transformer.setOutputProperty(INDENT, "yes");
			//	transformer.setOutputProperty(MEDIA_TYPE, "MediaType.TEXT_XML_VALUE");
			transformer.setOutputProperty(ENCODING, UTF_8.toString());
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

	public static String parseYaml2JsonNode(String yaml, String applicationNode) {

		String txtLine = "";
		try {
			ClassLoader classLoader = ClassLoader.getSystemClassLoader();
			InputStream inputStream = classLoader.getResourceAsStream(yaml);
			//
			// create yaml from file
			YAMLFactory yamlFactory = new YAMLFactory();
			ObjectMapper objectMapperYaml = new ObjectMapper(yamlFactory);
			Object objectYaml = objectMapperYaml.readValue(inputStream, Map.class);
			//
			ObjectMapper objectMapperJson = new ObjectMapper();
			String json = objectMapperJson.writeValueAsString(objectYaml);
			//
			ObjectMapper objectMapperNode = new ObjectMapper();
			JsonNode jsonNode = objectMapperNode.readTree(json);
			txtLine = ( jsonNode.get(applicationNode) ).asText();
		}
		catch (IOException ex) { LOGGER.warning(ex.getMessage()); }
		return txtLine;
	}

	public static String parseJsonList2List(String jsonArr, int listFormat) {

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
