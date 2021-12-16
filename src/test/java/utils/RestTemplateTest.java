package utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.util.Assert;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import samples.AppResponse;
import samples.ClientHttpRequestInterceptorImpl;
import samples.OauthToken;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED;
import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE;
import static utils.UtilityMain.EOL;
import static utils.UtilityMain.exposeObject;

// @RunWith( MockitoJUnitRunner.class ) JUnit 4
public class RestTemplateTest {

	private static final Logger LOGGER = Logger.getLogger(RestTemplateTest.class.getName());
	private static final String TXT_URL = "http://localhost:3000";
	private static final String TXT_URLEXT = "http://httpbin.org";
	private static final String PATHFILE_LOCAL = "src/test/resources/";
	private static final String FILENAME_BOOKS = "booksCatalog.json";
	private static final String FILENAME_WAVE = "hal9000.wav";
	private static final String FRMT = "\t%-20s %s\n";
	private static final String ASSERT_MSG = "ASSERT_MSG";
	//
	private static final String TESTSERVER_DOWNMSG = "I/O error on GET Connection refused; using Mock";
	public static final String DEFAULT_OAUTH =
			"{ \"access_token\": \"TOKEN_DEFAULT\", \"token_type\": \"TYPE_DEFAULT\", \"expires_in\": " +
					"\"EXPIRES_DEFAULT\", \"id_token\": \"ID_DEFAULT\" }";
	public static final String DEFAULT_APIRSP = "{ \"userid\": \"1234567890\", \"ttslength\": \"8000\" }";

	//#### RestTemplate
	@Test public void test_RT_objects() {
		//
		StringBuilder sb = new StringBuilder();
		RestTemplate restTemplate = new RestTemplate();
		URI uri = null;
		try {
			uri = new URI(TXT_URL);
		} catch (URISyntaxException ex) {
			LOGGER.info(ex.getMessage());
		}
		HttpEntity<String> httpEntity = new HttpEntity<>("http_text");
		RequestEntity<String> requestEntity = RequestEntity.post(uri).body("request_text");
		ResponseEntity<String> responseEntity = new ResponseEntity<>("response_text", OK);
		Object[] objects = {restTemplate, httpEntity, requestEntity, responseEntity};
		//
		Arrays.stream(objects).forEach(obj -> sb.append(exposeObject(obj)));
		//
		String txtLines = sb.toString();
		System.out.print(txtLines);
		Assert.isTrue(txtLines.split(EOL).length >= 7, ASSERT_MSG);
	}

	@Test public void test_RT_getForEntity() {
		//
		String txtLines = "";
		String url = TXT_URLEXT + "/get?sid=A123456";
		//
		RestTemplate restTemplate = new RestTemplate();
		ResponseEntity<String> responseEntity = restTemplate.getForEntity(url, String.class);
		HttpStatus httpStatus = responseEntity.getStatusCode();
		String response = responseEntity.getBody().replaceAll("\n", "");
		response = response.replaceAll("  ", " ").substring(0, 80);
		String headers = responseEntity.getHeaders().toString().replaceAll(",", ",\n\t\t");
		//
		txtLines += String.format(FRMT, "getStatusCode", httpStatus);
		txtLines += String.format(FRMT, "getBody", response);
		txtLines += String.format(FRMT, "getHeaders", headers);
		//
		System.out.println(txtLines);
		Assert.isTrue(httpStatus.equals(OK), ASSERT_MSG);
	}

	@Test public void test_RT_getForEntity_simplified() {
		//
		// ResponseEntity<String> responseEntity = restTemplate.getForEntity(txtURL, String.class);
		RestTemplate restTemplate = new RestTemplate();
		ResponseEntity<String> responseEntity = getForEntity_String(restTemplate, TXT_URL);
		HttpStatus httpStatus = responseEntity.getStatusCode();
		//
		// RT.getForEntity( ) > RE.getStatusCode( ) > HS
		String txtLines = String.format("httpStatus: %s\n", httpStatus);
		System.out.println(txtLines);
		Assert.isTrue(httpStatus.equals(OK), ASSERT_MSG);
	}

	@Test public void test_RT_exchange_get() {
		//
		RestTemplate restTemplate = new RestTemplate();
		HttpEntity<String> httpEntity = new HttpEntity<>("");
		ResponseEntity<String> responseEntity = exchange_Entity(restTemplate, TXT_URL, GET, httpEntity);
		HttpStatus httpStatus = responseEntity.getStatusCode();
		//
		String txtLines = String.format("httpStatus: %s\n", httpStatus);
		System.out.println(txtLines);
		Assert.isTrue(httpStatus.equals(OK), ASSERT_MSG);
	}

