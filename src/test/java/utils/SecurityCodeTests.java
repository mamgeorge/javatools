package utils;

import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Enumeration;
import java.util.List;
import java.util.UUID;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static utils.EncryptDecrypt.AES_KEY_BIT;
import static utils.EncryptDecrypt.BLOCK_SIZE;
import static utils.EncryptDecrypt.BYTES_IV_LEN;
import static utils.EncryptDecrypt.SALT_SAMPLE;
import static utils.EncryptDecrypt.decrypt;
import static utils.EncryptDecrypt.decryptFile;
import static utils.EncryptDecrypt.decryptPWD;
import static utils.EncryptDecrypt.encrypt;
import static utils.EncryptDecrypt.encryptFile;
import static utils.EncryptDecrypt.encryptPWD;
import static utils.EncryptDecrypt.getAESKey;
import static utils.EncryptDecrypt.getAESKeyFromPassword;
import static utils.EncryptDecrypt.getHex;
import static utils.EncryptDecrypt.getHexWithBlockSize;
import static utils.EncryptDecrypt.getRandomIV;
import static utils.UtilityMain.EOL;

/**
 * AES      Advanced Encryption Standard
 * GCM      Galois Counter Mode
 * PBKDF2   Password-Based Key Derivation Function 2
 * HMAC     Hashbased Message Authentication Code
 * SHA256   Secure Hash Algorithm
 */
class SecurityCodeTests {

	private static final String PATHFILE_LOCAL = "src/test/resources/";
	private static final String FILE_ORIG = PATHFILE_LOCAL + "txtOriginals.txt";
	private static final String FILE_ENCR = PATHFILE_LOCAL + "txtEncrypted.txt";

	private static final String ASSERT_MSG = "ASSERT_MSG";
	private static final String TXT_CONTENT = "North America, Unites States, Ohio, Martin George";
	private static final String PASSWORD = "ABCD1234";


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

	// EncryptDecrypt
	@Test void test_EncryptDecrypt_getRandomIV( ) {

		byte[] bytesIV = getRandomIV(BYTES_IV_LEN);
		System.out.println(bytesIV.toString() + " / " + bytesIV.length);
		assertTrue(bytesIV.length > 10, ASSERT_MSG);
	}

	@Test void test_EncryptDecrypt_getAESKey( ) {

		SecretKey secretKey = getAESKey(AES_KEY_BIT);
		String txtLines = showSecretKey(secretKey);

		System.out.println("getAESKey " + txtLines);
		assertTrue(secretKey.toString().length() > 10, ASSERT_MSG);
	}

	@Test void test_EncryptDecrypt_getAESKeyFromPassword( ) {

		SecretKey secretKey = getAESKeyFromPassword(PASSWORD, SALT_SAMPLE.getBytes(UTF_8));
		String txtLines = showSecretKey(secretKey);

		System.out.println("getAESKeyFromPassword " + txtLines);
		assertTrue(secretKey.toString().length() > 10, ASSERT_MSG);
	}

	@Test void test_EncryptDecrypt_encrypt( ) {

		String txtLines = "";
		byte[] bytesEncrypted = null;

		byte[] bytesContent = TXT_CONTENT.getBytes();
		byte[] bytesIV = getRandomIV(BYTES_IV_LEN);
		SecretKey secretKey = getAESKey(AES_KEY_BIT);

		bytesEncrypted = encrypt(bytesContent, secretKey, bytesIV);
		txtLines += "\tbytesEncrypted txt: " + new String(bytesEncrypted, UTF_8) + "\n";
		txtLines += "\tbytesEncrypted Hex: " + getHex(bytesEncrypted) + "\n";
		txtLines += "\tbytesEncrypted Blk: " + getHexWithBlockSize(bytesEncrypted, BLOCK_SIZE);
		System.out.println(txtLines);
		assertTrue(bytesEncrypted.length > 10, ASSERT_MSG);
	}

