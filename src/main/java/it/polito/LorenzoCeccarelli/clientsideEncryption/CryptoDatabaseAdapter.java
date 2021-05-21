package it.polito.LorenzoCeccarelli.clientsideEncryption;


public class CryptoDatabaseAdapter {

    private CryptoDatabaseAdapter(){}

    public static class Builder{

        private String url;
        private String username;
        private String password;

        public Builder(){}

        public Builder setDatabaseConnection(String url, String username, String password){
            this.url = url;
            this.username= username;
            this.password = password;
            return this;
        }



    }
}
