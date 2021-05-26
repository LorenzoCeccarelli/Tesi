package clientsideEncryption.core.token;

import java.util.Base64;

public class ClearToken implements Token {

    private String data;

    public ClearToken(String data){
        this.data = data;
    }

    public String getData() {
        return data;
    }

    @Override
    public String generateToken() {
        String header = "PLAINTEXT";
        String encodedData = Base64.getEncoder().withoutPadding().encodeToString(data.getBytes());
        return header + "." + encodedData;
    }
}