	@Test void test_EncryptDecrypt_encryptPWD( ) {

		String txtLines = "";

		String contentEncryptedBase64 = encryptPWD(TXT_CONTENT, PASSWORD);
		try {
			Path pathEncr = Paths.get(FILE_ENCR);
			Files.write(pathEncr, contentEncryptedBase64.getBytes(UTF_8));
		}
		catch (IOException ex) { System.out.println("ERROR: " + ex.getMessage()); }

		byte[] bytesEncryptedBase64 = contentEncryptedBase64.getBytes(UTF_8);
		txtLines += "\tTXT_CONTENT.........: " + TXT_CONTENT + "\n";
		txtLines += "\tcontentEncrypted txt: " + contentEncryptedBase64 + "\n";
		txtLines += "\tcontentEncrypted Blk: " + getHexWithBlockSize(bytesEncryptedBase64, BLOCK_SIZE);
		System.out.println(txtLines);
		assertTrue(contentEncryptedBase64.length() > 10, ASSERT_MSG);
	}

	@Test void test_EncryptDecrypt_encryptFile( ) {

		encryptFile(FILE_ORIG, FILE_ENCR, PASSWORD);

		String contentEncryptedBase64 = UtilityMain.getFileLocal(FILE_ENCR);
		String txtLines = "contentEncryptedBase64:\n" + contentEncryptedBase64;
		System.out.println(txtLines);
		assertTrue(contentEncryptedBase64.length() > 10, ASSERT_MSG);
	}

	@Test void test_EncryptDecrypt_decrypt( ) {

		String txtLines = "";

		byte[] bytesContent = TXT_CONTENT.getBytes();
		byte[] bytesIV = getRandomIV(BYTES_IV_LEN);
		SecretKey secretKey = getAESKey(AES_KEY_BIT);
		byte[] bytesEncrypted = encrypt(bytesContent, secretKey, bytesIV);

		String contentDecrypted = decrypt(bytesEncrypted, secretKey, bytesIV);

		txtLines += "original!: " + TXT_CONTENT + "\n";
		txtLines += "decrypted: " + contentDecrypted;
		System.out.println(txtLines);
		assertEquals(TXT_CONTENT, contentDecrypted, ASSERT_MSG);
	}

	@Test void test_EncryptDecrypt_decryptPWD( ) {

		String txtLines = "";

		String contentEncrypted = encryptPWD(TXT_CONTENT, PASSWORD);
		String contentDecrypted = decryptPWD(contentEncrypted, PASSWORD);
		try {
			Path pathOrig = Paths.get(FILE_ORIG);
			Files.write(pathOrig, contentDecrypted.getBytes(UTF_8));
		}
		catch (IOException ex) { System.out.println("ERROR: " + ex.getMessage()); }

		txtLines += "original!: " + TXT_CONTENT + "\n";
		txtLines += "encrypted: " + contentEncrypted + "\n";
		txtLines += "decrypted: " + contentDecrypted;
		System.out.println(txtLines);
		assertEquals(TXT_CONTENT, contentDecrypted, ASSERT_MSG);
	}

	@Test void test_EncryptDecrypt_decryptFile( ) {

		String contentDecrypted = decryptFile(FILE_ENCR, PASSWORD);

		String txtLines = "contentDecrypted:\n" + contentDecrypted;
		System.out.println(txtLines);
		assertTrue(contentDecrypted.length() > 10, ASSERT_MSG);
	}

	// utilites
	private String showSecretKey(SecretKey secretKey) {

		String FRMT = "\t%-15s %s\n";
		String txtLines = "SecretKey" + "\n";
		txtLines += String.format(FRMT, "secretKey", secretKey.toString());
		txtLines += String.format(FRMT, "getAlgorithm", secretKey.getAlgorithm());
		txtLines += String.format(FRMT, "getFormat", secretKey.getFormat());
		txtLines += String.format(FRMT, "getEncoded", secretKey.getEncoded());
		txtLines += String.format(FRMT, "hexEncoded", getHex(secretKey.getEncoded()));
		txtLines +=
			String.format(FRMT, "hexBlocked", getHexWithBlockSize(secretKey.getEncoded(), BLOCK_SIZE));
		return txtLines;
	}

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
