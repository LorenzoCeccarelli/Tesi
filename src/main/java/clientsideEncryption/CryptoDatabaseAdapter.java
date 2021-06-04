package clientsideEncryption;

import clientsideEncryption.core.crypto.CryptoUtils;
import clientsideEncryption.core.database.DatabaseManager;
import clientsideEncryption.core.database.Query;
import clientsideEncryption.core.database.Tuple;
import clientsideEncryption.core.exceptions.*;
import clientsideEncryption.core.keystore.KeyStoreInfo;
import clientsideEncryption.core.keystore.KeystoreUtils;
import clientsideEncryption.core.logger.AdapterLogger;
import clientsideEncryption.core.token.ClearToken;
import clientsideEncryption.core.token.EncryptedToken;
import clientsideEncryption.core.token.Token;
import clientsideEncryption.core.token.TokenParser;

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
import java.util.Arrays;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;


public class CryptoDatabaseAdapter {

    private final Configuration configuration;
    private DatabaseManager dbManager;
    private KeyStoreInfo ksi;
    private SecretKey mek;
    private final static Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    /**
     * The constructor is private. It is possible to create a CryptoDatabaseAdapter using Builder inner static class
     * @param conf the configuration
     */
    private CryptoDatabaseAdapter(Configuration conf){
        this.configuration = conf;
    }

    /**
     * This methods initializes the CryptoDatabaseAdapter
     * @throws InitializationError throws it if an error during the initialization occurs
     */
    public void init() throws InitializationError {
        try {
            //Setup the logger
            String logfilePath = configuration.getLogfilePath();
            if(logfilePath == null)
                LOGGER.setLevel(Level.OFF);
            else AdapterLogger.setup(logfilePath);
            LOGGER.info("Init CryptoDatabaseAdapter");

            //Create a new database connection
            dbManager = new DatabaseManager(configuration.getDatabaseUrl(), configuration.getDatabaseUsername(), configuration.getDatabasePassword());
            dbManager.connect();
            LOGGER.info("Connected to: " + configuration.getDatabaseUrl());

            //Try to load the keystore
            if(KeystoreUtils.existKeystore(configuration.getKeystorePassword(), configuration.getKeystorePath())) {
                LOGGER.info("Keystore at '"+configuration.getKeystorePath()+"' exists");
                ksi = KeystoreUtils.loadKeystore(configuration.getKeystorePassword(), configuration.getKeystorePath());
            }
            else{
                //If the keystore does not exist create it
                LOGGER.warning("Keystore at '"+configuration.getKeystorePath()+"' does not exist. Creating it...");
                ksi = KeystoreUtils.createKeystore(configuration.getKeystorePassword());
                KeystoreUtils.saveKeystore(ksi, configuration.getKeystorePath());
                LOGGER.warning("Keystore at '"+configuration.getKeystorePath()+"' created");
            }

            //Check if the masterKey (called masterKeyName) exists, if not create it
            if(!KeystoreUtils.existKey(ksi,configuration.getMasterKeyName())){
                //Create the masterKey
                LOGGER.warning("Key named '"+configuration.getMasterKeyName()+"' does not exist. Creating it...");
                SecretKey masterKey = CryptoUtils.createSymKey(CryptoUtils.Algorithm.AES256);
                KeystoreUtils.insertKey(ksi,masterKey, configuration.getMasterKeyName());
                KeystoreUtils.saveKeystore(ksi, configuration.getKeystorePath());
                LOGGER.warning("Key named '"+configuration.getMasterKeyName()+"' creates");
            } else LOGGER.info("Key named '"+configuration.getMasterKeyName()+"' exists");


        } catch (SQLException | ConnectionParameterNotValid | CertificateException | KeyStoreException | IOException | NoSuchAlgorithmException | KeystoreOperationError | UnrecoverableKeyException throwables) {
            LOGGER.severe("Error: "+throwables.getMessage());
            throw new InitializationError(throwables.getMessage());
        }
    }

    /**
     * This method permits to create a new query and then use the generated QueryBuilder object for configuring it
     * @param query the query
     * @return the QueryBuilder object
     */
    public QueryBuilder newQueryBuilder(String query){
        return new QueryBuilder(query);
    }