	@Test public void test_RT_postForEntity_body() {
		//
		String txtLines = "";
		String url = TXT_URLEXT + "/post";
		String body = UtilityMain.getFileLocal(PATHFILE_LOCAL + "books.json");
		//
		// add headers: MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
		MultiValueMap<String, String> MVM = new LinkedMultiValueMap<>();
		MVM.add(CONTENT_TYPE, APPLICATION_FORM_URLENCODED_VALUE);
		MVM.add("AnyId", "G002875");
		MVM.add("Phone", "614-377-7835");
		HttpEntity<String> httpEntity = new HttpEntity<>(body, MVM);
		//
		// send request
		RestTemplate restTemplate = new RestTemplate();
		ResponseEntity<String> responseEntity = restTemplate.exchange(url, POST, httpEntity, String.class);
		HttpStatus httpStatus = responseEntity.getStatusCode();
		String response = responseEntity.getBody().replaceAll("\n", "");
		response = response.replaceAll("  ", " ").substring(0, 80);
		String headers = responseEntity.getHeaders().toString().replaceAll(",", ",\n\t\t");
		//
		// show
		txtLines += String.format(FRMT, "getStatusCode", httpStatus);
		txtLines += String.format(FRMT, "getBody", response);
		txtLines += String.format(FRMT, "getHeaders", headers);
		System.out.println(txtLines);
		Assert.isTrue(httpStatus.equals(OK), ASSERT_MSG);
	}

	@Test public void test_RT_postForEntity_logged() {
		//
		String txtLines = "";
		String url = TXT_URLEXT + "/post";
		String body = UtilityMain.getFileLocal(PATHFILE_LOCAL + "books.json");
		//
		// add headers: MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
		MultiValueMap<String, String> MVM = new LinkedMultiValueMap<>();
		MVM.add(CONTENT_TYPE, APPLICATION_FORM_URLENCODED_VALUE);
		MVM.add("AnyId", "G002875");
		MVM.add("Phone", "614-377-7835");
		HttpEntity<String> httpEntity = new HttpEntity<>(body, MVM);
		//
		// add logging
		SimpleClientHttpRequestFactory SCHRF = new SimpleClientHttpRequestFactory();
		BufferingClientHttpRequestFactory BCHRF = new BufferingClientHttpRequestFactory(SCHRF);
		RestTemplate restTemplate = new RestTemplate(BCHRF);
		List<ClientHttpRequestInterceptor> list = new ArrayList<>();
		ClientHttpRequestInterceptorImpl CHRII = new ClientHttpRequestInterceptorImpl();
		list.add(CHRII);
		restTemplate.setInterceptors(list);
		//
		// send request
		ResponseEntity<String> responseEntity = restTemplate.exchange(url, POST, httpEntity, String.class);
		HttpStatus httpStatus = responseEntity.getStatusCode();
		String response = responseEntity.getBody().replaceAll("\n", "");
		response = response.replaceAll("  ", " ").substring(0, 80);
		String headers = responseEntity.getHeaders().toString().replaceAll(",", ",\n\t\t");
		//
		// show
		txtLines += String.format(FRMT, "getStatusCode", httpStatus);
		txtLines += String.format(FRMT, "getBody", response);
		txtLines += String.format(FRMT, "getHeaders", headers);
		System.out.println(txtLines);
		Assert.isTrue(httpStatus.equals(OK), ASSERT_MSG);
	}

	@Test public void test_RT_postForEntity_simplified() {
		//
		RestTemplate restTemplate = new RestTemplate();
		HttpEntity<?> httpEntity = new HttpEntity<>("bar");
		if (false) {
			HttpHeaders httpHeaders = new HttpHeaders();
			httpHeaders.setContentType(APPLICATION_FORM_URLENCODED);
			HashMap<String, Object> map = new HashMap<>();
			map.put("AgentId", "G002875");
			map.put("Phone", "614-377-7835");
			httpEntity = new HttpEntity<>(map, httpHeaders);
		}
		ResponseEntity<String> responseEntity =
				exchange_Entity(restTemplate, TXT_URL + "/post", POST, httpEntity);
		HttpStatus httpStatus = responseEntity.getStatusCode();
		//
		String txtLines = String.format("httpStatus: %s\n", httpStatus);
		System.out.println(txtLines);
		Assert.isTrue(httpStatus.equals(OK), ASSERT_MSG);
	}

