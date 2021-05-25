package clientsideEncryption.core.crypto;

import clientsideEncryption.core.exceptions.DecryptionError;
import clientsideEncryption.core.exceptions.EncryptionError;
import org.apache.commons.lang3.tuple.ImmutablePair;
import javax.crypto.*;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;


public class CryptoUtils {

    private static final int IV_LENGTH_BYTE = 12;
    private static final int TAG_LENGTH_BIT = 128;
    private static final String ENCRYPT_ALGO = "AES/GCM/NoPadding";

    /**
     * Supported Algorithm
     */
    public enum Algorithm{
        AES128,
        AES192,
        AES256
    }

    private CryptoUtils(){}

    /**
     * Generate a nonce of length "length"
     * @param length int
     * @return the generated nonce
     */
    public static byte[] getRandomNonce(int length){
        byte[] nonce = new byte[length];
        new SecureRandom().nextBytes(nonce);
        return nonce;
    }

    /**
     * Create a symmetric key compatible with "alg"
     * @param alg the algorithm type
     * @return the generated symmetric key
     * @throws NoSuchAlgorithmException throws it if the algorithm is invalid
     */
    public static SecretKey createSymKey(Algorithm alg) throws NoSuchAlgorithmException {
        ImmutablePair<String,Integer> ip = algorithmInfo(alg);
        SecureRandom sr = SecureRandom.getInstanceStrong();
        KeyGenerator kg = KeyGenerator.getInstance(ip.left);
        kg.init(ip.right,sr);
        return kg.generateKey();
    }

    /**
     * Generate key from password using a key derivation function
     * @param alg the algorithm
     * @param password the initial password
     * @param salt the salt
     * @return the generated key
     * @throws NoSuchAlgorithmException throws it if the algorithm is invalid
     * @throws InvalidKeySpecException throws it if the key is invalid
     */
    public static SecretKey createKeyFromPassword(Algorithm alg, char[] password, byte[] salt)
            throws NoSuchAlgorithmException, InvalidKeySpecException {

        ImmutablePair<String, Integer> ip = algorithmInfo(alg);
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        // iterationCount = 65536
        KeySpec spec = new PBEKeySpec(password, salt, 65536, ip.right);
        return new SecretKeySpec(factory.generateSecret(spec).getEncoded(), ip.left);
    }

    /**
     * Retrieve the "al" information (name, key length)
     * @param al the algorithm
     * @return a Pair containing the algorithm information
     * @throws NoSuchAlgorithmException throws it if the algorithm is invalid
     */
    private static ImmutablePair<String, Integer> algorithmInfo(Algorithm al) throws NoSuchAlgorithmException {
        switch (al){
            case AES128: return ImmutablePair.of("AES",128);
            case AES192: return ImmutablePair.of("AES",192);
            case AES256: return ImmutablePair.of("AES",256);
            default: throw new NoSuchAlgorithmException("Algorithm not supported");
        }
    }

    /**
     * Encrypt data without appending the iv to ciphertext
     * @param sk SecretKey
     * @param toEncrypt plaintext
     * @param iv Initialization Vector
     * @return ciphertext
     * @throws EncryptionError throws it if an error during encryption occurs
     */
    public static byte[] encryptData(SecretKey sk, byte[] toEncrypt, byte[] iv) throws EncryptionError {
        try {
            Cipher c = Cipher.getInstance(ENCRYPT_ALGO);
            c.init(Cipher.ENCRYPT_MODE, sk, new GCMParameterSpec(TAG_LENGTH_BIT, iv));
            return c.doFinal(toEncrypt);
        } catch (InvalidAlgorithmParameterException | NoSuchPaddingException | IllegalBlockSizeException | NoSuchAlgorithmException | BadPaddingException | InvalidKeyException e) {
            throw new EncryptionError(e.getMessage());
        }
    }

    /**
     * Encrypt data appending the iv to ciphertext
     * @param sk SecretKey
     * @param toEncrypt plaintext
     * @return ciphertext + iv
     * @throws EncryptionError throws it if an error during the encryption occurs
     */
    public static byte[] encryptDataWithPrefixIV(SecretKey sk, byte[] toEncrypt) throws EncryptionError {
        byte[] iv = getRandomNonce(IV_LENGTH_BYTE);
        byte[] cipherText = encryptData(sk,toEncrypt,iv);
        return ByteBuffer.allocate(iv.length + cipherText.length)
                .put(iv)
                .put(cipherText)
                .array();
    }

    /**
     * Decrypt data
     * @param sk SecretKey
     * @param toDecrypt the ciphertext
     * @param iv Initialization Vector
     * @return the decrypted data
     * @throws DecryptionError throws it if an error during decryption occurs
     */
    public static byte[] decryptData(SecretKey sk, byte[] toDecrypt, byte[] iv) throws DecryptionError {
        try {
            Cipher c = Cipher.getInstance(ENCRYPT_ALGO);
            c.init(Cipher.DECRYPT_MODE, sk, new GCMParameterSpec(TAG_LENGTH_BIT, iv));
            return c.doFinal(toDecrypt);
        } catch (InvalidAlgorithmParameterException | NoSuchPaddingException | IllegalBlockSizeException | NoSuchAlgorithmException | BadPaddingException | InvalidKeyException e) {
            throw new DecryptionError(e.getMessage());
        }
    }

    /**
     * Extracts the iv from the encrypted data and then decrypt the data using the iv
     * @param sk SecretKey
     * @param toDecrypt ciphertext
     * @return plaintext
     * @throws DecryptionError throws it if an error during decryption occurs
     */
    public static byte[] decryptDataWithPrefixIV(SecretKey sk, byte[] toDecrypt) throws DecryptionError {

        ByteBuffer bb = ByteBuffer.wrap(toDecrypt);

        byte[] iv = new byte[IV_LENGTH_BYTE];
        bb.get(iv);

        byte[] cipherText = new byte[bb.remaining()];
        bb.get(cipherText);

        return decryptData(sk, cipherText,iv);
    }

}
