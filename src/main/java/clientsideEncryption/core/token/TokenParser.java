package clientsideEncryption.core.token;

import java.util.Base64;

public class TokenParser {

    private TokenParser(){}

    public static Token parseToken(String token){
        String[] tokens = token.split("\\.");
        //String decodedHeader = new String(Base64.getUrlDecoder().decode(tokens[0]));
        if(tokens[0].equals("PLAINTEXT")){
            if(!(tokens.length ==2)) return null;
            String decodedData = new String(Base64.getDecoder().decode(tokens[1]));
            return new ClearToken(decodedData);
        }
        else if (tokens[0].equals("CIPHERTEXT")){
            if(!(tokens.length==3)) return null;
            byte[] ciphertext = Base64.getDecoder().decode(tokens[1]);
            byte[] encryptedKey = Base64.getDecoder().decode(tokens[2]);
            return new EncryptedToken(encryptedKey,ciphertext );
        }
        else return null;
    }
}
