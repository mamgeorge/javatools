package samples;

import java.time.LocalDateTime;

import static java.time.format.DateTimeFormatter.ISO_DATE_TIME;

public class AnyClass {

	public static void main(String[] args) {
		//
		System.out.println(showTime());
		System.out.println("DONE");
	}

	public static String showTime( ) {
		//
		LocalDateTime localDateTime = LocalDateTime.now();
		String txtLine = ISO_DATE_TIME.format(localDateTime);
		return txtLine;
	}
}
