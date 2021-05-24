package clientsideEncryption.core.token;

import java.util.Base64;


public class EncryptedToken extends Token{

    private byte[] encryptedKey;
    private byte[] ciphertext;

    public EncryptedToken(byte[] encryptedKey, byte[] ciphertext){
        this.encryptedKey = encryptedKey;
        this.ciphertext = ciphertext;
    }


    public byte[] getEncryptedKey(){
        return encryptedKey;
    }

    public byte[] getCiphertext() {
        return ciphertext;
    }

    /*public String toString(){
        String header = Base64.getUrlEncoder().withoutPadding().encodeToString("CIPHERTEXT".getBytes());
        String ciphertextB64 = Base64.getUrlEncoder().withoutPadding().encodeToString(ciphertext);
        String encryptedKeyB64 = Base64.getUrlEncoder().withoutPadding().encodeToString(encryptedKey);
        return header+"."+ciphertextB64+"."+encryptedKeyB64;
    }*/

    @Override
    public String generateToken() {
        String header = Base64.getUrlEncoder().withoutPadding().encodeToString("CIPHERTEXT".getBytes());
        String ciphertextB64 = Base64.getUrlEncoder().withoutPadding().encodeToString(ciphertext);
        String encryptedKeyB64 = Base64.getUrlEncoder().withoutPadding().encodeToString(encryptedKey);
        return header+"."+ciphertextB64+"."+encryptedKeyB64;
    }
}
