package samples;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.springframework.http.HttpMethod;

import java.io.IOException;
import java.io.OutputStream;
import java.time.Instant;
import java.util.logging.Logger;

import static org.springframework.http.HttpStatus.OK;

public class AnyHttpHandler implements HttpHandler {

	// https://dzone.com/articles/simple-http-server-in-java
	private static final Logger LOGGER = Logger.getLogger(AnyHttpHandler.class.getName());

	@Override public void handle(HttpExchange httpExchange) throws IOException {
		//
		String parm = "";
		String method = httpExchange.getRequestMethod();
		if (HttpMethod.GET.toString().equals(method)) {
			parm = handleGets(httpExchange);
		}
		if (HttpMethod.POST.toString().equals(method)) {
			parm = handlePost(httpExchange);
		}
		handleResponse(httpExchange, parm);
	}

	private String handleGets(HttpExchange httpExchange) {
		//
		String parm = "";
		String uri = httpExchange.getRequestURI().toString();
		if (uri.contains("?")) {
			parm = uri.split("\\?")[1];
		}
		if (uri.contains("=")) {
			parm = uri.split("=")[1];
		}
		return parm;
	}

	private String handlePost(HttpExchange httpExchange) {
		return "";
	}

	private void handleResponse(HttpExchange httpExchange, String parm) throws IOException {
		//
		OutputStream outputStream = httpExchange.getResponseBody();
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("<html><style>body { font-family: verdana; }</style><body><center>");
		stringBuilder.append("<h3>Greetings, " + parm + "!</h3>");
		stringBuilder.append("<h5>" + Instant.now() + "</h5>");
		stringBuilder.append("</center></body></html>");
		try {
			// encode HTML content
			String htmlResponse = stringBuilder.toString(); // StringEscapeUtils.escapeHtml4(...)
			httpExchange.sendResponseHeaders(OK.value(), htmlResponse.length());
			outputStream.write(htmlResponse.getBytes());
			outputStream.flush();
			outputStream.close();
		} catch (IOException ex) {
			LOGGER.severe(ex.getMessage());
		}
	}
}
