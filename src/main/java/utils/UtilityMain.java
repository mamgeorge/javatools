package utils;

import static java.time.format.DateTimeFormatter.ISO_DATE_TIME;
import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class UtilityMain {

	public static final Logger LOGGER = Logger.getLogger( UtilityMain.class.getName( ) );

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

	public static final String FLD_SAMPLE = "static/" ;
	public static final String TXT_SAMPLE = "Genesis_01.txt" ;
	public static final String YML_SAMPLE = "application.yml" ;
	public static final String XML_SAMPLE = "xml/books.xml" ;
	public static final String JSN_SAMPLE = "xml/books.json" ;
	public static final String ZIP_SAMPLE = "xml_wav_plants_w10.zip" ;

	public static void main( String[ ] strings ) {
		//
		LOGGER.info( "UtilityMain" );
		System.out.println( showTime( ) );
		System.out.println( GREEN + "DONE" + RESET );
	}

	public static String showSys( ) {
		//
		Map<String, String> mapEnv = System.getenv( );
		Map<String, String> mapEnvTree = new TreeMap<String, String>( mapEnv );
		StringBuffer stringBuffer = new StringBuffer( );
		stringBuffer.append( "[" );
		// env.forEach( ( key , val ) -> stringBuffer.append( key + ": " + val + dlm ) );
		mapEnvTree.forEach( ( key, val ) -> {
			//
			val = val.replace( "\\", "/" );
			val = val.replace( "\"", "'" );
			stringBuffer.append( "{\"" + key + "\":\"" + val + "\"}," );
		} );
		stringBuffer.append( "\n{\"" + "USERNAME" 		+ "\":\"" + System.getenv( "USERNAME" )	+ "\"}" );
		stringBuffer.append( "\n]" );
		return stringBuffer.toString( );
	}

	public static String showTime( ) {
		//
		String txtLine = "";
		LocalDateTime localDateTime = LocalDateTime.now( );
		txtLine = ISO_DATE_TIME.format(localDateTime);
		// txtLine = new Date( ).toString( );
		return txtLine;
	}

	public static String getFileLines( String fileName , String delim ) {
		//
		// https://mkyong.com/java8/java-8-stream-read-a-file-line-by-line/
		String txtLines = "";
		if ( fileName == null || fileName.equals( "" ) ) { fileName = FLD_SAMPLE + TXT_SAMPLE; }
		if ( delim == null || delim.equals( "" ) ) { delim = DLM; }
		//
		List<String> list = new ArrayList<>( );
		try ( BufferedReader bReader = Files.newBufferedReader( Paths.get( fileName) ) ) {
			//
			list = bReader.lines( ).collect( Collectors.toList( ) );
			txtLines = String.join( "\n" , list );
			txtLines = txtLines.replaceAll( "\n" , delim );
		}
		catch (IOException ex) { LOGGER.warning( ex.getMessage( ) ); }
		//
		return txtLines;
	}

	public static String getFileLocal( String fileName , String delim ) {
		//
		// https://howtodoinjava.com/java/io/read-file-from-resources-folder/
		// File file = ResourceUtils.getFile("classpath:config/sample.txt")
		String txtLines = "";
		String urlFile = "";
		if ( fileName == null || fileName.equals( "" ) ) { fileName = FLD_SAMPLE + TXT_SAMPLE; }
		if ( delim == null || delim.equals( "" ) ) { delim = DLM; }
		try {
			//
			ClassLoader classLoader = ClassLoader.getSystemClassLoader( );
			// fails if run in: mvn exec:java -Dexec.mainClass
			// URL[] urls = ( (URLClassLoader) classLoader ).getURLs();
			// for(URL url: urls){ System.out.println(url.getFile( ) ); }
			//
			URL url = classLoader.getResource( fileName );
			urlFile = url.getFile( );
			File file = new File( urlFile );
			txtLines = new String( Files.readAllBytes( file.toPath( ) ), UTF_8 );
			txtLines = txtLines.replaceAll( "\n" , delim );
		}
		catch (IOException ex) { LOGGER.warning( ex.getMessage( ) ); }
		return txtLines;
	}

	public static String urlGet( String link ) {
		//
		String txtLines = "";
		try{
			URL url = new URL( link );
			HttpURLConnection httpConn = (HttpURLConnection) url.openConnection( );
			httpConn.setRequestMethod( "GET" );
			httpConn.setRequestProperty( "User-Agent", USER_AGENT );
		//	httpConn.setRequestProperty( "Content-Type", CONTENTTYPE_JSON );
		//	httpConn.setRequestProperty( "Authorization", "JWT " + jwtSourceId );
			httpConn.setConnectTimeout(5000);
			httpConn.setReadTimeout(5000);
			//
			int responseCode = httpConn.getResponseCode( );
			LOGGER.info( "sends GET to: " + url );
			LOGGER.info( "responseCode: " + responseCode );
			//
			InputStream inputStream = httpConn.getInputStream( );
			InputStreamReader isr = new InputStreamReader( inputStream );
			BufferedReader bufferedReader = new BufferedReader( isr );
			StringBuilder stringBuilder = new StringBuilder( );
			String txtLine = "";
			while ( ( txtLine = bufferedReader.readLine( ) ) != null ) {
				stringBuilder.append( txtLine );
			}
			txtLines = stringBuilder.toString( );
		}
		catch( IOException ex ) {
			txtLines = ex.getMessage( );
			LOGGER.log( Level.SEVERE, "#### ERROR: {0} ", txtLines );
		}
		return txtLines;
	}

	public static String urlPost( String link, String postParms ) {
		//
		// http://zetcode.com/java/getpostrequest/
		String txtLines = "";
		try{
			URL url = new URL( link );
			HttpURLConnection httpConn = (HttpURLConnection) url.openConnection( );
			httpConn.setDoOutput(true);
			httpConn.setRequestMethod( "POST" );
			httpConn.setRequestProperty( "User-Agent", USER_AGENT );
			httpConn.setRequestProperty( "Content-Type", CONTENTTYPE_FORM );
		//	httpConn.setRequestProperty( "Authorization", "JWT " + jwtSourceId );
			//
			OutputStream outputStream = httpConn.getOutputStream( );
			outputStream.write( postParms.getBytes( ) );
			outputStream.flush( );
			outputStream.close( );
			//
			int responseCode = httpConn.getResponseCode( );
			LOGGER.info( "Sending POST : " + url );
			LOGGER.info( "Response code: " + responseCode );
			//
			InputStream inputStream = httpConn.getInputStream( );
			InputStreamReader isr = new InputStreamReader( inputStream );
			BufferedReader bufferedReader = null;
			StringBuffer stringBuffer = new StringBuffer( );
			String txtLine = "";
			if (responseCode == HttpURLConnection.HTTP_OK ) {
				//
				bufferedReader = new BufferedReader(isr);
				while ( ( txtLine = bufferedReader.readLine( ) ) != null ) {
					stringBuffer.append(txtLine);
				}
				bufferedReader.close( );
				txtLines = stringBuffer.toString( );
			}
			else {
				LOGGER.info( "POST failed to: " +  link );
			}
		}
		catch( IOException ex ) {
			txtLines = ex.getMessage( );
			LOGGER.log( Level.SEVERE, "#### ERROR: {0} ", txtLines );
		}
		return txtLines;
	}
}
//----
