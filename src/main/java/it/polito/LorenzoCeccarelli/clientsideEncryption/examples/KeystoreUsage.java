package it.polito.LorenzoCeccarelli.clientsideEncryption.examples;

import it.polito.LorenzoCeccarelli.clientsideEncryption.crypto.CryptoUtils;
import it.polito.LorenzoCeccarelli.clientsideEncryption.exceptions.KeystoreOperationError;
import it.polito.LorenzoCeccarelli.clientsideEncryption.keystore.KeyStoreInfo;
import it.polito.LorenzoCeccarelli.clientsideEncryption.keystore.KeystoreUtils;

import javax.crypto.SecretKey;
import java.security.NoSuchAlgorithmException;

public class KeystoreUsage {
    public static void main(String[] args) {
        try {
            final String password = "veryStrongPassword";
            final String path = "exampleKeystore.p12";
            // Keystore creation
            KeyStoreInfo ksi = KeystoreUtils.createKeystore(password);
            // Save keystore in filesystem
            KeystoreUtils.saveKeystore(ksi, path);
            // Load keystore from the filesystem
            KeyStoreInfo ksiLoaded = KeystoreUtils.loadKeystore(password, path);
            // Create a key
            SecretKey sk = CryptoUtils.createSymKey(CryptoUtils.Algorithm.AES256);
            KeystoreUtils.insertKey(ksiLoaded, sk, "myKey");
            KeystoreUtils.saveKeystore(ksiLoaded,path);
            // Load a key
            SecretKey skLoaded = KeystoreUtils.getKey(ksiLoaded,"myKey");
            // Delete a key
            KeystoreUtils.deleteKey(ksiLoaded,"myKey");
            // Delete a keystore
            KeystoreUtils.deleteKeystore(path);

        } catch (KeystoreOperationError | NoSuchAlgorithmException keystoreOperationError) {
            keystoreOperationError.printStackTrace();
        }
    }
}