    /**
     * This method executes the batch
     * @throws SQLException the generic SQL exception
     * @throws ConnectionParameterNotValid Throws it if the connection parameters are not valid  or if the batch is not instantiated
     */
    public void executeBatch() throws SQLException, ConnectionParameterNotValid {
        mek = null;
        dbManager.executeBatch();
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
        private String logfilePath;

        /**
         * The constructor builds a Builder object
         */
        public Builder(){}

        /**
         * This method permits to build a CryptoDatabaseAdapter starting from a configuration file
         * @param configurationFilePath the path of the filesystem where the configuration file is located
         * @return the new CryptoDatabaseAdapter object
         * @throws ConfigurationFileError throws it if an error in the configuration exists
         */
        public CryptoDatabaseAdapter buildByFile(String configurationFilePath) throws ConfigurationFileError {
            try {
                File configFile = new File(configurationFilePath);
                FileReader reader;
                reader = new FileReader(configFile);
                Properties props = new Properties();
                props.load(reader);
                url = props.getProperty("DatabaseUrl");
                username = props.getProperty("DatabaseUsername");
                password = props.getProperty("DatabasePassword");
                path = props.getProperty("KeystorePath");
                keystorePassword = props.getProperty("KeystorePassword");
                masterKeyName = props.getProperty("MasterKeyName");
                logfilePath = props.getProperty("LogFilePath");
                reader.close();
                Configuration conf = new Configuration(url, username, password, path, keystorePassword,masterKeyName, logfilePath);
                conf.validate();
                return new CryptoDatabaseAdapter(conf);
            } catch (IOException e) {
                throw new ConfigurationFileError(e.getMessage());
            }
        }

        /**
         * DatabaseUrl setter
         * @param url the database url
         * @return the Builder object
         */
        public Builder setDatabaseUrl(String url){
            this.url = url;
            return this;
        }

        /**
         * DatabaseUsername setter
         * @param username the username of the database user
         * @return the Builder object
         */
        public Builder setDatabaseUsername(String username){
            this.username = username;
            return this;
        }

        /**
         * DatabasePassword setter
         * @param password the password of the database user
         * @return the Builder object
         */
        public Builder setDatabasePassword(String password){
            this.password = password;
            return this;
        }

        /**
         * KeystorePath setter
         * @param path the path of the keystore in the filesystem
         * @return the Builder object
         */
        public Builder setKeystorePath(String path){
            this.path = path;
            return this;
        }

        /**
         * KeystorePassword setter
         * @param password the password
         * @return the Builder object
         */
        public Builder setKeystorePassword(String password){
            this.keystorePassword = password;
            return this;
        }

        /**
         * MasterKeyName setter
         * @param masterKeyName the name of the masterKey
         * @return the Builder object
         */
        public Builder setMasterKeyName(String masterKeyName){
            this.masterKeyName = masterKeyName;
            return this;
        }

        /**
         * LogFilePath sett
         * @param logfilePath the path of the log file
         * @return the Builder object
         */
        public Builder setLogFilePath(String logfilePath){
            this.logfilePath = logfilePath;
            return this;
        }
        /**
         * This method build and return a new CryptoDatabaseAdapter
         * @return the new CryptoDatabaseAdapter object
         * @throws ConfigurationFileError throws it if the configuration file contains errors
         */
        public CryptoDatabaseAdapter build() throws ConfigurationFileError {
            Configuration conf = new Configuration(this.url, this.username, this.password, this.path, this.keystorePassword,this.masterKeyName, this.logfilePath);
            conf.validate();
            return new CryptoDatabaseAdapter(conf);
        }
    }

    /**
     * This inner class permits to build a new query. You can create a QueryBuilder object only calling the newQueryBuilder
     * method on CryptoDatabaseAdapter object
     */
    public class QueryBuilder{
        private final Query query;

        /**
         * Private constructor. Call CryptoDatabaseAdapter.newQueryBuilder for creating a QueryBuilder object
         * @param query_ the Query in string format
         */
        private QueryBuilder(String query_){
            query = new Query(query_);
        }

