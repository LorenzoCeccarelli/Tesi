package clientsideEncryption.core.token;

import java.util.Base64;

public class ClearToken extends Token {

    private String data;

    public ClearToken(String data){
        this.data = data;
    }

    public String getData() {
        return data;
    }

    @Override
    public String toString() {
        String header = Base64.getUrlEncoder().withoutPadding().encodeToString("PLAINTEXT".getBytes());
        String encodedData = Base64.getUrlEncoder().withoutPadding().encodeToString(data.getBytes());
        return header + "." + encodedData;
    }
}
