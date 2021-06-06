package junit;

import clientsideEncryption.core.crypto.CryptoUtils;
import clientsideEncryption.core.exceptions.DecryptionError;
import clientsideEncryption.core.exceptions.EncryptionError;
import clientsideEncryption.core.exceptions.KeystoreOperationError;
import clientsideEncryption.core.keystore.KeyStoreInfo;
import clientsideEncryption.core.keystore.KeystoreUtils;
import clientsideEncryption.core.token.ClearToken;
import clientsideEncryption.core.token.EncryptedToken;
import clientsideEncryption.core.token.Token;
import clientsideEncryption.core.token.TokenParser;
import org.junit.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.NoSuchAlgorithmException;

public class ClientsideEncryptionTest {

    @Test
    public void enableEncryption() throws KeystoreOperationError, NoSuchAlgorithmException, EncryptionError, DecryptionError {

        //Generate a keystore
        String password = "securePassword";
        KeyStoreInfo ksi = KeystoreUtils.createKeystore(password);

        //Generate the master key
        SecretKey sk = CryptoUtils.createSymKey(CryptoUtils.Algorithm.AES256);

        //Insert the key into the keystore
        KeystoreUtils.insertKey(ksi,sk,"MEK");

        //Create on the fly encryption key
        SecretKey ek = CryptoUtils.createSymKey(CryptoUtils.Algorithm.AES192);

        //Encrypt the data with the encryption key
        String data = "VerySecret";
        byte[] ciphertext = CryptoUtils.encryptDataWithPrefixIV(ek,data.getBytes());

        //Encrypt the EK with the MEK
        byte[] cipherkey = CryptoUtils.encryptDataWithPrefixIV(sk,ek.getEncoded());

        //Create the token
        EncryptedToken et = new EncryptedToken(cipherkey,ciphertext);

        //Parse the token
        Token t = TokenParser.parseToken(et.generateToken());

        //Decrypt the encryption key
        assert t != null;
        byte[] key = CryptoUtils.decryptDataWithPrefixIV(sk,((EncryptedToken) t).getEncryptedKey());
        SecretKey originalKey = new SecretKeySpec(key, 0, key.length, "AES");

        //Decrypt the original data
        String originalData = new String(CryptoUtils.decryptDataWithPrefixIV(originalKey,((EncryptedToken) t).getCiphertext()));

        assertEquals(originalData,data, "The data should be equal to original data");
    }

    @Test
    public void clearData(){

        String data = "clearData";

        //Generate the token
        ClearToken ct = new ClearToken(data);

        //Parse the token
        Token t = TokenParser.parseToken(ct.generateToken());

        assert t != null;
        String originalData = ((ClearToken) t).getData();

        assertEquals(originalData,data,"The data should be equal to original data");


    }

}