        /**
         * This method permits to set an encrypted paramter to a query
         * @param position the field position in the query
         * @param value the value of the parameter
         * @param alg the encryption algorithm
         * @return the QueryBuilder object
         * @throws InvalidQueryException throws it if the query is invalid
         */
        public QueryBuilder setCipherParameter(int position, String value, CryptoUtils.Algorithm alg) throws InvalidQueryException {
            try {
                SecretKey sk = CryptoUtils.createSymKey(alg);
                if(mek==null) mek = KeystoreUtils.getKey(ksi,configuration.getMasterKeyName());
                byte[] ciphertext = CryptoUtils.encryptDataWithPrefixIV(sk, value.getBytes());
                byte[] cipherkey = CryptoUtils.encryptDataWithPrefixIV(mek,sk.getEncoded());
                EncryptedToken et = new EncryptedToken(cipherkey,ciphertext);
                query.setParameter(position,et.generateToken());
                return this;

            } catch (NoSuchAlgorithmException | EncryptionError | KeystoreOperationError | KeyDoesNotExistException e) {
                throw new InvalidQueryException(e.getMessage());
            }
        }

        /**
         * This method permits to set a non encrypted parameter to a query
         * @param position the field position in the query
         * @param value the value of the parameter
         * @return the QueryBuilder object
         */
        public QueryBuilder setParameter(int position, String value){
            ClearToken ct = new ClearToken(value);
            query.setParameter(position,ct.generateToken());
            return this;
        }

        /**
         * This method permits to run a query that modifies the database
         * @return false if an error occurs, true otherwise
         * @throws QueryExecutionError throws it if an error during the query execution occurs
         */
        public boolean run() throws QueryExecutionError {
            try {
                LOGGER.info("Run: "+ query.getQuery());
                mek = null;
                return dbManager.runMutableQuery(query);
            } catch (SQLException | ConnectionParameterNotValid throwables) {
                LOGGER.severe("Error: "+ Arrays.toString(throwables.getStackTrace()));
                throw new QueryExecutionError(throwables.getMessage());
            }
        }

        /**
         * This method permits to retrieve data from database and return the original data in transparent way to caller
         * @return the results in Set format
         * @throws QueryExecutionError throws it if an error during the query execution occurs
         */
        public Set<Tuple> runSelect() throws QueryExecutionError {
            try {
                LOGGER.info("Run: "+ query.getQuery());
                ResultSet rs = dbManager.runImmutableQuery(query);
                ResultSetMetaData rsmd = rs.getMetaData();
                mek = KeystoreUtils.getKey(ksi, configuration.getMasterKeyName());
                Set<Tuple> result = new HashSet<>();
                while (rs.next()) {
                    Tuple t = new Tuple();
                    for (int i = 0; i < rsmd.getColumnCount(); i++) {
                        String columnName = rsmd.getColumnName(i + 1);
                        String token = rs.getString(columnName);

                        //Parse token
                        Token tk = TokenParser.parseToken(token);
                        if(tk==null) throw new QueryExecutionError("Error during the extraction of the token");
                        //If token is an EncryptedToken
                        if (tk instanceof EncryptedToken) {
                            //Decrypt the key with MEK in order to retrieve the original key
                            byte[] key = CryptoUtils.decryptDataWithPrefixIV(mek, ((EncryptedToken) tk).getEncryptedKey());
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
                            //System.out.println(((ClearToken) tk).getData());
                            t.setColumn(columnName, originalData);
                        }

                    }
                    result.add(t);
                }
                mek = null;
                return result;
            } catch (SQLException | KeystoreOperationError | KeyDoesNotExistException | ConnectionParameterNotValid | DecryptionError throwables) {
                LOGGER.severe("Error: "+ Arrays.toString(throwables.getStackTrace()));
                throw new QueryExecutionError(throwables.getMessage());
            }
        }

        /**
         * This method adds the query to the batch
         * @throws SQLException a generic SQL exception
         * @throws ConnectionParameterNotValid Throws it if the connection parameters are not valid
         */
        public void addToBatch() throws SQLException, ConnectionParameterNotValid {
            dbManager.addBatch(query);
        }


    }
}
