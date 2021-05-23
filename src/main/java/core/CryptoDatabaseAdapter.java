package core;

import core.crypto.CryptoUtils;
import core.database.DatabaseManager;
import core.database.Query;
import core.database.Tuple;
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
import java.io.FileReader;
import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

public class CryptoDatabaseAdapter {

    private Configuration configuration;
    private DatabaseManager dbManager;
    private KeyStoreInfo ksi;

    /**
     * The constructor is private. It is possible to create a CryptoDatabaseAdapter using Builder inner static class
     * @param conf
     */
    private CryptoDatabaseAdapter(Configuration conf){
        this.configuration = conf;
    }

    /**
     * This methods initializes the CryptoDatabaseAdapter
     * @throws InitializationError
     */
    public void init() throws InitializationError {
        try {
            //Create a new database connection
            dbManager = new DatabaseManager(configuration.getDatabaseUrl(), configuration.getDatabaseUsername(), configuration.getDatabasePassword());
            dbManager.connect();
            System.out.println("Connesso a: " + configuration.getDatabaseUrl());

            //Try to load the keystore
            if(KeystoreUtils.existKeystore(configuration.getKeystorePassword(), configuration.getKeystorePath()))
                ksi = KeystoreUtils.loadKeystore(configuration.getKeystorePassword(), configuration.getKeystorePath());
            else{
                //If the keystore does not exist create it
                System.out.println("Keystore non esistente");
                ksi = KeystoreUtils.createKeystore(configuration.getKeystorePassword());
                KeystoreUtils.saveKeystore(ksi, configuration.getKeystorePath());
            }

            //Check if the masterKey (called masterKeyName) exists, if not create it
            if(!KeystoreUtils.existKey(ksi,configuration.getMasterKeyName())){
                //Create the masterKey
                System.out.println(configuration.getMasterKeyName() + "Key does not exist");
                SecretKey masterKey = CryptoUtils.createSymKey(CryptoUtils.Algorithm.AES256);
                KeystoreUtils.insertKey(ksi,masterKey, configuration.getMasterKeyName());
                KeystoreUtils.saveKeystore(ksi, configuration.getKeystorePath());
            } else System.out.println(configuration.getMasterKeyName() + "Key exists");


        } catch (SQLException | ConnectionParameterNotValid | CertificateException | KeyStoreException | IOException | NoSuchAlgorithmException | KeystoreOperationError | UnrecoverableKeyException throwables) {
            throw new InitializationError(throwables.getMessage());
        }
    }

    /**
     * This method permits to create a new query and then use the generated QueryBuilder object for configuring it
     * @param query
     * @return
     */
    public QueryBuilder newQueryBuilder(String query){
        return new QueryBuilder(query);
    }

    /**
     * This static inner class permits to build a new CryptoDatabaseAdapter object
     */
    public static class Builder{
        private String url;
        private String username;
        private String password;
        private String path;
        private String keystorePassword;
        private String masterKeyName;

        /**
         * The constructor builds a Builder object
         */
        public Builder(){}

        /**
         * This method permits to build a CryptoDatabaseAdapter starting from a configuration file
         * @param configurationFilePath
         * @return
         * @throws ConfigurationFileError
         */
        public CryptoDatabaseAdapter buildByFile(String configurationFilePath) throws ConfigurationFileError {
            try {
                File configFile = new File(configurationFilePath);
                FileReader reader;
                reader = new FileReader(configFile);
                Properties props = new Properties();
                props.load(reader);
                String url = props.getProperty("DatabaseUrl");
                String username = props.getProperty("DatabaseUsername");
                String password = props.getProperty("DatabasePassword");
                String path = props.getProperty("KeystorePath");
                String keystorePassword = props.getProperty("KeystorePassword");
                String masterKeyName = props.getProperty("MasterKeyName");
                //System.out.println("Url is: " + url);
                //System.out.println("Username is: " + username);
                //System.out.println("Password is: " + password);
                //System.out.println("Keystore path is: " + path);
                //System.out.println("Keystore password is: " + keystorePassword);
                //System.out.println("Master key name is: "+ masterKeyName);
                reader.close();
                Configuration conf = new Configuration(url, username, password, path, keystorePassword,masterKeyName);
                conf.validate();
                return new CryptoDatabaseAdapter(conf);
            } catch (IOException e) {
                throw new ConfigurationFileError(e.getMessage());
            }
        }

