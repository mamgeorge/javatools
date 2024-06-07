package samples;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDateTime;
import java.util.Random;
import java.util.logging.Logger;

import static java.time.format.DateTimeFormatter.ISO_DATE_TIME;

// https://www3.ntu.edu.sg/home/ehchua/programming/java/J4b_CustomGraphics.html
public class SwingConsole extends JPanel {

	public static final String EXIT = "EXIT";
	public static final String TIME = "TIME";
	public static final String WARP = "WARP";
	public static final String FIRE = "FIRE";
	public static final Logger LOGGER = Logger.getLogger(SwingConsole.class.getName());
	public static final Action ACTION_EXIT = new AbstractAction(EXIT) {
		public void actionPerformed(ActionEvent ae) {
			System.out.println("DONE");
			System.exit(0);
		}
	};
	public static final Action ACTION_TIME = new AbstractAction(TIME) {
		public void actionPerformed(ActionEvent ae) {
			System.out.println(ISO_DATE_TIME.format(LocalDateTime.now()));
		}
	};
	public static final Dimension DIMENSION_BTNS = new Dimension(80, 20);
	public static final Font FONT_MAIN = new Font("Verdana", Font.BOLD, 14);
	public static final Color COLOR_GRA = new Color(20, 20, 20);
	public static final Color COLOR_RED = new Color(160, 060, 060);
	public static final Color COLOR_BLU = new Color(100, 100, 160);
	public static final Color COLOR_GRN = new Color(100, 160, 100);
	public static final Color COLOR_PUR = new Color(160, 100, 160);
	public AnyJComponent ANYJCOMPONENT = new AnyJComponent();

	private static final String TITLE = "SwingConsole";
	private static final int BFR = 10;
	private static final int MXH = 600;
	private static final int MXV = 400;
	private static final int MNH = 0;
	private static final int MNV = 0;
	private static final Dimension dimension = new Dimension(MXH + 20, MXV + 20);
	private static final Random random = new Random();

	public static void main(String[] strings) {

		LOGGER.info(TITLE);
		System.out.println("ISO_DATE_TIME: " + ISO_DATE_TIME.format(LocalDateTime.now()));
		new SwingConsole().showField();
		System.out.println("DONE");
	}

	public void showFrame( ) {

		JFrame jFrame = new JFrame();
		jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		jFrame.setLocationRelativeTo(null);
		jFrame.setSize(dimension);

		JButton jButton_EXIT = getJButton(EXIT, COLOR_RED, DIMENSION_BTNS, ACTION_EXIT);
		JButton jButton_TIME = getJButton(TIME, COLOR_GRN, DIMENSION_BTNS, ACTION_TIME);
		JLabel jLabel = new JLabel("TEST");
		jLabel.setForeground(Color.black);

		jFrame.add(jButton_EXIT, BorderLayout.WEST);
		jFrame.add(jButton_TIME, BorderLayout.CENTER);
		jFrame.add(jLabel, BorderLayout.EAST);
		jFrame.setVisible(true);
	}

	public void showField( ) {

		// create the frame & container
		JFrame jFrame = new JFrame();
		jFrame.setTitle(TITLE);
		jFrame.setSize(dimension);
		jFrame.setLocationRelativeTo(null);
		JFrame.setDefaultLookAndFeelDecorated(true);
		jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// create the component object
		JButton jButton_EXIT = getJButton(EXIT, COLOR_RED, DIMENSION_BTNS, ACTION_EXIT);
		JButton jButton_TIME = getJButton(TIME, COLOR_GRN, DIMENSION_BTNS, ACTION_TIME);
		JButton jButton_WARP = getJButton(WARP, COLOR_BLU, DIMENSION_BTNS, null);
		JButton jButton_FIRE = getJButton(FIRE, COLOR_PUR, DIMENSION_BTNS, null);
		jButton_WARP.addActionListener(new AnyActionListener());
		jButton_FIRE.addActionListener(new AnyActionListener());
		JPanel jPanel = new JPanel();
		jPanel.add(jButton_EXIT);
		jPanel.add(jButton_TIME);
		jPanel.add(jButton_WARP);
		jPanel.add(jButton_FIRE);

		Container containerJFrame = jFrame.getContentPane();
		containerJFrame.setBackground(COLOR_GRA);
		containerJFrame.add(jPanel, BorderLayout.SOUTH);
		containerJFrame.add(ANYJCOMPONENT, BorderLayout.CENTER);
		jFrame.setVisible(true);
	}

