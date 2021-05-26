package clientsideEncryption.core.token;

import java.util.Base64;


public class EncryptedToken implements Token{

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


    @Override
    public String generateToken() {
        String header = "CIPHERTEXT";
        String ciphertextB64 = Base64.getEncoder().withoutPadding().encodeToString(ciphertext);
        String encryptedKeyB64 = Base64.getEncoder().withoutPadding().encodeToString(encryptedKey);
        return header+"."+ciphertextB64+"."+encryptedKeyB64;
    }
}
