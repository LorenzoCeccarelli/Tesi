package ClientsideEncryption;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

enum QueryStatus{
    MUTABLE,
    IMMUTABLE
}
class DatabaseManager {
    private String url;
    private String username;
    private String password;
    private Connection con;
    private PreparedStatement ps;
    private QueryStatus qs;

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

    public void connect() throws SQLException {
        con = DriverManager.getConnection(url,username,password);
    }

    public void createImmutableQuery(String query) throws SQLException {
        this.qs = QueryStatus.IMMUTABLE;
        this.ps = con.prepareStatement(query);
    }

    public void createMutableQuery(String query) throws SQLException {
        this.qs = QueryStatus.MUTABLE;
        this.ps = con.prepareStatement(query);
    }

    public void executeQuery() throws SQLException {
        if(qs == QueryStatus.MUTABLE)
            ps.execute();
        else if (qs == QueryStatus.IMMUTABLE)
            ps.executeQuery();
    }
}
