package clientsideEncryption.core.keystore;

import java.security.KeyStore;

/**
 * KeystoreInfo models a Keystore
 */
public class KeyStoreInfo {

    private KeyStore ks;
    private String password;

    public KeyStoreInfo(KeyStore ks/*, String path*/, String password){
        this.ks = ks;
        this.password = password;
    }


    public KeyStore getKeystore(){
        return ks;
    }

    public String getPassword() {
        return password;
    }

    public void setKeystore(KeyStore ks) {
        this.ks = ks;
    }

    public void setPassword(String password) {
        this.password = password;
    }

}
