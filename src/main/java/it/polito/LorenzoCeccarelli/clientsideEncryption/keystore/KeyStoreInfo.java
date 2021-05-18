package it.polito.LorenzoCeccarelli.clientsideEncryption.keystore;

import java.security.KeyStore;

public class KeyStoreInfo {

    private KeyStore ks;
    //private String path;
    private String password;

    public KeyStoreInfo(KeyStore ks/*, String path*/, String password){
        this.ks = ks;
        //this.path = path;
        this.password = password;
    }


    public KeyStore getKeystore(){
        return ks;
    }

    public String getPassword() {
        return password;
    }

    /*public String getPath() {
        return path;
    }*/

    public void setKeystore(KeyStore ks) {
        this.ks = ks;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    /*public void setPath(String path) {
        this.path = path;
    }*/
}
