package utils;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Enumeration;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static utils.UtilityMain.EOL;

/**
 * AES      Advanced Encryption Standard
 * GCM      Galois Counter Mode
 * PBKDF2   Password-Based Key Derivation Function 2
 * HMAC     Hashbased Message Authentication Code
 * SHA256   Secure Hash Algorithm
 */
class SecurityCodeTests {

	private static final String ASSERT_MSG = "ASSERT_MSG";

	@Test void test_UUID( ) {

		String txtLines = "";
		byte[] bytes = "1234".getBytes();
		txtLines += "base64 getEncoder: " + Base64.getEncoder().encodeToString(bytes) + EOL;
		txtLines += "base64 getUrlEncoder: " + Base64.getUrlEncoder().encodeToString(bytes) + EOL;
		txtLines += "base64 getMimeEncoder: " + Base64.getMimeEncoder().encodeToString(bytes) + EOL;
		txtLines += new String(new char[20]).replace('\u0000', '-') + EOL;

		UUID uuid = UUID.randomUUID();
		txtLines += "UUID randomUUID(): " + uuid + EOL;
		txtLines += "UUID replace toUpperCase: " + uuid.toString().replace("-", "").toUpperCase() + EOL;
		try {
			txtLines += "UUID fromString(): " + UUID.fromString("3bf121bc-14e9-45fa-9b38-b264759eb233") + EOL;
		}
		catch (IllegalArgumentException ex) { System.out.println("ERROR: " + ex.getMessage()); }

		System.out.println(txtLines);
		assertNotNull(txtLines);
	}

	@Test void test_NetworkAddress( ) { /**/

		String txtLines = EOL + "InetAddress" + EOL;
		try {
			InetAddress INA = InetAddress.getLocalHost();
			txtLines += String.format(
				"\tlocalhost: %s\n\t" + "hostName : %s\n\t" + "hostAddr : %s\n\t" + "canonical: %s\n\n",
				INA.toString(),
				INA.getHostName(),
				INA.getHostAddress(),
				INA.getCanonicalHostName());
		}
		catch (IOException ex) { System.out.println("ERROR: " + ex.getMessage()); }

		txtLines += "NetworkInterface SSIDs" + EOL;
		List<String> list = new ArrayList<>();
		Enumeration<NetworkInterface> enums = null;
		try { enums = NetworkInterface.getNetworkInterfaces(); }
		catch (SocketException ex) { System.out.println("ERROR: " + ex.getMessage()); }
		while ( enums.hasMoreElements() ) {
			NetworkInterface netIface = enums.nextElement();
			list.add(getNetIface(netIface, 1));
		}

		StringBuilder stringBuilder = new StringBuilder();
		for ( String txt : list ) stringBuilder.append(txt);
		txtLines += stringBuilder.toString();
		System.out.println(txtLines);
		assertTrue(list.size() >= 7, ASSERT_MSG);
	}

	// utilites
	private static String getNetIface(NetworkInterface netIface, int mode) {

		String txtLines = "", address;
		try {
			byte[] bytes = netIface.getHardwareAddress();
			if ( bytes == null ) {
				address = "[empty!]";
			} else {
				address = Base64.getEncoder().encodeToString(bytes);
			}
			// else{ address = bytes.length + " / " + new String(bytes, UTF_8);}
			if ( mode == 0 ) {
				txtLines += String.format("\t%02d %s\n", netIface.getIndex(), netIface.getDisplayName());
			} else {
				if ( bytes != null ) {
					txtLines += String.format("\t%6s %2d %04d %s %s\n",
						netIface.getName(),
						netIface.getIndex(),
						netIface.getMTU(), // Maximum Transmission Unit (MTU) of this interface
						address,
						netIface.getDisplayName());
				}
			}
		}
		catch (IOException ex) { System.out.println("ERROR: " + ex.getMessage()); }
		return txtLines;
	}
}
