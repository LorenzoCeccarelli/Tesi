package clientsideEncryption;

import clientsideEncryption.core.exceptions.ConfigurationFileError;

public class Configuration {
    private String databaseUrl;
    private String databaseUsername;
    private String databasePassword;
    private String keystorePath;
    private String keystorePassword;
    private String masterKeyName;

    public Configuration(String databaseUrl, String databaseUsername, String databasePassword, String keystorePath, String keystorePassword, String masterKeyName){
        this.databaseUrl = databaseUrl;
        this.databaseUsername = databaseUsername;
        this.databasePassword = databasePassword;
        this.keystorePath = keystorePath;
        this.keystorePassword = keystorePassword;
        this.masterKeyName = masterKeyName;
    }

    public void validate() throws ConfigurationFileError {
        if(databaseUrl == null || databaseUsername == null || databasePassword == null || keystorePath == null || keystorePassword == null || masterKeyName == null)
            throw new ConfigurationFileError("At least one parameter is null");
    }

    public String getDatabasePassword() {
        return databasePassword;
    }

    public String getDatabaseUrl() {
        return databaseUrl;
    }

    public String getDatabaseUsername() {
        return databaseUsername;
    }

    public String getKeystorePassword() {
        return keystorePassword;
    }

    public String getKeystorePath() {
        return keystorePath;
    }

    public String getMasterKeyName() {
        return masterKeyName;
    }
}
