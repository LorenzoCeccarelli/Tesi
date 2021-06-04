package clientsideEncryption;

import clientsideEncryption.core.exceptions.ConfigurationFileError;

public class Configuration {
    private final String databaseUrl;
    private final String databaseUsername;
    private final String databasePassword;
    private final String keystorePath;
    private final String keystorePassword;
    private final String masterKeyName;
    private final String logfilePath;

    public Configuration(String databaseUrl, String databaseUsername, String databasePassword, String keystorePath, String keystorePassword, String masterKeyName, String logfilePath){
        this.databaseUrl = databaseUrl;
        this.databaseUsername = databaseUsername;
        this.databasePassword = databasePassword;
        this.keystorePath = keystorePath;
        this.keystorePassword = keystorePassword;
        this.masterKeyName = masterKeyName;
        this.logfilePath = logfilePath;
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

    public String getLogfilePath() {
        return logfilePath;
    }
}
