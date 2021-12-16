package samples;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import utils.UtilityMain;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Set;

import static java.nio.charset.StandardCharsets.UTF_8;

public class ClientHttpRequestInterceptorImpl implements ClientHttpRequestInterceptor {

	@Override
	public ClientHttpResponse intercept(HttpRequest httpRequest, byte[] bytes, ClientHttpRequestExecution CHRE) {
		//
		ClientHttpResponse clientHttpResponse = null;
		try {
			System.out.println(traceRequest(httpRequest, bytes));
			clientHttpResponse = CHRE.execute(httpRequest, bytes);
			System.out.println(traceResponse(clientHttpResponse));
		} catch (IOException ex) {
			System.out.println("ERROR: " + ex.getMessage());
		}
		return clientHttpResponse;
	}

	private String traceRequest(HttpRequest httpRequest, byte[] bytes) {
		//
		String txtLines = "traceRequest: " + "\n";
		String FRMT = "\t%-20s %s\n";
		txtLines += "httpRequest: " + UtilityMain.exposeObject(httpRequest);
		//
		HttpHeaders httpHeaders = httpRequest.getHeaders();
		Set<String> set = httpHeaders.keySet();
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("\n");
		set.forEach(hdr -> stringBuilder.append(String.format(FRMT, hdr, httpHeaders.get(hdr))));
		txtLines += "httpHeaders: " + stringBuilder;
		//
		txtLines += "body: " + new String(bytes, UTF_8);
		return txtLines;
	}

	private String traceResponse(ClientHttpResponse clientHttpResponse) {
		//
		String txtLines = "traceResponse: " + "\n";
		StringBuilder stringBuilder = new StringBuilder();
		try {
			InputStream inputStream = clientHttpResponse.getBody();
			InputStreamReader ISR = new InputStreamReader(inputStream);
			BufferedReader bufferedReader = new BufferedReader(ISR);
			String txtLine;
			while ((txtLine = bufferedReader.readLine()) != null) {
				stringBuilder.append(txtLine);
			}
		} catch (IOException ex) {
			System.out.println("ERROR: " + ex.getMessage());
		}
		txtLines += stringBuilder.toString();
		return txtLines;
	}
}
