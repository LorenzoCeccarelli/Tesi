package it.polito.LorenzoCeccarelli.clientsideEncryption.utils;

import java.util.Arrays;
import java.util.Base64;

public class Cookie {

    private byte[] encryptedKey;
    private byte[] ciphertext;

    public Cookie(byte[] encryptedKey, byte[] ciphertext){
        this.ciphertext = ciphertext;
        this.encryptedKey = encryptedKey;
    }

    public Cookie(String cookie){
        String[] tokens = cookie.split("\\.");
        System.out.println(tokens[0]);
        System.out.println(tokens[1]);
        this.ciphertext = Base64.getDecoder().decode(tokens[0]);
        this.encryptedKey = Base64.getDecoder().decode(tokens[1]);
        System.out.println("Decoded ciphertext "+ Arrays.toString(ciphertext));
        System.out.println("Decoded encryptedKey "+ Arrays.toString(encryptedKey));

    }

    public byte[] getEncryptedKey(){
        return encryptedKey;
    }

    public byte[] getCiphertext() {
        return ciphertext;
    }

    public String toString(){
        String ciphertextB64 = Base64.getEncoder().withoutPadding().encodeToString(ciphertext);
        String encryptedKeyB64 = Base64.getEncoder().withoutPadding().encodeToString(encryptedKey);
        return ciphertextB64+"."+encryptedKeyB64;
    }
}
