package it.polito.LorenzoCeccarelli.clientsideEncryption.database;

import it.polito.LorenzoCeccarelli.clientsideEncryption.exceptions.ConnectionParameterNotValid;

import java.sql.*;


public class DatabaseManager {

    private String url;
    private String username;
    private String password;
    private Connection con;
    private PreparedStatement ps;

    public DatabaseManager(String url, String username, String password){
        this.url = url;
        this.username = username;
        this.password = password;
    }

    public void setUrl(String url){
        this.url = url;
    }

    public void setUsername(String username){
        this.username = username;
    }

    public void setPassword(String password){
        this.password = password;
    }

    public void connect() throws SQLException, ConnectionParameterNotValid {
        if(url == null || username == null || password == null) throw new ConnectionParameterNotValid("Url, username or password are null");
        con = DriverManager.getConnection(url,username,password);
    }

    public ResultSet runImmutableQuery(Query query) throws SQLException, ConnectionParameterNotValid {
        if(con == null) throw new ConnectionParameterNotValid("The connection is not instantiated");
        setParameters(query);
        String[] tokens = query.getQuery().split(" ");
        if(!tokens[0].toUpperCase().equals("SELECT")) return null; //TODO("Thorw an exception")
        return ps.executeQuery();
    }

    public boolean runMutableQuery(Query query) throws SQLException, ConnectionParameterNotValid {
        if(con == null) throw new ConnectionParameterNotValid("The connection is not instantiated");
        setParameters(query);
        String[] tokens = query.getQuery().split(" ");
        if(tokens[0].toUpperCase().equals("SELECT")) return false; //TODO("Thorw an exception")
        return ps.execute();
    }

    private void setParameters(Query query) throws SQLException {
        this.ps = con.prepareStatement(query.getQuery());
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
