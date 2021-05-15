package ClientsideEncryption.keystore;

import javax.crypto.SecretKey;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.*;
import java.security.cert.CertificateException;

public class KeystoreManager {


    public KeystoreManager(){}

    public KeyStoreInfo createKeystore(String password) throws KeyStoreException, CertificateException, IOException, NoSuchAlgorithmException {

        KeyStore ks = KeyStore.getInstance("pkcs12");
        char[] pwdArray = password.toCharArray();
        ks.load(null,pwdArray);

        return new KeyStoreInfo(ks,null,password);
    }

    public void saveKeystore(KeyStoreInfo ksi) throws IOException, CertificateException, KeyStoreException, NoSuchAlgorithmException {
        char[] pwdArray = ksi.getPassword().toCharArray();
        //TODO("check extension using Guava https://www.baeldung.com/java-file-extension")
        FileOutputStream fos = new FileOutputStream(ksi.getPath());
        ksi.getKeystore().store(fos, pwdArray);
    }

    public KeyStoreInfo loadKeystore(KeyStoreInfo ksi) throws KeyStoreException, IOException, CertificateException, NoSuchAlgorithmException {
        KeyStore ks = KeyStore.getInstance("pkcs12");
        char[] pwdArray = ksi.getPassword().toCharArray();
        ks.load(new FileInputStream(ksi.getPath()),pwdArray);
        ksi.setKeystore(ks);
        return ksi;
    }

    public Key getKey(KeyStoreInfo ksi, String keyName) throws UnrecoverableKeyException, KeyStoreException, NoSuchAlgorithmException {
        return ksi.getKeystore().getKey(keyName,ksi.getPassword().toCharArray());

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
