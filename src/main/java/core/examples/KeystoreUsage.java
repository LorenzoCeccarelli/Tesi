package core.examples;

import core.crypto.CryptoUtils;
import core.exceptions.KeyDoesNotExistException;
import core.exceptions.KeystoreOperationError;
import core.keystore.KeyStoreInfo;
import core.keystore.KeystoreUtils;

import javax.crypto.SecretKey;
import java.io.FileNotFoundException;
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

        } catch (KeystoreOperationError | NoSuchAlgorithmException | FileNotFoundException | KeyDoesNotExistException keystoreOperationError) {
            keystoreOperationError.printStackTrace();
        }
    }
}
