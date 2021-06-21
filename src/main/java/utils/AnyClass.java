package utils;

import static java.time.format.DateTimeFormatter.ISO_DATE_TIME;
import java.util.Date;
import java.time.LocalDateTime;

public class AnyClass {
	
	public static void main( String[ ] args ) {
		//
		System.out.println( showTime( ) );
		System.out.println( "DONE" );
	}	
	
	public static String showTime( ) {
		//
		LocalDateTime localDateTime = LocalDateTime.now( );
		String txtLine = ISO_DATE_TIME.format(localDateTime);
		return txtLine;
	}	
}