	public static JButton getJButton(String name, Color color, Dimension dimension, Action action) {

		JButton jButton = new JButton(name);
		if ( action == null ) { }
		else { jButton.setAction(action); }
		jButton.setPreferredSize(dimension);
		jButton.setMinimumSize(dimension);
		jButton.setMaximumSize(dimension);
		jButton.setBackground(color);
		jButton.setForeground(Color.white);
		jButton.setName(name);
		jButton.setToolTipText(name);
		return jButton;
	}

	class AnyJComponent extends JComponent {

		public void paint(Graphics graphics) {

			setGrid(graphics);
			for ( int ictr = 0; ictr < 10; ++ictr )
				setPoints(graphics);
		}

		public void setGrid(Graphics graphics) {

			Graphics2D g2d = (Graphics2D) graphics;
			// frame
			graphics.setColor(Color.red);
			g2d.drawLine(MNH + BFR, MNV + BFR, MXH - BFR, MNV + BFR);
			graphics.setColor(Color.blue);
			g2d.drawLine(MXH - BFR, MNV + BFR, MNH + BFR, MXV - BFR - 70);
			graphics.setColor(Color.green);
			g2d.drawLine(MNH + BFR, MXV - BFR - 70, MXH - BFR, MXV - BFR - 70);
			graphics.setColor(Color.magenta);
			g2d.drawLine(MXH - BFR, MXV - BFR - 70, MNH + BFR, MNV + BFR);

			// center
			graphics.setColor(Color.yellow);
			graphics.drawOval(MXH / 2 - BFR + 7, MXV / 2 - BFR - 30, 10, 10); // drawOval/fillOval
		}

		public void setBeam(Graphics graphics) {

			Graphics2D g2d = (Graphics2D) graphics;
			int trgx = ( MXH / 2 - BFR + 10 ) + ( random.nextInt(100)-50 );
			int trgy = ( MXV / 2 - BFR - 25 ) + ( random.nextInt(100)-50 );

			for ( int ictr = 0; ictr < 2; ictr++ ) {

				graphics.setColor(Color.yellow);
				g2d.drawLine(MNH + BFR, MXV / 2, trgx, trgy);
				try { Thread.sleep(50); } catch (InterruptedException ex) { System.out.println("ERROR: " + ex.getMessage()); }
				graphics.setColor(Color.yellow);
				g2d.drawLine(MXH - BFR, MXV / 2, trgx, trgy);
				try { Thread.sleep(50); } catch (InterruptedException ex) { System.out.println("ERROR: " + ex.getMessage()); }

				graphics.setColor(COLOR_GRA);
				g2d.drawLine(MNH + BFR, MXV / 2, trgx, trgy);
				try { Thread.sleep(20); } catch (InterruptedException ex) { System.out.println("ERROR: " + ex.getMessage()); }
				graphics.setColor(COLOR_GRA);
				g2d.drawLine(MXH - BFR, MXV / 2, trgx, trgy);
				try { Thread.sleep(20); } catch (InterruptedException ex) { System.out.println("ERROR: " + ex.getMessage()); }
			}
		}

		public void setPoints(Graphics graphics) {

			int intDensity = 40;
			graphics.setColor(Color.white);
			int xval, yval, trad;
			for ( int ictr = 0; ictr < intDensity; ++ictr ) {
				xval = random.nextInt(MXH - BFR);
				yval = random.nextInt(MXV - BFR);
				trad = random.nextInt(5);
				graphics.fillOval(xval, yval, trad, trad);
			}
		}
	}

	class AnyActionListener implements ActionListener {

		public void actionPerformed(ActionEvent actionEvent) {

			// actionEvent.getSource().toString()
			System.out.print(actionEvent.getActionCommand());

			if ( actionEvent.getActionCommand().equals(FIRE) ) {
				ANYJCOMPONENT.setBeam(ANYJCOMPONENT.getGraphics());
			}

			// if ( actionEvent.getSource() instanceof JComponent )
			if ( actionEvent.getActionCommand().equals(WARP) ) {
				ANYJCOMPONENT.repaint();
			}
		}
	}
}