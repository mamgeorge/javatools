package utils;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.logging.Logger;

import static java.nio.charset.StandardCharsets.UTF_8;

public class EncryptDecrypt {

	// https://mkyong.com/java/java-aes-encryption-and-decryption/
	public static final Logger LOGGER = Logger.getLogger(EncryptDecrypt.class.getName());
	public static final String DIGITALENCRYPTKEY_AES = "AES";
	public static final String ALGORITHM_ENCRYPTION = "AES/GCM/NoPadding";
	public static final String ALGORITHM_SKF = "PBKDF2WithHmacSHA256";

	public static final int BYTES_IV_LEN = 12;
	public static final int BYTES_SALT_LEN = 16;
	public static final int AES_KEY_BIT = 256;
	public static final int TAG_LENGTH_BIT = 128;
	public static final int KEYGENERATOR_SIZE = 256;
	public static final int BLOCK_SIZE = 16;
	public static final String SALT_SAMPLE = "ABCD";

	public static byte[] getRandomIV(int numBytes) {
		//
		// Initial Vector
		byte[] bytesIV = new byte[numBytes];
		new SecureRandom().nextBytes(bytesIV);
		return bytesIV;
	}

	public static SecretKey getAESKey(int keysize) {
		//
		SecretKey secretKey = null;
		try {
			KeyGenerator keyGen = KeyGenerator.getInstance(DIGITALENCRYPTKEY_AES);
			keyGen.init(keysize, SecureRandom.getInstanceStrong());
			secretKey = keyGen.generateKey();
		} catch (NoSuchAlgorithmException ex) {
			LOGGER.severe(ex.getMessage());
		}
		return secretKey;
	}

	public static SecretKey getAESKeyFromPassword(String password, byte[] bytesSalt) {
		//
		SecretKey secretKey = null;
		try {
			int iterationCount = 65536;
			int keyLength = 256;
			SecretKeyFactory SKF = SecretKeyFactory.getInstance(ALGORITHM_SKF);
			KeySpec keySpec = new PBEKeySpec(password.toCharArray(), bytesSalt, iterationCount, keyLength);
			secretKey = new SecretKeySpec(SKF.generateSecret(keySpec).getEncoded(), DIGITALENCRYPTKEY_AES);
		} catch (NoSuchAlgorithmException | InvalidKeySpecException ex) {
			LOGGER.severe(ex.getMessage());
		}
		return secretKey;
	}

	public static String getHex(byte[] bytes) {
		//
		StringBuilder stringBuilder = new StringBuilder();
		for (byte bytee : bytes) {
			stringBuilder.append(String.format("%02x", bytee));
		}
		return stringBuilder.toString();
	}

	public static String getHexWithBlockSize(byte[] bytes, int blockSize) {

		String txtHex = getHex(bytes);
		blockSize = blockSize * 2;
		List<String> list = new ArrayList<>();
		int idx = 0;
		while (idx < txtHex.length()) {
			list.add(txtHex.substring(idx, Math.min(idx + blockSize, txtHex.length())));
			idx += blockSize;
		}
		return list.toString();
	}

