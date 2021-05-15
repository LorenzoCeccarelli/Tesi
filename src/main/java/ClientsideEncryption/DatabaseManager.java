package ClientsideEncryption;

import ClientsideEncryption.exceptions.ConnectionParameterNotValid;

import java.sql.*;

class DatabaseManager {

    private String url;
    private String username;
    private String password;
    private Connection con;
    private PreparedStatement ps;

    public DatabaseManager(){}

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

    public void prepareQuery(String query) throws SQLException, ConnectionParameterNotValid {
        if(con == null) throw new ConnectionParameterNotValid("The connection is not instantiated");
        this.ps = con.prepareStatement(query);
    }

    public boolean executeMutableQuery() throws SQLException {
        return ps.execute();
    }

    public ResultSet executeImmutableQuery() throws SQLException {
        return ps.executeQuery();
    }


}
