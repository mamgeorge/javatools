package samples;

import javax.swing.*;
import java.awt.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.Instant;
import java.util.logging.Logger;

import static utils.UtilityMain.EOL;

public class AdditionalServer extends JFrame {

	static final Logger LOGGER = Logger.getLogger(AdditionalServer.class.getName());

	private final JTextArea jTextArea = new JTextArea();
	private final int PORT = 9000;

	public AdditionalServer() {
		setTitle("Additional_Server");
		jTextArea.setBackground(Color.BLACK);
		jTextArea.setForeground(Color.GREEN);
		add("Center", jTextArea);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(400, 300);
		setVisible(true);
		startServer();
	}

	public static void main(String[] args) {
		new AdditionalServer();
	}

	private void startServer() {
		//
		boolean connected;
		while (true) {
			try {
				InetAddress INA = InetAddress.getLocalHost();
				System.out.println("INA: " + INA.toString());
				ServerSocket serverSocket = new ServerSocket(PORT, 0, INA);
				jTextArea.append("INA: " + INA + ", port " + PORT + "\n");
				jTextArea.append(Instant.now() + "\n");
				Socket SocketConnection = serverSocket.accept();
				connected = true;
				//
				// create an input stream from the client
				InputStream inputStream = SocketConnection.getInputStream();
				OutputStream outputStream = SocketConnection.getOutputStream();
				DataInputStream DIS = new DataInputStream(inputStream);
				DataOutputStream DOS = new DataOutputStream(outputStream);
				//
				// wait for a string from the client
				String txtClient = DIS.readUTF();
				jTextArea.append("Connection established with " + txtClient + EOL);
				int first, second, sum;
				while (connected) {
					//
					first = DIS.readInt();
					second = DIS.readInt();
					sum = first + second;
					jTextArea.append("First number receievd: " + first + EOL);
					jTextArea.append("Second number receievd: " + second + EOL);
					jTextArea.append("Sum returned: " + sum + "\n\n");
					DOS.writeInt(sum); // DOS.writeUTF(txtSum);
				}
			} catch (IOException ex) {
				connected = false;
				LOGGER.info(ex.getMessage());
			}
		}
	}
}