	public static byte[] encrypt(byte[] bytesContent, SecretKey secretKey, byte[] bytesIV) {
		//
		byte[] bytesEncrypted = null;
		try {
			Cipher cipher = Cipher.getInstance(ALGORITHM_ENCRYPTION);
			cipher.init(Cipher.ENCRYPT_MODE, secretKey, new GCMParameterSpec(TAG_LENGTH_BIT, bytesIV));
			bytesEncrypted = cipher.doFinal(bytesContent);
		} catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException |
				InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException ex) {
			LOGGER.severe(ex.getMessage());
		}
		return bytesEncrypted;
	}

	public static String encryptPWD(String content, String password) {
		//
		String contentEncryptedBase64 = "";
		byte[] bytesSalt = getRandomIV(BYTES_SALT_LEN);
		byte[] bytesIV = getRandomIV(BYTES_IV_LEN);
		SecretKey secretKey = getAESKeyFromPassword(password, bytesSalt);
		try {
			//
			Cipher cipher = Cipher.getInstance(ALGORITHM_ENCRYPTION);
			cipher.init(Cipher.ENCRYPT_MODE, secretKey, new GCMParameterSpec(TAG_LENGTH_BIT, bytesIV));
			byte[] bytesEncrypted = cipher.doFinal(content.getBytes(UTF_8));
			byte[] bytesEncryptedWithIvSalt = ByteBuffer.allocate(bytesIV.length + bytesSalt.length + bytesEncrypted.length)
					.put(bytesIV)
					.put(bytesSalt)
					.put(bytesEncrypted)
					.array();
			contentEncryptedBase64 = Base64.getEncoder().encodeToString(bytesEncryptedWithIvSalt);
		} catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException |
				InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException ex) {
			LOGGER.severe(ex.getMessage());
		}
		return contentEncryptedBase64;
	}

	public static void encryptFile(String fileOrig, String fileEncr, String password) {
		//
		try {
			File fileLocal = new File(fileOrig);
			File pathFileLocal = new File(fileLocal.getAbsolutePath());
			byte[] bytesContent = Files.readAllBytes(pathFileLocal.toPath());
			String content = new String(bytesContent, UTF_8);
			//
			String contentEncryptedBase64 = encryptPWD(content, password);
			Path pathEncr = Paths.get(fileEncr);
			Files.write(pathEncr, contentEncryptedBase64.getBytes(UTF_8));
		} catch (IOException ex) {
			LOGGER.severe(ex.getMessage());
		}
	}

	public static String decrypt(byte[] bytesContent, SecretKey secretKey, byte[] bytesIV) {
		//
		String txtLines = "";
		try {
			Cipher cipher = Cipher.getInstance(ALGORITHM_ENCRYPTION);
			cipher.init(Cipher.DECRYPT_MODE, secretKey, new GCMParameterSpec(TAG_LENGTH_BIT, bytesIV));
			byte[] bytesDecrypted = cipher.doFinal(bytesContent);
			txtLines = new String(bytesDecrypted, UTF_8);
		} catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException |
				InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException ex) {
			LOGGER.severe(ex.getMessage());
		}
		return txtLines;
	}

	public static String decryptPWD(String contentEncryptedBase64, String password) {
		//
		String content = "";
		byte[] bytesDecode = Base64.getDecoder().decode(contentEncryptedBase64.getBytes(UTF_8));
		byte[] bytesIV = new byte[BYTES_IV_LEN];
		byte[] bytesSalt = new byte[BYTES_SALT_LEN];
		byte[] bytesCipher = null;
		ByteBuffer byteBuffer = ByteBuffer.wrap(bytesDecode);
		byteBuffer.get(bytesIV);
		byteBuffer.get(bytesSalt);
		bytesCipher = new byte[byteBuffer.remaining()];
		byteBuffer.get(bytesCipher);
		//
		try {
			SecretKey secretKey = getAESKeyFromPassword(password, bytesSalt);
			Cipher cipher = Cipher.getInstance(ALGORITHM_ENCRYPTION);
			cipher.init(Cipher.DECRYPT_MODE, secretKey, new GCMParameterSpec(TAG_LENGTH_BIT, bytesIV));
			byte[] bytesDecrypted = cipher.doFinal(bytesCipher);
			content = new String(bytesDecrypted, UTF_8);
		} catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException |
				InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException ex) {
			LOGGER.severe(ex.getMessage());
		}
		return content;
	}

	public static String decryptFile(String fileEncr, String password) {
		//
		String content = "";
		try {
			byte[] bytesContent = Files.readAllBytes(Paths.get(fileEncr));
			String contentEncryptedBase64 = new String(bytesContent, UTF_8);
			content = decryptPWD(contentEncryptedBase64, password);
		} catch (IOException ex) {
			LOGGER.severe(ex.getMessage());
		}
		return content;
	}
}