package utils;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.time.Instant;
import java.util.logging.Logger;
import javax.swing.*;

public class AdditionalClient extends JFrame implements ActionListener {

	static final Logger LOGGER = Logger.getLogger(AdditionalClient.class.getName());

	private final JTextField jTextField_fst = new JTextField(3);
	private final JTextField jTextField_sec = new JTextField(3);
	private final JTextField jTextField_msg = new JTextField(20);
	private final JLabel jLabel_plus = new JLabel("+");
	private final JLabel jLabel_equals = new JLabel("=");
	private final JLabel jLabel_sum = new JLabel();
	private final JButton addButton = new JButton("Press to see the sum of the two numbers");

	private InputStream inputStream;
	private OutputStream outputStream;
	private DataInputStream DIS;
	private DataOutputStream DOS;
	private Socket socketConnection;

	// declare attribute to tell details of remote machine and port
	// ipconfig /all | findstr "192\.168\.1 Wireless"
	private final String[] HOST_REMOTES = {"127.0.0.1", "192.168.1.4", "192.168.58.1", "192.168.238.1", "172.27.16.1"};
	private final String HOST_REMOTE = HOST_REMOTES[4];
	private final int PORT = 9000;

	public AdditionalClient() {

		//add the visual components
		add(jTextField_fst);
		add(jLabel_plus);
		add(jTextField_sec);
		add(jLabel_equals);
		add(jLabel_sum);
		add(jTextField_msg);
		add(addButton);
		//
		// configure frame
		setBackground(Color.BLACK);
		setForeground(Color.CYAN);
		setLayout(new FlowLayout());
		setTitle("Additional_Client");
		jTextField_msg.setHorizontalAlignment(JLabel.CENTER);
		addButton.addActionListener(this);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(300, 150);
		setVisible(true);
		//
		startClient();
	}

	public static void main(String[] args) {
		new AdditionalClient();
	}

	private void startClient() {
		//
		try {
			socketConnection = new Socket(HOST_REMOTE, PORT);
			jTextField_msg.setText("conn@: " + Instant.now());
			// create an input stream from the server
			inputStream = socketConnection.getInputStream();
			outputStream = socketConnection.getOutputStream();
			DIS = new DataInputStream(inputStream);
			DOS = new DataOutputStream(outputStream);
			InetAddress ina_local = socketConnection.getLocalAddress();
			String txt_host = ina_local.getHostAddress();
			System.out.println(txt_host);
			DOS.writeUTF(txt_host);
		}
		catch (UnknownHostException ex) {
			jTextField_msg.setText("host err");
			LOGGER.info("startClient(host): " + ex.getMessage());
		}
		catch (IOException ex) {
			jTextField_msg.setText("io err");
			LOGGER.info("startClient(ioex): " + ex.getMessage());
		}
	}

	@Override
	public void actionPerformed(ActionEvent actionEvent) {
		//
		try {
			int fst = Integer.parseInt(jTextField_fst.getText());
			int scd = Integer.parseInt(jTextField_sec.getText());
			DOS.writeInt(fst);
			DOS.writeInt(scd);

			//read and display the results sent back from the server
			// String results= inDataStream.readUTF();
			int results = DIS.readInt();
			jLabel_sum.setText("" + results);
		}
		catch (IOException ex) { LOGGER.info("actionPerformed: " + ex.getMessage()); }
	}
}
