package ClientsideEncryption;

import org.apache.commons.lang3.tuple.ImmutablePair;

import javax.crypto.*;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

enum Algorithm{
    AES128,
    AES192,
    AES256
}
public class EncryptionManager {

    public EncryptionManager(){}

    public SecretKey createSymKey(Algorithm alg) throws NoSuchAlgorithmException {
        ImmutablePair<String,Integer> ip = algorithmInfo(alg);
        SecureRandom sr = new SecureRandom();
        KeyGenerator kg = KeyGenerator.getInstance(ip.left);
        kg.init(ip.right,sr);
        return kg.generateKey();
    }

    private ImmutablePair<String, Integer> algorithmInfo(Algorithm al) throws NoSuchAlgorithmException {
        switch (al){
            case AES128: return ImmutablePair.of("AES",128);
            case AES192: return ImmutablePair.of("AES",192);
            case AES256: return ImmutablePair.of("AES",256);
            default: throw new NoSuchAlgorithmException("Algorithm not supported");
        }
    }


    public String encryptData(SecretKey sk, String toEncrypt, Algorithm alg) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        ImmutablePair<String, Integer> ip = algorithmInfo(alg);
        Cipher c = Cipher.getInstance("AES/CBC/PKCS5PADDING");
        c.init(Cipher.ENCRYPT_MODE, sk);
        byte[] ciphertext = c.doFinal(toEncrypt.getBytes());
        String b = Base64.getEncoder().withoutPadding().encodeToString(ciphertext);
        System.out.println(toEncrypt + "-> "+b);
        return b;
    }

    public String decryptData(SecretKey sk, String toDecrypt, Algorithm alg) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        Cipher c = Cipher.getInstance("AES/CBC/PKCS5PADDING");
        c.init(Cipher.DECRYPT_MODE, sk);
        byte[] plaintext = c.doFinal(toDecrypt.getBytes());
        return new String(plaintext);
    }
}
