package it.polito.LorenzoCeccarelli.clientsideEncryption.keystore;

import it.polito.LorenzoCeccarelli.clientsideEncryption.exceptions.KeystoreOperationError;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.security.cert.CertificateException;

/**
 * KeyStoreUtils is a class which contains only static method for managing Keystore
 */
public class KeystoreUtils {

    private KeystoreUtils(){}

    /**
     * Create a KeyStore object (of type PKCS12) protected by a password
     * @param password the password that protects the keystore
     * @return KeystoreInfo
     * @throws KeystoreOperationError
     */
    public static KeyStoreInfo createKeystore(String password) throws KeystoreOperationError {
        try {
            KeyStore ks = KeyStore.getInstance("pkcs12");
            char[] pwdArray = password.toCharArray();
            ks.load(null, pwdArray);
            return new KeyStoreInfo(ks, password);
        } catch (CertificateException | KeyStoreException | IOException | NoSuchAlgorithmException e) {
            throw new KeystoreOperationError(e.getMessage());
        }
    }

    /**
     * Save the KeyStore on the filesystem
     * @param ksi KeyStoreInfo object
     * @param path the filesystem path
     * @throws KeystoreOperationError
     */
    public static void saveKeystore(KeyStoreInfo ksi, String path) throws KeystoreOperationError {
        try {
            char[] pwdArray = ksi.getPassword().toCharArray();
            //TODO("check extension using Guava https://www.baeldung.com/java-file-extension")
            FileOutputStream fos = new FileOutputStream(path);
            ksi.getKeystore().store(fos, pwdArray);
            fos.close();
        } catch (CertificateException | KeyStoreException | IOException | NoSuchAlgorithmException e) {
            throw new KeystoreOperationError(e.getMessage());
        }
    }

    /**
     * Load the Keystore from the filesystem
     * @param password the password that protects the keystore
     * @param path the filesystem path
     * @return KeystoreInfoObject
     * @throws KeystoreOperationError
     */
    public static KeyStoreInfo loadKeystore(String password, String path) throws KeystoreOperationError {
        try {
            KeyStore ks = KeyStore.getInstance("pkcs12");
            char[] pwdArray = password.toCharArray();
            ks.load(new FileInputStream(path), pwdArray);
            return new KeyStoreInfo(ks, password);
        } catch (CertificateException | KeyStoreException | IOException | NoSuchAlgorithmException e) {
            throw new KeystoreOperationError(e.getMessage());
        }
    }

    /**
     * Return the symmetric key having the name equals to keyName
     * @param ksi KeystoreInfo Object
     * @param keyName the key alias
     * @return the symmetric key
     * @throws KeystoreOperationError
     */
    public static SecretKey getKey(KeyStoreInfo ksi, String keyName) throws KeystoreOperationError {
        try {
            Key k = ksi.getKeystore().getKey(keyName, ksi.getPassword().toCharArray());
            return new SecretKeySpec(k.getEncoded(), 0, k.getEncoded().length, "AES");
        } catch (UnrecoverableKeyException | KeyStoreException | NoSuchAlgorithmException e) {
            throw new KeystoreOperationError(e.getMessage());
        }
    }

    /**
     * Permits to insert a key into the keystore ksi (but not in the filesystem)
     * @param ksi KeyStoreInfo object
     * @param sk the secretKey to add to the keystore
     * @param keyName the key alias
     * @return the updating keystore
     * @throws KeystoreOperationError
     */
    public static KeyStoreInfo insertKey(KeyStoreInfo ksi,SecretKey sk,String keyName) throws KeystoreOperationError {
        try {
            KeyStore.SecretKeyEntry secret = new KeyStore.SecretKeyEntry(sk);
            char[] pwdArray = ksi.getPassword().toCharArray();
            KeyStore.PasswordProtection password = new KeyStore.PasswordProtection(pwdArray);
            ksi.getKeystore().setEntry(keyName, secret, password);
            return ksi;
        } catch (KeyStoreException e) {
            throw new KeystoreOperationError(e.getMessage());
        }

    }

    /**
     * Permits to delete a specified key
     * @param ks KeystoreInfo object
     * @param keyName the key alias
     * @throws KeystoreOperationError
     */
    public static void deleteKey(KeyStoreInfo ks, String keyName) throws KeystoreOperationError {
        try {
            ks.getKeystore().deleteEntry(keyName);
        } catch (KeyStoreException e) {
            throw new KeystoreOperationError(e.getMessage());
        }
    }

    /**
     * Permits to delete a specified keystore
     * @param keystorePath the path of the keystore to delete
     * @throws KeystoreOperationError
     */
    public static void deleteKeystore(String keystorePath) throws KeystoreOperationError {
        try {
            Files.delete(Paths.get(keystorePath));
        } catch (IOException e) {
            throw new KeystoreOperationError(e.getMessage());
        }
    }
}