	@Test public void test_RT_exchange_post() {
		//
		RestTemplate restTemplate = new RestTemplate();
		HttpEntity<String> httpEntity = new HttpEntity<>("bar");
		ResponseEntity<String> responseEntity =
				exchange_Entity(restTemplate, TXT_URL + "/post", POST, httpEntity);
		HttpStatus httpStatus = responseEntity.getStatusCode();
		//
		String txtLines = String.format("httpStatus: %s\n", httpStatus);
		System.out.println(txtLines);
		Assert.isTrue(httpStatus.equals(OK), ASSERT_MSG);
	}

	@Test public void test_RT_SCHRF() {
		//
		String txtLines = "";
		int timeout = 5000;
		//
		SimpleClientHttpRequestFactory SCHRF = new SimpleClientHttpRequestFactory();
		SCHRF.setConnectTimeout(timeout);
		SCHRF.setReadTimeout(timeout);
		//
		// RestTemplate restTemplate = new RestTemplate(SCHRF);
		RestTemplate restTemplate = new RestTemplate();
		restTemplate.setRequestFactory(SCHRF);
		ResponseEntity<String> responseEntity = getForEntity_String(restTemplate, TXT_URL);
		HttpStatus httpStatus = responseEntity.getStatusCode();
		//
		txtLines += String.format("SCHRF: %s\n", SCHRF);
		txtLines += String.format("responseEntity.getBody(): %s\n", responseEntity.getBody());
		System.out.println(txtLines);
		Assert.isTrue(httpStatus.equals(OK), ASSERT_MSG);
	}

	@Test public void test_RT_HCCHRF() {
		//
		String txtLines = "";
		int timeout = 5000;
		HttpComponentsClientHttpRequestFactory HCCHRF = new HttpComponentsClientHttpRequestFactory();
		HCCHRF.setConnectTimeout(timeout);
		//
		RestTemplate restTemplate = new RestTemplate(HCCHRF);
		ResponseEntity<String> responseEntity = getForEntity_String(restTemplate, TXT_URL);
		HttpStatus httpStatus = responseEntity.getStatusCode();
		//
		txtLines += String.format("HCCHRF: %s\n", HCCHRF);
		txtLines += String.format("responseEntity.getBody(): %s\n", responseEntity.getBody());
		System.out.println(txtLines);
		Assert.isTrue(httpStatus.equals(OK), ASSERT_MSG);
	}

	@Test public void test_RT_HCCHRF_config() {
		//
		String txtLines = "";
		int timeout = 5000;
		RequestConfig requestConfig = RequestConfig.custom()
				.setConnectTimeout(timeout)
				.setConnectionRequestTimeout(timeout)
				.setSocketTimeout(timeout)
				.build();
		//
		CloseableHttpClient closeableHttpClient = HttpClientBuilder
				.create()
				.setDefaultRequestConfig(requestConfig)
				.build();
		//
		HttpComponentsClientHttpRequestFactory HCCHRF =
				new HttpComponentsClientHttpRequestFactory(closeableHttpClient);
		//
		RestTemplate restTemplate = new RestTemplate(HCCHRF);
		ResponseEntity<String> responseEntity = getForEntity_String(restTemplate, TXT_URL);
		HttpStatus httpStatus = responseEntity.getStatusCode();
		//
		txtLines += String.format("requestConfig: %s\n", requestConfig.toString());
		txtLines += String.format("closeableHttpClient: %s\n", closeableHttpClient);
		txtLines += String.format("HCCHRF: %s\n", HCCHRF);
		txtLines += String.format("responseEntity.getBody(): %s\n", responseEntity.getBody());
		System.out.println(txtLines);
		Assert.isTrue(httpStatus.equals(OK), ASSERT_MSG);
	}

	//#### ResponseEntity
	@Test public void test_RE_getStatusCode() {
		//
		RestTemplate restTemplate = new RestTemplate();
		ResponseEntity<String> responseEntity = getForEntity_String(restTemplate, TXT_URL);
		HttpStatus httpStatus = responseEntity.getStatusCode();
		//
		System.out.println("responseEntity.getStatusCode(): " + httpStatus);
		Assert.isTrue(httpStatus == OK, ASSERT_MSG);
	}

