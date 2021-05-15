package ClientsideEncryption;

import javax.crypto.SecretKey;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.*;
import java.security.cert.CertificateException;

public class KeystoreManager {


    public KeystoreManager(){}

    public KeyStore createKeystore(String password) throws KeyStoreException, CertificateException, IOException, NoSuchAlgorithmException {

        KeyStore ks = KeyStore.getInstance("pkcs12");
        char[] pwdArray = password.toCharArray();
        ks.load(null,pwdArray);
        return ks;
    }

    public void saveKeystore(KeyStore ks,String keystorePath, String keystorePassword) throws IOException, CertificateException, KeyStoreException, NoSuchAlgorithmException {
        char[] pwdArray = keystorePassword.toCharArray();
        //TODO("check extension using Guava https://www.baeldung.com/java-file-extension")
        FileOutputStream fos = new FileOutputStream(keystorePath);
        ks.store(fos, pwdArray);
    }

    public KeyStore loadKeystore(String keystorePath,String keystorePassword) throws KeyStoreException, IOException, CertificateException, NoSuchAlgorithmException {
        KeyStore ks = KeyStore.getInstance("pkcs12");
        char[] pwdArray = keystorePassword.toCharArray();
        ks.load(new FileInputStream(keystorePath),pwdArray);
        return ks;
    }

    public Key getKey(KeyStore ks, String keyName, String keystorePassword) throws UnrecoverableKeyException, KeyStoreException, NoSuchAlgorithmException {
        return ks.getKey(keyName,keystorePassword.toCharArray());

    }

    public KeyStore insertKey(KeyStore ks,SecretKey sk,String keyName,String keystorePassword) throws KeyStoreException {

        KeyStore.SecretKeyEntry secret = new KeyStore.SecretKeyEntry(sk);
        char[] pwdArray = keystorePassword.toCharArray();
        KeyStore.PasswordProtection password = new KeyStore.PasswordProtection(pwdArray);
        ks.setEntry(keyName,secret,password);
        return ks;

    }

    public void deleteKey(KeyStore ks, String keyName) throws KeyStoreException {
        ks.deleteEntry(keyName);
    }

    public void deleteKeystore(String keystorePath) throws IOException {
        Files.delete(Paths.get(keystorePath));
    }
}
