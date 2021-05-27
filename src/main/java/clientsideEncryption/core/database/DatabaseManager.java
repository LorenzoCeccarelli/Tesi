package clientsideEncryption.core.database;

import clientsideEncryption.core.exceptions.ConnectionParameterNotValid;

import java.sql.*;

/**
 * This class manages the operation with the database
 */
public class DatabaseManager {

    private String url;
    private String username;
    private String password;
    private Connection con;
    private PreparedStatement ps;

    /**
     * The constructor receives three parameters for connecting to the db
     * @param url the url of the db
     * @param username the username of the db user
     * @param password the password of the db user
     */
    public DatabaseManager(String url, String username, String password){
        this.url = url;
        this.username = username;
        this.password = password;
    }

    /**
     * url Setter
     * @param url the url of the db
     */
    public void setUrl(String url){
        this.url = url;
    }

    /**
     * username Setter
     * @param username the username of the db user
     */
    public void setUsername(String username){
        this.username = username;
    }

    /**
     * password Setter
     * @param password th epassword of the db user
     */
    public void setPassword(String password){
        this.password = password;
    }

    /**
     * This method tries to connect to the db
     * @throws ConnectionParameterNotValid Throws it if the connection parameters are not valid
     * @throws SQLException throws it if an error during the connection occurs
     */
    public void connect() throws ConnectionParameterNotValid, SQLException {
        if(url == null || username == null || password == null) throw new ConnectionParameterNotValid("Url, username or password are null");
        con = DriverManager.getConnection(url,username,password);
    }

    /**
     * This method executes a SELECT query
     * @param query the query
     * @return the results
     * @throws SQLException throws it if an error during the query execution occurs
     * @throws ConnectionParameterNotValid Throws it if the connection parameters are not valid
     */
    public ResultSet runImmutableQuery(Query query) throws SQLException, ConnectionParameterNotValid {
        if(con == null) throw new ConnectionParameterNotValid("The connection is not instantiated");
        this.ps = con.prepareStatement(query.getQuery());
        setParameters(query);
        String[] tokens = query.getQuery().split(" ");
        if(!tokens[0].equalsIgnoreCase("SELECT")) throw new SQLException("The query is not a SELECT");
        ResultSet rs = ps.executeQuery();
        this.ps = null;
        return rs;
    }

    /**
     * This methods executes a query that modifies the db
     * @param query the query
     * @return true if successfully, false otherwise
     * @throws SQLException
     * @throws ConnectionParameterNotValid
     */
    public boolean runMutableQuery(Query query) throws SQLException, ConnectionParameterNotValid {
        if(con == null) throw new ConnectionParameterNotValid("The connection is not instantiated");
        this.ps = con.prepareStatement(query.getQuery());
        setParameters(query);
        String[] tokens = query.getQuery().split(" ");
        if(tokens[0].equalsIgnoreCase("SELECT")) throw new SQLException("The query must be different from a SELECT");
        boolean result = ps.execute();
        this.ps = null;
        return result;
    }

    /**
     * This methods adds a query into a batch. A new batch is created if it does not already exist
     * @param query the query
     * @throws ConnectionParameterNotValid Throws it if the connection parameters are not valid
     * @throws SQLException throws it if ar error occurs
     */
    public void addBatch(Query query) throws ConnectionParameterNotValid, SQLException {
        if(con == null) throw new ConnectionParameterNotValid("The connection is not instantiated");
        if(this.ps == null) {
            this.ps = con.prepareStatement(query.getQuery());
            setParameters(query);
        }
        setParameters(query);
        this.ps.addBatch();
    }

    /**
     * This methods executes the batch
     * @throws ConnectionParameterNotValid Throws it if the connection parameters are not valid  or if the batch is not instantiated
     * @throws SQLException throws it if an error occurs during the execution of tha batch
     */
    public void executeBatch() throws ConnectionParameterNotValid, SQLException {
        if(con == null) throw new ConnectionParameterNotValid("The connection is not instantiated");
        if(ps == null) throw new ConnectionParameterNotValid("the batch is not instantiated");
        ps.executeBatch();
    }

    private void setParameters(Query query) throws SQLException {
        //this.ps = con.prepareStatement(query.getQuery());
        if(query.getParameters() != null)
            query.getParameters().forEach((key,value)-> {
                try {
                    ps.setObject(key,value);
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            });
    }



}
