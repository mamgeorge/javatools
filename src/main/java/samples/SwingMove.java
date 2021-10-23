// https://codereview.stackexchange.com/questions/29630/simple-java-animation-with-swing

package samples;

import static java.awt.Color.BLUE;
import static java.awt.Color.GREEN;
import static java.awt.Color.BLACK;
import static java.awt.Color.WHITE;

import static samples.SwingSample.COLOR_RED;
import static samples.SwingSample.COLOR_GRN;
import static samples.SwingSample.COLOR_PUR;
import static samples.SwingSample.ACTION_EXIT;
import static samples.SwingSample.ACTION_TIME;
import static samples.SwingSample.getJButton;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class SwingMove {
	//
	JFrame jFrame;
	private AnyJComponent ANYJCOMPONENT = new AnyJComponent( );
	private JButton jButton_EXIT = null;
	private JButton jButton_TIME = null;
	private JButton jButton_L = null;
	private JButton jButton_R = null;
	private JButton jButton_U = null;
	private JButton jButton_D = null;
	private final Dimension DIMENSION_SMLR = new Dimension( 45 , 20 ) ;

	private int oneX = 140;
	private int oneY = 110;
	private int maxWdt = 275;
	private int maxHth = 250;
	private int minWdt = 7;
	private int minHth = 7;
	private int ballx = 6;
	private int bally = 6;
	private int timer = 1;
	private int inc = 10;

	public static void main(String... strings) { new SwingMove().startup(); }

	private void startup() {
		//
		jFrame = new JFrame("SwingMove");
		jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		jFrame.getContentPane();
		//
		jButton_EXIT	= getJButton( "X", COLOR_RED, DIMENSION_SMLR, ACTION_EXIT );
		jButton_TIME	= getJButton( "T", COLOR_GRN, DIMENSION_SMLR, ACTION_TIME );
		jButton_L		= getJButton( "<", COLOR_PUR, DIMENSION_SMLR, null );
		jButton_U		= getJButton( "^", COLOR_PUR, DIMENSION_SMLR, null );
		jButton_D		= getJButton( "V", COLOR_PUR, DIMENSION_SMLR, null );
		jButton_R		= getJButton( ">", COLOR_PUR, DIMENSION_SMLR, null );
		AnyActionListener anyActionListener = new AnyActionListener( );
		jButton_L.addActionListener( anyActionListener ) ;
		jButton_U.addActionListener( anyActionListener ) ;
		jButton_D.addActionListener( anyActionListener ) ;
		jButton_R.addActionListener( anyActionListener ) ;
		//
		JPanel jPanel = new JPanel();
		jPanel.add( jButton_EXIT );
		jPanel.add( jButton_L );
		jPanel.add( jButton_U );
		jPanel.add( jButton_D );
		jPanel.add( jButton_R );
		//
		//
		Container containerJFrame = jFrame.getContentPane( );
		containerJFrame.add( jPanel, BorderLayout.NORTH ) ;
		containerJFrame.add( ANYJCOMPONENT, BorderLayout.CENTER ) ;
		//
		jFrame.setResizable(false);
		jFrame.setSize(300, 300);
		jFrame.setLocationByPlatform(true);
		jFrame.setVisible(true);
	}

	class AnyJComponent extends JComponent {
		//
		private static final long serialVersionUID = 1L;
		public void paintComponent(Graphics graphics)
		{
			graphics.setColor(BLUE ); graphics.fillRect(0, 0, this.getWidth() - 0 , this.getHeight() - 0 );
			graphics.setColor(GREEN); graphics.fillRect(3, 3, this.getWidth() - 6 , this.getHeight() - 6 );
			graphics.setColor(BLACK); graphics.fillRect(6, 6, this.getWidth() - 12, this.getHeight() - 12);
			graphics.setColor(WHITE); graphics.	drawOval( oneX, oneY, ballx, bally );
		}
	}

	// event dispatch thread
	class AnyActionListener implements ActionListener {
		//
		public void actionPerformed( ActionEvent actionEvent ) {
			//
			// System.out.print( actionEvent.getSource( ).toString() );
			if ( actionEvent.getSource( ) == jButton_L ) { if (oneX > minWdt) { oneX = oneX - inc; } ANYJCOMPONENT.repaint(); }
			if ( actionEvent.getSource( ) == jButton_D ) { if (oneY < maxHth) { oneY = oneY + inc; } ANYJCOMPONENT.repaint(); }
			if ( actionEvent.getSource( ) == jButton_U ) { if (oneY > minHth) { oneY = oneY - inc; } ANYJCOMPONENT.repaint(); }
			if ( actionEvent.getSource( ) == jButton_R ) { if (oneX < maxWdt) { oneX = oneX + inc; } ANYJCOMPONENT.repaint(); }
			// if ( actionEvent.getSource( ) instanceof JComponent ) { ANYJCOMPONENT.repaint(); }
		}
		private void lesser() {
			oneX = oneX - 1;
			try { Thread.sleep(50); } catch ( InterruptedException ex )
			{ System.out.println( ex.getMessage() ); }
			// System.out.println( ictr );
		}
	}
}
