package samples;

import static java.time.format.DateTimeFormatter.ISO_DATE_TIME;
import java.util.logging.Logger;
import java.util.Random;
import java.time.LocalDateTime;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Action;
import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JComponent;

// https://www3.ntu.edu.sg/home/ehchua/programming/java/J4b_CustomGraphics.html
public class SwingSample extends JPanel {
	//
	public static final String EXIT = "EXIT";
	public static final String TIME = "TIME";
	public static final String MOVE = "MOVE";
	public static final Logger LOGGER = Logger.getLogger( SwingSample.class.getName( ) );
	public static final Action ACTION_EXIT = new AbstractAction( EXIT ) { public void actionPerformed( ActionEvent ae ) { System.out.println( "DONE" ) ; System.exit( 0 ) ; } } ;
	public static final Action ACTION_TIME = new AbstractAction( TIME ) { public void actionPerformed( ActionEvent ae ) { System.out.println( ISO_DATE_TIME.format( LocalDateTime.now( ) ) ) ; } } ;
	public static final Dimension DIMENSION_BTNS = new Dimension( 80 , 20 ) ;
	public static final Font FONT_MAIN = new Font( "Verdana" , Font.BOLD , 14 ) ;
	public static final Color COLOR_GRA = new Color( 20 , 20 , 20 );
	public static final Color COLOR_RED = new Color( 160, 060, 060);
	public static final Color COLOR_BLU = new Color( 100, 100, 160);
	public static final Color COLOR_GRN = new Color( 100, 160, 100);
	public static final Color COLOR_PUR = new Color( 160, 100, 160);
	public AnyJComponent ANYJCOMPONENT = new AnyJComponent( );
	
	private static final String TITLE = "SwingSample";
	private static final int BFR = 10;
	private static final int MXH = 600 ;
	private static final int MXV = 400 ;
	private static final int MNH = 0 ;
	private static final int MNV = 0 ;
	private static final Dimension dimension = new Dimension ( MXH+20 , MXV+20 ) ;

	public static void main( String[] strings ) {
		//
		LOGGER.info( TITLE );
		System.out.println( "USERNAME: " + System.getenv( "USERNAME" ) );
		System.out.println( "ISO_DATE_TIME: " + ISO_DATE_TIME.format( LocalDateTime.now( ) ) );
		new SwingSample().showField();
		System.out.println( "DONE" );
	}

	public void showFrame( ) {
		//
		JFrame jFrame = new JFrame( ) ;
		jFrame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
		jFrame.setLocationRelativeTo( null );
		jFrame.setSize( dimension );
		//
		JButton jButton_EXIT = getJButton( EXIT, COLOR_RED, DIMENSION_BTNS, ACTION_EXIT );
		JButton jButton_TIME = getJButton( TIME, COLOR_GRN, DIMENSION_BTNS, ACTION_TIME );
		JLabel jLabel = new JLabel( "TEST" );
		jLabel.setForeground( Color.black ) ;
		//
		jFrame.add( jButton_EXIT, BorderLayout.WEST );
		jFrame.add( jButton_TIME, BorderLayout.CENTER );
		jFrame.add( jLabel, BorderLayout.EAST );
		jFrame.setVisible( true ) ;
	}

	public void showField( ) {
		// create the frame & container
		JFrame jFrame = new JFrame( ) ;
		jFrame.setTitle( TITLE );
		jFrame.setSize( dimension );
		jFrame.setLocationRelativeTo( null );
		jFrame.setDefaultLookAndFeelDecorated( true );
		jFrame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
		//
		// create the component object
		JButton jButton_EXIT = getJButton( EXIT, COLOR_RED, DIMENSION_BTNS, ACTION_EXIT );
		JButton jButton_TIME = getJButton( TIME, COLOR_GRN, DIMENSION_BTNS, ACTION_TIME );
		JButton jButton_MOVE = getJButton( MOVE, COLOR_BLU, DIMENSION_BTNS, null );
		jButton_MOVE.addActionListener( new AnyActionListener( ) ) ;
		JPanel jPanel = new JPanel();
		jPanel.add( jButton_EXIT );
		jPanel.add( jButton_TIME );
		jPanel.add( jButton_MOVE );
		//
		Container containerJFrame = jFrame.getContentPane( );
		containerJFrame.setBackground( COLOR_GRA ) ;
		containerJFrame.add( jPanel, BorderLayout.NORTH ) ;
		containerJFrame.add( ANYJCOMPONENT, BorderLayout.CENTER ) ;
		jFrame.setVisible( true ) ;
	}

	public static JButton getJButton( String name, Color color, Dimension dimension, Action action ) {
		//
		JButton jButton = new JButton( name );
		if (action == null) { } else { jButton.setAction( action ); }
		jButton.setPreferredSize( dimension ) ;
		jButton.setMinimumSize ( dimension ) ;
		jButton.setMaximumSize ( dimension ) ;
		jButton.setBackground( color ) ;
		jButton.setForeground( Color.white ) ;
		jButton.setName( name ) ;
		jButton.setToolTipText( name ) ;
		return jButton;
	}

	class AnyJComponent extends JComponent {
		//
		public void paint( Graphics graphics ) {
			//
			Graphics2D graphics2D = (Graphics2D) graphics;
			setGrid( graphics, graphics2D );
			for (int ictr = 0; ictr < 10; ++ictr )
			setPoints( graphics, graphics2D );
		}

		public void setGrid( Graphics graphics, Graphics2D g2d ) {
			//
			graphics.setColor( Color.red	) ; g2d.drawLine( MNH+BFR, MNV+BFR-00, MXH-BFR, MNV+BFR );
			graphics.setColor( Color.blue	) ; g2d.drawLine( MXH-BFR, MNV+BFR-00, MNH+BFR, MXV-BFR-70 );
			graphics.setColor( Color.green	) ; g2d.drawLine( MNH+BFR, MXV-BFR-70, MXH-BFR, MXV-BFR-70 );
			graphics.setColor( Color.magenta) ; g2d.drawLine( MXH-BFR, MXV-BFR-70, MNH+BFR, MNV+BFR );
			//
			graphics.setColor( Color.yellow );
			graphics.drawOval( MXH/2-BFR+7, MXV/2-BFR-30, 10, 10 ); // drawOval/fillOval
		}

		public void setPoints( Graphics graphics, Graphics2D g2d ) {
			//
			Random random = null;
			graphics.setColor( Color.white );
			int xval, yval, trad;
			for (int ictr = 0; ictr < 20; ++ictr ) {
				random = new Random();
				xval = random.nextInt( MXH-BFR );
				yval = random.nextInt( MXV-BFR );
				trad = random.nextInt(5);
				graphics.fillOval( xval, yval, trad, trad );
			}
		}
	}

	class AnyActionListener implements ActionListener {
		//
		public void actionPerformed( ActionEvent actionEvent ) {
			//
			System.out.print( actionEvent.getSource( ).toString() );
			// if ( actionEvent.getSource( ) == b1 ) { System.out.print( "GOOD" ) ; }
			if ( actionEvent.getSource( ) instanceof JComponent ) { ANYJCOMPONENT.repaint(); }
		}
	}
}