import clientsideEncryption.core.crypto.CryptoUtils;
import clientsideEncryption.core.exceptions.DecryptionError;
import clientsideEncryption.core.exceptions.EncryptionError;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.assertEquals;
import javax.crypto.SecretKey;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

public class CryptoUtilsTest {

    @Test
    @DisplayName("SecureRandom.nextBytes should work")
    public void getRandomNonceTest(){
        int length = 32;
        byte[] nonce = CryptoUtils.getRandomNonce(length);
        assertEquals(length,nonce.length,"The length of nonce must be equal to length");
    }

    @Test
    @DisplayName("createSymKey should generate a key of specified algorithm")
    public void createSymKeyTest() throws NoSuchAlgorithmException {
        SecretKey sk = CryptoUtils.createSymKey(CryptoUtils.Algorithm.AES128);
        assertEquals("AES",sk.getAlgorithm(),"The specified algorithm is AES");
        assertEquals(128,sk.getEncoded().length*8, "The specified key length is 128 bits");
        SecretKey sk2 = CryptoUtils.createSymKey(CryptoUtils.Algorithm.AES192);
        assertEquals("AES",sk2.getAlgorithm(),"The specified algorithm is AES");
        assertEquals(192,sk2.getEncoded().length*8, "The specified key length is 192 bits");
        SecretKey sk3 = CryptoUtils.createSymKey(CryptoUtils.Algorithm.AES256);
        assertEquals("AES",sk3.getAlgorithm(),"The specified algorithm is AES");
        assertEquals(256,sk3.getEncoded().length*8, "The specified key length is 256 bits");
    }

    @Test
    public void createKeyFromPasswordTest() throws NoSuchAlgorithmException, InvalidKeySpecException {
        SecretKey sk = CryptoUtils.createKeyFromPassword(CryptoUtils.Algorithm.AES128,"password".toCharArray(),"salt".getBytes());
        assertEquals("AES",sk.getAlgorithm(),"The specified algorithm is AES");
        assertEquals(128,sk.getEncoded().length*8, "The specified key length is 128 bits");
        SecretKey sk2 = CryptoUtils.createKeyFromPassword(CryptoUtils.Algorithm.AES192,"password".toCharArray(),"salt".getBytes());
        assertEquals("AES",sk2.getAlgorithm(),"The specified algorithm is AES");
        assertEquals(192,sk2.getEncoded().length*8, "The specified key length is 192 bits");
        SecretKey sk3 = CryptoUtils.createKeyFromPassword(CryptoUtils.Algorithm.AES256,"password".toCharArray(),"salt".getBytes());
        assertEquals("AES",sk3.getAlgorithm(),"The specified algorithm is AES");
        assertEquals(256,sk3.getEncoded().length*8, "The specified key length is 256 bits");
    }

    @Test
    public void encryptAndDecryptWithPrefixIVDataTest() throws NoSuchAlgorithmException, EncryptionError, DecryptionError {
        SecretKey sk = CryptoUtils.createSymKey(CryptoUtils.Algorithm.AES256);
        byte[] ciphertext = CryptoUtils.encryptDataWithPrefixIV(sk,"plaintext".getBytes());
        byte[] plaintext = CryptoUtils.decryptDataWithPrefixIV(sk,ciphertext);
        assertEquals("plaintext", new String(plaintext));

    }

    @Test
    public void encryptAndDecryptDataTest() throws DecryptionError, EncryptionError, NoSuchAlgorithmException {
        SecretKey sk = CryptoUtils.createSymKey(CryptoUtils.Algorithm.AES128);
        byte[] iv = CryptoUtils.getRandomNonce(12);
        byte[] ciphertext = CryptoUtils.encryptData(sk,"plaintext".getBytes(),iv);
        byte[] plaintext = CryptoUtils.decryptData(sk,ciphertext,iv);
        assertEquals("plaintext", new String(plaintext));
    }

}