	@Test public void test_RE_responseEntity() {
		//
		RestTemplate restTemplate = new RestTemplate();
		ResponseEntity<String> responseEntity = getForEntity_String(restTemplate, TXT_URL);
		HttpStatus httpStatus = responseEntity.getStatusCode();
		//
		String txtLines = "ResponseEntity" + EOL;
		txtLines += String.format("\tgetStatusCode()\t\t %s\n", responseEntity.getStatusCode());
		txtLines += String.format("\tgetHeaders()...\t\t %s\n", responseEntity.getHeaders());
		txtLines += String.format("\tgetBody()......\t\t %s\n", responseEntity.getBody().substring(0, 20));
		System.out.println(txtLines);
		Assert.isTrue(httpStatus == OK, ASSERT_MSG);
	}

	@Test public void test_httpStatus() {
		//
		RestTemplate restTemplate = new RestTemplate();
		ResponseEntity<String> responseEntity = getForEntity_String(restTemplate, TXT_URL);
		HttpStatus httpStatus = responseEntity.getStatusCode();
		//
		String txtLines = "HttpStatus" + EOL;
		txtLines += String.format("\ttoString()\t\t %s\n", httpStatus);
		txtLines += String.format("\tseries()..\t\t %s\n", httpStatus.series());
		txtLines += String.format("\tvalue()...\t\t %s\n", httpStatus.value());
		txtLines += String.format("\tname()....\t\t %s\n", httpStatus.name());
		System.out.println(txtLines);
		Assert.isTrue(httpStatus.equals(OK), ASSERT_MSG);
	}

	//#### implementations
	@Test public void test_getOauthToken() {
		//
		String access_token = getOauthToken(TXT_URL + "/OAUTH", "ANY_PASSWORD");
		System.out.println(access_token);
		Assert.isTrue(access_token.length() >= 8, ASSERT_MSG);
	}

	@Test public void test_sendFiles2App() {
		//
		String txtUrl = TXT_URL + "/API";
		String token = getOauthToken(TXT_URL + "/OAUTH", "ANY_PASSWORD");
		String pathJson = PATHFILE_LOCAL + FILENAME_BOOKS;
		String pathWav = PATHFILE_LOCAL + FILENAME_WAVE;
		//
		String body = sendFiles2App(txtUrl, token, pathJson, pathWav);
		AppResponse appResponse = null;
		try {
			appResponse = new ObjectMapper().readValue(body, AppResponse.class);
		} catch (JsonProcessingException | IllegalArgumentException ex) {
			LOGGER.info(ex.getMessage());
		}
		//
		String txtLines = "body: " + body + EOL;
		txtLines += "getUserid: " + appResponse.getUserid() + EOL;
		txtLines += "getTtslength: " + appResponse.getTtslength() + EOL;
		int ttsLength = Integer.valueOf(appResponse.getTtslength());
		System.out.println(txtLines);
		Assert.isTrue(ttsLength > 1000, ASSERT_MSG);
	}

	//#### helpers ####
	private ResponseEntity<String> getForEntity_String(RestTemplate restTemplate, String txtUrl) {
		//
		ResponseEntity<String> responseEntity;
		try {
			responseEntity = restTemplate.getForEntity(txtUrl, String.class);
		} catch (ResourceAccessException ex) {
			LOGGER.info(TESTSERVER_DOWNMSG);
			String body = UtilityMain.getFileLocal(PATHFILE_LOCAL + FILENAME_BOOKS);
			//
			MultiValueMap<String, String> MVM = new LinkedMultiValueMap<>();
			MVM.add(CONTENT_TYPE, APPLICATION_FORM_URLENCODED_VALUE);
			MVM.add("AgentId", "G002875");
			MVM.add("Phone", "614-377-7835");
			responseEntity = new ResponseEntity<>(body, MVM, OK);
		}
		return responseEntity;
	}

	private ResponseEntity<String> exchange_Entity(RestTemplate RT, String txtUrl,
	                                               HttpMethod httpMethod, HttpEntity httpEntity) {
		//
		ResponseEntity<String> responseEntity;
		try {
			responseEntity = RT.exchange(txtUrl, httpMethod, httpEntity, String.class);
		} catch (ResourceAccessException ex) {
			LOGGER.info(TESTSERVER_DOWNMSG);
			String body = UtilityMain.getFileLocal(PATHFILE_LOCAL + FILENAME_BOOKS);
			MultiValueMap<String, String> MVM = new LinkedMultiValueMap<>();
			MVM.add(CONTENT_TYPE, APPLICATION_JSON_VALUE);
			responseEntity = new ResponseEntity<>(body, MVM, OK);
		}
		return responseEntity;
	}

