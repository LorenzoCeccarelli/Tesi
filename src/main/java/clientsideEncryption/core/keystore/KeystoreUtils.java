package clientsideEncryption.core.keystore;

import clientsideEncryption.core.exceptions.KeyDoesNotExistException;
import clientsideEncryption.core.exceptions.KeystoreOperationError;

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
     * @throws KeystoreOperationError throws it if an error during the keystore operation occurs
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
     * @throws KeystoreOperationError throws it if an error during the keystore operation occurs
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
     * @throws KeystoreOperationError throws it if an error during the keystore operation occurs
     */
    public static KeyStoreInfo loadKeystore(String password, String path) throws KeystoreOperationError, FileNotFoundException {
        try {
            KeyStore ks = KeyStore.getInstance("pkcs12");
            char[] pwdArray = password.toCharArray();
            ks.load(new FileInputStream(path), pwdArray);
            return new KeyStoreInfo(ks, password);
        } catch(FileNotFoundException ex){
            throw ex;
        } catch (CertificateException | KeyStoreException | IOException | NoSuchAlgorithmException e) {
            throw new KeystoreOperationError(e.getMessage());
        }
    }

    /**
     * Return the symmetric key having the name equals to keyName
     * @param ksi KeystoreInfo Object
     * @param keyName the key alias
     * @return the symmetric key
     * @throws KeystoreOperationError throws it if an error during the keystore operation occurs
     */
    public static SecretKey getKey(KeyStoreInfo ksi, String keyName) throws KeystoreOperationError, KeyDoesNotExistException {
        try {
            Key k = ksi.getKeystore().getKey(keyName, ksi.getPassword().toCharArray());
            if(k == null) throw new KeyDoesNotExistException("Key called '"+keyName+"' does not exist in keystore ");
            return new SecretKeySpec(k.getEncoded(), 0, k.getEncoded().length, "AES");
        }  catch (UnrecoverableKeyException | KeyStoreException | NoSuchAlgorithmException  e) {
            throw new KeystoreOperationError(e.getMessage());
        }
    }

    /**
     * Check if a key exist in a keystore
     * @param ksi the KeyStoreInfo object
     * @param keyName the key name
     * @return true if the key exists, false otherwise
     * @throws UnrecoverableKeyException throws it if an error during the opening of keystore occurs
     * @throws KeyStoreException a generic KeyStore exception
     * @throws NoSuchAlgorithmException throws it if the algorithm is invalid
     */
    public static boolean existKey(KeyStoreInfo ksi, String keyName) throws UnrecoverableKeyException, KeyStoreException, NoSuchAlgorithmException {
        Key k = ksi.getKeystore().getKey(keyName, ksi.getPassword().toCharArray());
        return k != null;
    }

    /**
     * This method checks if a specified keystore exist
     * @param password the password that protects the keystore
     * @param path the filesystem path of the keystore
     * @return true if the keystore exists, false otherwise
     * @throws CertificateException throws it if an error occurs during the opening of th keystore
     * @throws KeyStoreException generic keystore error
     * @throws IOException throws it if an error occurs in the file IO
     * @throws NoSuchAlgorithmException throws it if the algorithm is invalid
     */
    public static boolean existKeystore(String password, String path) throws CertificateException, KeyStoreException, IOException, NoSuchAlgorithmException {
        try {
            KeyStore ks = null;
            ks = KeyStore.getInstance("pkcs12");
            char[] pwdArray = password.toCharArray();
            ks.load(new FileInputStream(path), pwdArray);
            return true;
        } catch (FileNotFoundException e) {
            return false;
        } catch (CertificateException | KeyStoreException | IOException | NoSuchAlgorithmException e) {
            throw e;
        }
    }
    /**
     * Permits to insert a key into the keystore ksi (but not in the filesystem)
     * @param ksi KeyStoreInfo object
     * @param sk the secretKey to add to the keystore
     * @param keyName the key alias
     * @return the updating keystore
     * @throws KeystoreOperationError throws it if an error during the keystore operation occurs
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
     * @throws KeystoreOperationError throws it if an error during the keystore operation occurs
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
     * @throws KeystoreOperationError throws it if an error during the keystore operation occurs
     */
    public static void deleteKeystore(String keystorePath) throws KeystoreOperationError {
        try {
            Files.delete(Paths.get(keystorePath));
        } catch (IOException e) {
            throw new KeystoreOperationError(e.getMessage());
        }
    }
}
