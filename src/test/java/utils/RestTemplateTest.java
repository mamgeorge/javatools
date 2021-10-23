package utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.util.Assert;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.OK;
import static utils.UtilityMain.EOL;
import static utils.UtilityMain.exposeObject;

// @RunWith( MockitoJUnitRunner.class ) JUnit 4
public class RestTemplateTest {

	private static final Logger LOGGER = Logger.getLogger(RestTemplateTest.class.getName());
	private static final String TXT_URL = "http://localhost:3000";
	private static final String PATHFILE_LOCALJSON = "src/test/resources/" + "booksCatalog.json";
	private static final String ASSERT_MSG = "ASSERT_MSG";
	private static final String TESTSERVER_DOWNMSG = "I/O error on GET Connection refused; using Mock";

	//#### RestTemplate
	@Test public void test_RT_objects() throws URISyntaxException {
		//
		StringBuilder sb = new StringBuilder();
		RestTemplate restTemplate = new RestTemplate();
		HttpEntity<String> httpEntity = new HttpEntity<>("http_text");
		RequestEntity<String> requestEntity = RequestEntity.post(new URI(TXT_URL)).body("request_text");
		ResponseEntity<String> responseEntity = new ResponseEntity<>("response_text", OK);
		Object[] objects = {restTemplate, httpEntity,requestEntity,responseEntity};
		//
		Arrays.stream(objects).forEach(obj -> sb.append(exposeObject(obj)));
		//
		String txtLines = sb.toString();
		System.out.print(txtLines);
		Assert.isTrue(txtLines.split(EOL).length >= 7, ASSERT_MSG);
	}

	@Test public void test_RT_getForEntity() {
		//
		// ResponseEntity<String> responseEntity = restTemplate.getForEntity(txtURL, String.class);
		RestTemplate restTemplate = new RestTemplate();
		ResponseEntity<String> responseEntity = getForEntity_String(restTemplate, TXT_URL);
		HttpStatus httpStatus = responseEntity.getStatusCode();
		//
		// RT.getForEntity( ) > RE.getStatusCode( ) > HS
		String txtLines = String.format("httpStatus: %s\n", httpStatus.toString());
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
		String txtLines = String.format("httpStatus: %s\n", httpStatus.toString());
		System.out.println(txtLines);
		Assert.isTrue(httpStatus.equals(OK), ASSERT_MSG);
	}

	@Test public void test_RT_postForEntity() {
		//
		RestTemplate restTemplate = new RestTemplate();
		HttpEntity<?> httpEntity = new HttpEntity<>("bar");
		if (false) {
			HttpHeaders httpHeaders = new HttpHeaders();
			httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
			HashMap<String, Object> map = new HashMap<>();
			map.put("AgentId", "G002875");
			map.put("Phone", "614-377-7835");
			httpEntity = new HttpEntity<>(map, httpHeaders);
		}
		ResponseEntity<String> responseEntity =
			exchange_Entity(restTemplate, TXT_URL + "/post", POST, httpEntity);
		HttpStatus httpStatus = responseEntity.getStatusCode();
		//
		String txtLines = String.format("httpStatus: %s\n", httpStatus.toString());
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
		String txtLines = String.format("httpStatus: %s\n", httpStatus.toString());
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
		System.out.println("responseEntity.getStatusCode(): " + httpStatus.toString());
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

	//#### HttpStatus
	@Test public void test_httpStatus() {
		//
		RestTemplate restTemplate = new RestTemplate();
		ResponseEntity<String> responseEntity = getForEntity_String(restTemplate, TXT_URL);
		HttpStatus httpStatus = responseEntity.getStatusCode();
		//
		String txtLines = "HttpStatus" + EOL;
		txtLines += String.format("\ttoString()\t\t %s\n", httpStatus.toString());
		txtLines += String.format("\tseries()..\t\t %s\n", httpStatus.series());
		txtLines += String.format("\tvalue()...\t\t %s\n", httpStatus.value());
		txtLines += String.format("\tname()....\t\t %s\n", httpStatus.name());
		System.out.println(txtLines);
		Assert.isTrue(httpStatus.equals(OK), ASSERT_MSG);
	}

	//#### helpers ####
	private ResponseEntity<String> getForEntity_String(RestTemplate restTemplate, String txtUrl) {
		//
		ResponseEntity<String> responseEntity;
		try {
			responseEntity = restTemplate.getForEntity(txtUrl, String.class);
		}
		catch (ResourceAccessException ex) {
			System.out.println(TESTSERVER_DOWNMSG);
			String body = UtilityMain.getFileLocal(PATHFILE_LOCALJSON, "");
			HttpHeaders httpHeaders = new HttpHeaders();
			httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
			Map<String, Object> map = new HashMap<>();
			map.put("AgentId", "G002875");
			map.put("Phone", "614-377-7835");
			HttpEntity<Map<String, Object>> httpEntity = new HttpEntity<>(map, httpHeaders);
			responseEntity = new ResponseEntity<>(body, OK);
		}
		return responseEntity;
	}

	private ResponseEntity<String> exchange_Entity(RestTemplate restTemplate, String txtUrl,
		HttpMethod httpMethod, HttpEntity httpEntity) {
		//
		ResponseEntity<String> responseEntity;
		try {
			responseEntity = restTemplate
				.exchange(txtUrl, httpMethod, httpEntity, String.class);
		}
		catch (ResourceAccessException ex) {
			System.out.println(TESTSERVER_DOWNMSG);
			responseEntity = new ResponseEntity<>(OK);
		}
		return responseEntity;
	}
}
