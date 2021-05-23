package core;

import core.crypto.CryptoUtils;
import core.database.DatabaseManager;
import core.database.Query;
import core.exceptions.*;
import core.keystore.KeyStoreInfo;
import core.keystore.KeystoreUtils;
import core.token.ClearToken;
import core.token.EncryptedToken;
import core.token.Token;
import core.token.TokenParser;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

public class CryptoDatabaseAdapter {

    private Configuration configuration;
    private DatabaseManager dbManager;
    private KeyStoreInfo ksi;

    private CryptoDatabaseAdapter(Configuration conf){
        this.configuration = conf;
    }

    public void init() throws SQLException, ConnectionParameterNotValid, KeystoreOperationError, NoSuchAlgorithmException {
        //Create a new database connection
        dbManager = new DatabaseManager(configuration.getDatabaseUrl(), configuration.getDatabaseUsername(), configuration.getDatabasePassword());
        dbManager.connect();
        System.out.println("Connesso a: " + configuration.getDatabaseUrl());

        //Try to load the keystore
        try {
            ksi = KeystoreUtils.loadKeystore(configuration.getKeystorePassword(), configuration.getKeystorePath());
            System.out.println("Keystore esistente");


        } catch (FileNotFoundException ex){
            //If the keystore does not exist create it
            System.out.println("Keystore non esistente");
            ksi = KeystoreUtils.createKeystore(configuration.getKeystorePassword());
            KeystoreUtils.saveKeystore(ksi, configuration.getKeystorePath());
        }

        //Check if the masterKey (called masterKeyName) exists, if not create it
        try {
            SecretKey masterKey = KeystoreUtils.getKey(ksi, configuration.getMasterKeyName());
            System.out.println(configuration.getMasterKeyName() + "Key exists");
        } catch (KeyDoesNotExistException ex){
            //Create the masterKey
            System.out.println(configuration.getMasterKeyName() + "Key does not exist");
            SecretKey masterKey = CryptoUtils.createSymKey(CryptoUtils.Algorithm.AES256);
            KeystoreUtils.insertKey(ksi,masterKey, configuration.getMasterKeyName());
            KeystoreUtils.saveKeystore(ksi, configuration.getKeystorePath());
        }
    }

    public static class Builder{
        private String url;
        private String username;
        private String password;
        private String path;
        private String keystorePassword;
        private String masterKeyName;

        public Builder(){}

        public CryptoDatabaseAdapter buildByFile(String configurationFilePath) throws ConfigurationFileError {
            try {
                File configFile = new File(configurationFilePath);
                FileReader reader = null;
                reader = new FileReader(configFile);
                Properties props = new Properties();
                props.load(reader);
                String url = props.getProperty("DatabaseUrl");
                String username = props.getProperty("DatabaseUsername");
                String password = props.getProperty("DatabasePassword");
                String path = props.getProperty("KeystorePath");
                String keystorePassword = props.getProperty("KeystorePassword");
                String masterKeyName = props.getProperty("MasterKeyName");
                System.out.println("Url is: " + url);
                System.out.println("Username is: " + username);
                System.out.println("Password is: " + password);
                System.out.println("Keystore path is: " + path);
                System.out.println("Keystore password is: " + keystorePassword);
                System.out.println("Master key name is: "+ masterKeyName);
                reader.close();
                Configuration conf = new Configuration(url, username, password, path, keystorePassword,masterKeyName);
                conf.validate();
                return new CryptoDatabaseAdapter(conf);
            } catch (IOException e) {
                throw new ConfigurationFileError(e.getMessage());
            }
        }

        public Builder setDatabaseUrl(String url){
            this.url = url;
            return this;
        }

        public Builder setDatabaseUsername(String username){
            this.username = username;
            return this;
        }

        public Builder setDatabasePassword(String password){
            this.password = password;
            return this;
        }

        public Builder setKeystorePath(String path){
            this.path = path;
            return this;
        }

        public Builder setKeystorePassword(String password){
            this.keystorePassword = password;
            return this;
        }

        public Builder setMasterKeyName(String masterKeyName){
            this.masterKeyName = masterKeyName;
            return this;
        }

        public CryptoDatabaseAdapter build() throws ConfigurationFileError {
            Configuration conf = new Configuration(this.url, this.username, this.password, this.path, this.keystorePassword,this.masterKeyName);
            conf.validate();
            return new CryptoDatabaseAdapter(conf);
        }
    }

    public class QueryBuilder{
        private Query query;

        public QueryBuilder(String query_){
            query = new Query(query_);
        }

        public QueryBuilder setCipherParameter(int position, String value, CryptoUtils.Algorithm alg) throws InvalidQueryException {
            try {
                SecretKey sk = CryptoUtils.createSymKey(alg);
                SecretKey masterKey = KeystoreUtils.getKey(ksi,configuration.getMasterKeyName());
                byte[] ciphertext = CryptoUtils.encryptDataWithPrefixIV(sk, value.getBytes());
                byte[] cipherkey = CryptoUtils.encryptDataWithPrefixIV(masterKey,sk.getEncoded());
                EncryptedToken et = new EncryptedToken(cipherkey,ciphertext);
                query.setParameter(position,et.toString());
                return this;

            } catch (NoSuchAlgorithmException | EncryptionError | KeystoreOperationError | KeyDoesNotExistException e) {
                throw new InvalidQueryException(e.getMessage());
            }
        }

        public QueryBuilder setParameter(int position, String value){
            ClearToken ct = new ClearToken(value);
            query.setParameter(position,ct.toString());
            return this;
        }

        public boolean runMutable() throws SQLException, ConnectionParameterNotValid {
            return dbManager.runMutableQuery(query);
        }

        public Set<String> runImmutable() throws SQLException, ConnectionParameterNotValid, DecryptionError, KeystoreOperationError, KeyDoesNotExistException {
            ResultSet rs = dbManager.runImmutableQuery(query);
            ResultSetMetaData rsmd = rs.getMetaData();
            SecretKey masterKey = KeystoreUtils.getKey(ksi, configuration.getMasterKeyName());
            Set<String> result = new HashSet<>();
            while (rs.next()) {
                for(int i=0; i<rsmd.getColumnCount();i++) {
                    String token = rs.getString(rsmd.getColumnLabel(i+1));

                    //Parse token
                    Token tk = TokenParser.parseToken(token);
                    //If token is an EncryptedToken
                    if (tk instanceof EncryptedToken) {
                        //Decrypt the key with MEK in order to retrieve the original key
                        byte[] key = CryptoUtils.decryptDataWithPrefixIV(masterKey, ((EncryptedToken) tk).getEncryptedKey());
                        //Reconstruct the original key from byte[] to SecretKey object
                        SecretKey originalKey = new SecretKeySpec(key, 0, key.length, "AES");
                        //Decrypt the original data e print it
                        String originalData = new String(CryptoUtils.decryptDataWithPrefixIV(originalKey, ((EncryptedToken) tk).getCiphertext()));
                        System.out.println("Original data retrieved by the db: " + originalData);

                    }
                    //If token is a ClearToken print the data
                    if (tk instanceof ClearToken) {
                        System.out.println(((ClearToken) tk).getData());
                    }
                }
            }
            return result;
        }

    }



}
