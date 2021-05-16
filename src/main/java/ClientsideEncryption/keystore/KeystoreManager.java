package ClientsideEncryption.keystore;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.security.cert.CertificateException;

public class KeystoreManager {


    public KeystoreManager(){}

    public KeyStoreInfo createKeystore(String password) throws KeyStoreException, CertificateException, IOException, NoSuchAlgorithmException {

        KeyStore ks = KeyStore.getInstance("pkcs12");
        char[] pwdArray = password.toCharArray();
        ks.load(null,pwdArray);

        return new KeyStoreInfo(ks,password);
    }

    public void saveKeystore(KeyStoreInfo ksi, String path) throws IOException, CertificateException, KeyStoreException, NoSuchAlgorithmException {
        char[] pwdArray = ksi.getPassword().toCharArray();
        //TODO("check extension using Guava https://www.baeldung.com/java-file-extension")
        FileOutputStream fos = new FileOutputStream(path);
        ksi.getKeystore().store(fos, pwdArray);
    }

    public KeyStoreInfo loadKeystore(String password, String path) throws KeyStoreException, IOException, CertificateException, NoSuchAlgorithmException {
        KeyStore ks = KeyStore.getInstance("pkcs12");
        char[] pwdArray = password.toCharArray();
        ks.load(new FileInputStream(path),pwdArray);
        return new KeyStoreInfo(ks,password);
    }

    public SecretKey getKey(KeyStoreInfo ksi, String keyName) throws UnrecoverableKeyException, KeyStoreException, NoSuchAlgorithmException {
        Key k = ksi.getKeystore().getKey(keyName,ksi.getPassword().toCharArray());
        return new SecretKeySpec(k.getEncoded(),0,k.getEncoded().length,"AES");
    }

    public KeyStoreInfo insertKey(KeyStoreInfo ksi,SecretKey sk,String keyName) throws KeyStoreException {

        KeyStore.SecretKeyEntry secret = new KeyStore.SecretKeyEntry(sk);
        char[] pwdArray = ksi.getPassword().toCharArray();
        KeyStore.PasswordProtection password = new KeyStore.PasswordProtection(pwdArray);
        ksi.getKeystore().setEntry(keyName,secret,password);
        return ksi;

    }

    public void deleteKey(KeyStore ks, String keyName) throws KeyStoreException {
        ks.deleteEntry(keyName);
    }

    public void deleteKeystore(String keystorePath) throws IOException {
        Files.delete(Paths.get(keystorePath));
    }
}
