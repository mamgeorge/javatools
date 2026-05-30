package utils;

import jakarta.annotation.Nonnull;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;

import static java.nio.charset.StandardCharsets.UTF_8;

public class SpecialParse { // jsoup

	public static final String EOL = System.lineSeparator();
	private static final String[] TXT_URL = { "https://docs.google.com/document/d/e/2PACX-1vTMOmshQe8YvaRXi6gEPKKlsC6UpFJSMAk4mQjLm_u1gmHdVVTaeh7nBNFBRlui0sTZ-snGwZM4DBCT/pub",
	"https://docs.google.com/document/d/e/2PACX-1vSvM5gDlNvt7npYHhp_XfsJvuntUhq184By5xO_pA4b_gCWeXb6dM6ZxwN8rE6S4ghUsCj2VKR21oEP/pub"};
	public static void main(String[] args) {

		String txtDoc = getDoc(TXT_URL[1]);
		String parsedDoc = parsedDoc(txtDoc);
		String renderDoc = renderDoc(parsedDoc);

		System.out.println(renderDoc);
		System.out.println("DONE");
	}

	private static String getDoc(String textUrl) {

		StringBuilder sb = new StringBuilder();
		try {
			URL url = URI.create(textUrl).toURL();
			InputStreamReader ISR = new InputStreamReader(url.openStream(), UTF_8);
			BufferedReader buffRead = new BufferedReader(ISR);

			String inputLine;
			while ((inputLine = buffRead.readLine()) != null) {
				sb.append(inputLine);
				sb.append(System.lineSeparator());
			}
			buffRead.close();

		} catch (IOException ex) {
			System.out.println("ERROR: " + ex.getMessage());
		}
		return sb.toString();
	}

	private static String parsedDoc(String txtDoc) {

		StringBuilder sb = new StringBuilder();
		Document doc = Jsoup.parse(txtDoc);
		Element table = doc.select("table").first();
		Elements rowTrs = table.select("tr");

		for (Element rowTds : rowTrs) {
			Elements cells = rowTds.select("td");
			for (Element cell : cells) {
				sb.append(cell.text() + "\t");
			}
			sb.append("\n");
		}
		return sb.toString();
	}

	private static String renderDoc(String parsedDoc) {

		String[] rowsOrg = parsedDoc.split("\n");
		String[] rows = Arrays.copyOfRange(rowsOrg, 1, rowsOrg.length);
		int rowlen = rowsOrg.length - 1;

		// create matrix & fill
		String[] qval = new String[rowlen];
		int ictr = 0;
		int rctr = rowlen;
		for (String row : rows) {

			String[] cols = row.split("\t");
			qval[ictr] = Integer.parseInt(cols[2]) + "," + rctr--
					+ "," + Integer.parseInt(cols[0]) + "," + cols[1].trim();
			ictr++;
		}
		Arrays.sort(qval);
		Collections.reverse(Arrays.asList(qval));

		String lines = remapArrays(qval, rowlen);
		return lines;
	}

	@Nonnull
	private static String remapArrays(String[] qval, int rowlen) {

		StringBuilder sb = new StringBuilder();
		int yitm = 0;
		String citm = "";
		String lines = "";
		int yctr = Integer.parseInt(qval[0].split(",")[0]);
		for (int qctr = 0; qctr < rowlen; ) {

			yitm = Integer.parseInt(qval[qctr].split(",")[0]);
			citm = qval[qctr].split(",")[3];

			if (yitm == yctr) {
				sb.append(citm);
				lines+=citm;
				if (qctr==(rowlen -1) && lines.length()<4)
				{ sb.append(" ".repeat(4-lines.length())); }
			} else {
				yctr = yitm;
				//System.out.println(line);
				if (lines.length()<4) { sb.append(" ".repeat(4-lines.length())); }
				sb.append("\n");
				lines = "";
				qctr--;
			}
			qctr++;
		}
		return sb.toString();
	}
}
