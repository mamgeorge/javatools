package network;

import org.junit.jupiter.api.Test;
import utils.UtilityMain;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static network.EncryptDecrypt.AES_KEY_BIT;
import static network.EncryptDecrypt.BLOCK_SIZE;
import static network.EncryptDecrypt.BYTES_IV_LEN;
import static network.EncryptDecrypt.SALT_SAMPLE;
import static network.EncryptDecrypt.decrypt;
import static network.EncryptDecrypt.decryptFile;
import static network.EncryptDecrypt.decryptPWD;
import static network.EncryptDecrypt.encrypt;
import static network.EncryptDecrypt.encryptFile;
import static network.EncryptDecrypt.encryptPWD;
import static network.EncryptDecrypt.getAESKey;
import static network.EncryptDecrypt.getAESKeyFromPassword;
import static network.EncryptDecrypt.getHex;
import static network.EncryptDecrypt.getHexWithBlockSize;
import static network.EncryptDecrypt.getRandomIV;
import static utils.UtilityMainTest.ASSERT_MSG;
import static utils.UtilityMainTest.PATHFILE_LOCAL;

/**
 * AES      Advanced Encryption Standard
 * GCM      Galois Counter Mode
 * PBKDF2   Password-Based Key Derivation Function 2
 * HMAC     Hashbased Message Authentication Code
 * SHA256   Secure Hash Algorithm
 */
class EncryptDecryptTests {

	private static final String FILE_ORIG = PATHFILE_LOCAL + "txtOriginals.txt";
	private static final String FILE_ENCR = PATHFILE_LOCAL + "txtEncrypted.txt";
	private static final String TXT_CONTENT = "North America, Unites States, Ohio, Martin George";
	private static final String PASSWORD = "ABCD1234";

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
}
