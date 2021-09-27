package utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.util.Assert;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.logging.Logger;

import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.OK;

public class RestTemplateTest {

	private static final Logger LOGGER = Logger.getLogger(RestTemplateTest.class.getName());
	private static final String TXT_URL = "http://localhost:3000";
	private static final String JSON_PATH = "/catalog/book/0/author";
	private static final String ASSERT_MSG = "ASSERT_MSG";

	@Test public void test_objectMapper() {
		//
		String txtLine = "", txtURL = TXT_URL + "/json";
		RestTemplate restTemplate = new RestTemplate();
		ResponseEntity<String> responseEntity = restTemplate.getForEntity(txtURL, String.class);
		String body = responseEntity.getBody();
		try {
			ObjectMapper objectMapper = new ObjectMapper();
			JsonNode jsonNodeRoot = objectMapper.readTree(body);
			JsonNode jsonNodeAt = jsonNodeRoot.at(JSON_PATH);
			txtLine = jsonNodeAt.asText();
			// txtLine = jsonNodeRoot.at(JsonPointer.compile(jsonPath)).asText();
		} catch (JsonProcessingException ex) { LOGGER.severe(ex.getMessage()); }
		//
		System.out.println("jsonNodeAt.asText(): " + txtLine);
		Assert.isTrue(txtLine.equals("Gambardella , Matthew"));
	}

	@Test public void test_RT_getForEntity() {
		//
		RestTemplate restTemplate = new RestTemplate();
		ResponseEntity<String> responseEntity = restTemplate.getForEntity(TXT_URL, String.class);
		//
		String txtLine = responseEntity.getStatusCode().toString();
		System.out.println("RT > RE > HS; getForEntity: " + txtLine);
		Assert.isTrue(txtLine.equals(OK.toString()), ASSERT_MSG);
	}

	@Test public void test_RT_exchange_get() {
		//
		RestTemplate restTemplate = new RestTemplate();
		HttpEntity<String> httpEntityReq = new HttpEntity<>("");
		ResponseEntity<String> responseEntity = restTemplate
			.exchange(TXT_URL, GET, httpEntityReq, String.class);
		//
		String txtLine = responseEntity.getStatusCode().toString();
		System.out.println("RT > RE > HS; exchange: " + txtLine);
		Assert.isTrue(txtLine.equals(OK.toString()), ASSERT_MSG);
	}

	@Test public void test_RT_postForEntity() {
		//
		RestTemplate restTemplate = new RestTemplate();
		HttpEntity<?> request = null;
		if(false) {
			request = new HttpEntity<String>("bar");
		}
		else {
			HttpHeaders httpHeaders = new HttpHeaders();
			httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
			MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
			map.add("id", "1000");
			request = new HttpEntity<>(map, httpHeaders);
		}
		ResponseEntity<String> responseEntity =
			restTemplate.postForEntity(TXT_URL + "/post", request, String.class);
		//
		String txtLine = responseEntity.getStatusCode().toString();
		System.out.println("RT > RE > HS; postForEntity: " + txtLine);
		Assert.isTrue(txtLine.equals(OK.toString()), ASSERT_MSG);
	}

	@Test public void test_RT_exchange_post() {
		//
		RestTemplate restTemplate = new RestTemplate();
		HttpEntity<String> httpEntityReq = new HttpEntity<>("bar");
		ResponseEntity<String> responseEntity = restTemplate
			.exchange(TXT_URL + "/post", POST, httpEntityReq, String.class);
		//
		String txtLine = responseEntity.getStatusCode().toString();
		System.out.println("RT > RE > HS; exchange: " + txtLine);
		Assert.isTrue(txtLine.equals(OK.toString()), ASSERT_MSG);
	}

	@Test public void test_RT_HCCHRF() {
		//
		int timeout = 5000;
		HttpComponentsClientHttpRequestFactory HCCHRF = null;
		if(false) {
			HCCHRF = new HttpComponentsClientHttpRequestFactory();
			HCCHRF.setConnectTimeout(timeout);
			System.out.println("HCCHRF: " + HCCHRF.toString());
		} else {
			RequestConfig requestConfig = RequestConfig.custom()
				.setConnectTimeout(timeout)
				.setConnectionRequestTimeout(timeout)
				.setSocketTimeout(timeout)
				.build();
			System.out.println("requestConfig: " + requestConfig.toString());

			CloseableHttpClient closeableHttpClient = HttpClientBuilder
				.create()
				.setDefaultRequestConfig(requestConfig)
				.build();
			System.out.println("closeableHttpClient: " + closeableHttpClient.toString());

			HCCHRF = new HttpComponentsClientHttpRequestFactory(closeableHttpClient);
			System.out.println("HCCHRF: " + HCCHRF.toString());
		}
		RestTemplate restTemplate = new RestTemplate(HCCHRF);
		ResponseEntity<String> responseEntity = restTemplate.getForEntity(TXT_URL, String.class);
		//
		String txtLine = responseEntity.getBody();
		System.out.println("responseEntity: " + txtLine.substring(0,20));
		Assert.isTrue(responseEntity.getStatusCode().equals(OK), ASSERT_MSG);
	}

	@Test public void test_RE_getStatusCode() {
		//
		RestTemplate restTemplate = new RestTemplate();
		ResponseEntity<String> responseEntity = restTemplate.getForEntity(TXT_URL, String.class);
		HttpStatus httpStatus = responseEntity.getStatusCode();
		//
		String txtLines = responseEntity.getStatusCode().toString();
		System.out.println("responseEntity.getStatusCode(): " + txtLines);
		Assert.isTrue(httpStatus == OK, ASSERT_MSG);
	}

	@Test public void test_RE_responseEntity() {
		//
		RestTemplate restTemplate = new RestTemplate();
		ResponseEntity<String> responseEntity = restTemplate.getForEntity(TXT_URL, String.class);
		HttpStatus httpStatus = responseEntity.getStatusCode();
		//
		String txtLines = "";
		txtLines += "ResponseEntity" + "\n";
		txtLines += String.format("\tgetStatusCode()\t\t %s\n", responseEntity.getStatusCode());
		txtLines += String.format("\tgetHeaders()...\t\t %s\n", responseEntity.getHeaders());
		txtLines += String.format("\tgetBody()......\t\t %s\n", responseEntity.getBody().substring(0, 10));
		System.out.println(txtLines);
		Assert.isTrue(httpStatus == OK, ASSERT_MSG);
	}

	@Test public void test_httpStatus() {
		//
		RestTemplate restTemplate = new RestTemplate();
		ResponseEntity<String> responseEntity = restTemplate.getForEntity(TXT_URL, String.class);
		HttpStatus httpStatus = responseEntity.getStatusCode();
		//
		String txtLines = "HttpStatus" + "\n";
		txtLines += String.format("\tseries()\t\t %s\n", httpStatus.series());
		txtLines += String.format("\tvalue().\t\t %s\n", httpStatus.value());
		txtLines += String.format("\tname()..\t\t %s\n", httpStatus.name());
		System.out.println(txtLines);
		Assert.isTrue(httpStatus == OK, ASSERT_MSG);
	}
}