        /**
         * DatabaseUrl setter
         * @param url
         * @return
         */
        public Builder setDatabaseUrl(String url){
            this.url = url;
            return this;
        }

        /**
         * DatabaseUsername setter
         * @param username
         * @return
         */
        public Builder setDatabaseUsername(String username){
            this.username = username;
            return this;
        }

        /**
         * DatabasePassword setter
         * @param password
         * @return
         */
        public Builder setDatabasePassword(String password){
            this.password = password;
            return this;
        }

        /**
         * KeystorePath setter
         * @param path
         * @return
         */
        public Builder setKeystorePath(String path){
            this.path = path;
            return this;
        }

        /**
         * KeystorePassword setter
         * @param password
         * @return
         */
        public Builder setKeystorePassword(String password){
            this.keystorePassword = password;
            return this;
        }

        /**
         * MasterKeyName setter
         * @param masterKeyName
         * @return
         */
        public Builder setMasterKeyName(String masterKeyName){
            this.masterKeyName = masterKeyName;
            return this;
        }

        /**
         * This method build and return a new CryptoDatabaseAdapter
         * @return
         * @throws ConfigurationFileError
         */
        public CryptoDatabaseAdapter build() throws ConfigurationFileError {
            Configuration conf = new Configuration(this.url, this.username, this.password, this.path, this.keystorePassword,this.masterKeyName);
            conf.validate();
            return new CryptoDatabaseAdapter(conf);
        }
    }

    /**
     * This inner class permits to build a new query. You can create a QueryBuilder object only calling the newQueryBuilder
     * method on CryptoDatabaseAdapter object
     */
    public class QueryBuilder{
        private Query query;

        /**
         * Private constructor. Call CryptoDatabaseAdapter.newQueryBuilder for creating a QueryBuilder object
         * @param query_
         */
        private QueryBuilder(String query_){
            query = new Query(query_);
        }

        /**
         * This method permits to set an encrypted paramter to a query
         * @param position
         * @param value
         * @param alg
         * @return
         * @throws InvalidQueryException
         */
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

        /**
         * This method permits to set a non encrypted parameter to a query
         * @param position
         * @param value
         * @return
         */
        public QueryBuilder setParameter(int position, String value){
            ClearToken ct = new ClearToken(value);
            query.setParameter(position,ct.toString());
            return this;
        }

        /**
         * This method permits to run a query that modifies the database
         * @return
         * @throws SQLException
         * @throws ConnectionParameterNotValid
         */
        public boolean run() throws QueryExecutionError {
            try {
                return dbManager.runMutableQuery(query);
            } catch (SQLException | ConnectionParameterNotValid throwables) {
                throw new QueryExecutionError(throwables.getMessage());
            }
        }

        /**
         * This method permits to retrieve data from database and return the original data in transparent way to caller
         * @return
         * @throws QueryExecutionError
         */
        public Set<Tuple> runSelect() throws QueryExecutionError {
            try {
                ResultSet rs = dbManager.runImmutableQuery(query);
                ResultSetMetaData rsmd = rs.getMetaData();
                SecretKey masterKey = KeystoreUtils.getKey(ksi, configuration.getMasterKeyName());
                Set<Tuple> result = new HashSet<>();
                while (rs.next()) {
                    Tuple t = new Tuple();
                    for (int i = 0; i < rsmd.getColumnCount(); i++) {
                        String columnName = rsmd.getColumnName(i + 1);
                        String token = rs.getString(columnName);

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
                            //System.out.println("Original data retrieved by the db: " + originalData);
                            t.setColumn(columnName, originalData);

                        }
                        //If token is a ClearToken print the data
                        if (tk instanceof ClearToken) {
                            String originalData = ((ClearToken) tk).getData();
                            System.out.println(((ClearToken) tk).getData());
                            t.setColumn(columnName, originalData);
                        }

                    }
                    result.add(t);
                }
                return result;
            } catch (SQLException | KeystoreOperationError | KeyDoesNotExistException | ConnectionParameterNotValid | DecryptionError throwables) {
               throw new QueryExecutionError(throwables.getMessage());
            }
        }

    }
}
