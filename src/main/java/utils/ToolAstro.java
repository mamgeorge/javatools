package utils;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Iterator;
import java.util.logging.Logger;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.stream.FileImageOutputStream;

/**
 * ToolAstro.java
 *
 * http://calgary.rasc.ca/constellation.htm
 * http://www.iau.org/public/constellations/
 *
 * @author Martin
 */
public class ToolAstro {

	/** LOGGER */
	private static final Logger LOGGER = Logger.getLogger( ToolAstro.class.getName( ) );
	private static final String IMG_GIF = "gif";
	private static final String IMG_JPG = "jpg";
	private static final String IMG_PNG = "png";

	private static final String TXT_CONS = "AND ANT APS AQL AQR ARA ARI AUR BOO CAE CAM CAP CAR CAS CEN CEP CET CHA CIR CMA CMI CNC COL COM CRA CRB CRT CRU CRV CVN CYG DEL DOR DRA EQU ERI FOR GEM GRU HER HOR HYA HYI IND LAC LEO LEP LIB LMI LUP LYN LYR MEN MIC MON MUS NOR OCT OPH ORI PAV PEG PER PHE PIC PSA PSC PUP PYX RET SCL SCO SCT "
			+ "SERCD SERCP SEX SGE SGR TAU TEL TRA TRI TUC UMA UMI VEL VIR VOL VUL";
	private static final String[ ] STR_CONS = TXT_CONS.split( " " );
	private static final String TXT_PATH_ASTRO = "C:/Martin/5Personal/Astronomy/icons/";
	private static final String TXT_URL = "http://www.iau.org/static/public/constellations/";

	/**
	 * writeConsGifFromUrl.
	 *
	 * This program creates gifs from urls then saves them as gifs.
	 *
	 * @param strings
	 */
	public static void writeConsGifFromUrl( final String[ ] strings ) {
		//
		String txtIMG = "";
		//
		File file = null;
		URL url = null;
		BufferedImage bufferedImage = null;
		String txtURLPrefix = TXT_URL + "/" + IMG_GIF + "/";
		try {
			for ( String string : strings ) {
				//
				txtIMG = txtURLPrefix + string.toUpperCase( ) + "." + IMG_GIF;
				url = new URL( txtIMG );
				bufferedImage = ImageIO.read( url );
				file = new File( TXT_PATH_ASTRO + string.toUpperCase( ) + "." + IMG_GIF );
				ImageIO.write( bufferedImage , IMG_GIF , file );
			}
			//
		} catch ( IOException ex ) {
			LOGGER.severe( ex.getMessage( ) );
		}
	}

	/**
	 * writeConsPngFromUrl.
	 *
	 * This program creates gifs from urls then saves them as pngs.
	 *
	 * @param strings
	 */
	public static void writeConsPngFromUrl( final String[ ] strings ) {
		//
		String txtIMG = "";
		String txtURLPrefix = TXT_URL + "/" + IMG_GIF + "/";
		//
		URL url = null;
		BufferedImage bufferedImage = null;
		Iterator< ImageWriter > iterator = null;
		ImageWriter imageWriter = null;
		File file = null;
		FileImageOutputStream fos = null;
		IIOImage iioImage = null;
		try {
			for ( String string : strings ) {
				//
				txtIMG = txtURLPrefix + string.toUpperCase( ) + "." + IMG_GIF;
				url = new URL( txtIMG );
				bufferedImage = ImageIO.read( url );
				file = new File( TXT_PATH_ASTRO + string.toUpperCase( ) + "." + IMG_PNG );
				fos = new FileImageOutputStream( file );
				//
				iterator = ImageIO.getImageWritersBySuffix( IMG_PNG );
				imageWriter = iterator.next( );
				imageWriter.setOutput( fos );
				iioImage = new IIOImage( bufferedImage , null , null );
				imageWriter.write( null , iioImage , null );
				imageWriter.dispose( );
			}
			//
		} catch ( IOException ex ) {
			LOGGER.severe( ex.getMessage( ) );
		}
	}

	/**
	 * writeConsJpgFromDir.
	 *
	 * This program takes a list of gifs and convets them to jpgs.
	 *
	 * @param strings
	 */
	public static void writeConsJpgFromDir( final String[ ] strings ) {
		//
		String txtIMG = "";
		String txtDIRPrefix = TXT_PATH_ASTRO + "/originals/";
		//
		BufferedImage bufferedImage = null;
		Iterator< ImageWriter > iterator = null;
		ImageWriter imageWriter = null;
		File fileIn = null;
		File fileOt = null;
		FileImageOutputStream fos = null;
		BufferedImage bufferedImageWht = null;
		Graphics2D graphics2D = null;
		int intW = 0;
		int intH = 0;
		IIOImage iioImage = null;
		try {
			for ( String string : strings ) {
				//
				txtIMG = txtDIRPrefix + string.toUpperCase( ) + "." + IMG_GIF;
				fileIn = new File( txtIMG );
				bufferedImage = ImageIO.read( fileIn );
				fileOt = new File( TXT_PATH_ASTRO + string.toUpperCase( ) + "." + IMG_JPG );
				fos = new FileImageOutputStream( fileOt );
				//
				iterator = ImageIO.getImageWritersBySuffix( IMG_JPG );
				imageWriter = iterator.next( );
				imageWriter.setOutput( fos );
				//
				intW = bufferedImage.getWidth( );
				intH = bufferedImage.getHeight( );
				bufferedImageWht = new BufferedImage( intW , intH , BufferedImage.TYPE_INT_RGB );
				graphics2D = bufferedImageWht.createGraphics( );
				graphics2D.setColor( Color.WHITE );
				graphics2D.fillRect( 0 , 0 , intW , intH );
				graphics2D.drawRenderedImage( bufferedImage , null );
				graphics2D.dispose( );
				//
				iioImage = new IIOImage( bufferedImageWht , null , null );
				imageWriter.write( null , iioImage , null );
				imageWriter.dispose( );
			}
			//
		} catch ( IOException ex ) {
			LOGGER.severe( ex.getMessage( ) );
		}
	}
}