	public static byte[] getFileBinary(String fileName) {
		//
		// https://www.codejava.net/java-se/file-io/how-to-read-and-write-binary-files-in-java
		byte[] bytes = null;
		try {
			bytes = Files.readAllBytes(Paths.get(fileName));
		} catch (Exception ex) {
			LOGGER.info(ex.getMessage());
		}
		System.out.println("bytes.length: " + bytes.length + " for " + fileName + EOL);
		return bytes;
	}

	public static MultipartFile getMultipartFile(String fileName) {
		//
		// https://www.codejava.net/java-se/file-io/how-to-read-and-write-binary-files-in-java
		byte[] bytes = getFileBinary(fileName);
		String MPF = MULTIPART_FORM_DATA_VALUE;
		MultipartFile multipartFile = new MockMultipartFile("audioFile", fileName, MPF, bytes);
		System.out.println("multipartFile: " + multipartFile.getSize() + " for " + fileName + EOL);
		//
		return multipartFile;
	}

	public static MultiValueMap getMvmSample() {
		//
		MultiValueMap<String, String> MVM = new LinkedMultiValueMap<>();
		MVM.add(CONTENT_TYPE, APPLICATION_JSON_VALUE);
		return MVM;
	}

	public static String getOauthToken(String txtUrl, String password) {
		//
		String access_token;
		OauthToken oauthToken = new OauthToken();
		//
		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.setContentType(APPLICATION_FORM_URLENCODED);
		//
		MultiValueMap<String, String> MVM = new LinkedMultiValueMap<>();
		MVM.add("client_id", "ANY_CLIENT");
		MVM.add("resource", "ANY_RESOURCE");
		MVM.add("username", "ANY_USERNAME");
		MVM.add("password", password);
		MVM.add("grant_typr", "password");
		//
		HttpEntity<?> httpEntity = new HttpEntity<>(MVM, httpHeaders);
		RestTemplate restTemplate = new RestTemplate();
		ResponseEntity<String> responseEntity = new ResponseEntity<>(DEFAULT_OAUTH, getMvmSample(), OK);
		try {
			responseEntity = restTemplate.postForEntity(txtUrl, httpEntity, String.class);
		} catch (ResourceAccessException ex) {
			System.out.println("RAE ERROR: " + ex.getMessage());
		}
		//
		String body = responseEntity.getBody();
		try {
			oauthToken = new ObjectMapper().readValue(body, OauthToken.class);
		} catch (JsonProcessingException | IllegalArgumentException ex) {
			LOGGER.info(ex.getMessage());
		}
		//
		access_token = oauthToken.getAccess_token();
		System.out.println("oauthToken: " + access_token.substring(access_token.length() - 8));
		return access_token;
	}

	public static String sendFiles2App(String txtUrl, String token, String pathJson, String pathWav) {
		//
		String textFile = UtilityMain.getFileLocal(pathJson);
		MultipartFile multipartFile = getMultipartFile(pathWav);
		ByteArrayResource byteArrayResource_MPF = null;
		try {
			byteArrayResource_MPF = new ByteArrayResource(multipartFile.getBytes());
		} catch (IOException ex) {
			LOGGER.info(ex.getMessage());
		}
		System.out.println("barmFile.contentLength(): " + byteArrayResource_MPF.contentLength());
		//
		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.setContentType(MULTIPART_FORM_DATA); // APPLICATION_JSON
		httpHeaders.setBasicAuth(token);
		httpHeaders.add("sessionId", "anysessionid");
		httpHeaders.add("traceId", "anytraceid");
		//
		// https://www.programcreek.com/java-api-examples/?api=org.springframework.core.io.ByteArrayResource
		// MVM.add("fileWav", new ByteArrayResource(byteWave) { @Override public String getFilename() {
		// return pathWav; } } );
		MultiValueMap<String, Object> MVM = new LinkedMultiValueMap<>();
		MVM.add("fileTxt", textFile);
		MVM.add("fileWav", byteArrayResource_MPF);
		//
		HttpEntity<?> httpEntity = new HttpEntity<>(MVM, httpHeaders);
		RestTemplate restTemplate = new RestTemplate();
		ResponseEntity<String> responseEntity = new ResponseEntity<>(DEFAULT_APIRSP, getMvmSample(), OK);
		try {
			responseEntity = restTemplate.exchange(txtUrl, POST, httpEntity, String.class);
		} catch (HttpClientErrorException | ResourceAccessException ex) {
			LOGGER.info(ex.getMessage());
		}
		//
		return responseEntity.getBody();
	}
}
