package core.token;

import java.util.Base64;

public class TokenParser {

    private TokenParser(){}

    public static Token parseToken(String token){
        String[] tokens = token.split("\\.");
        String decodedHeader = new String(Base64.getUrlDecoder().decode(tokens[0]));
        if(decodedHeader.equals("PLAINTEXT")){
            String decodedData = new String(Base64.getUrlDecoder().decode(tokens[1]));
            return new ClearToken(decodedData);
        }
        else if (decodedHeader.equals("CIPHERTEXT")){
            byte[] ciphertext = Base64.getUrlDecoder().decode(tokens[1]);
            byte[] encryptedKey = Base64.getUrlDecoder().decode(tokens[2]);
            return new EncryptedToken(encryptedKey,ciphertext );
        }
        else return null;
    }
}
