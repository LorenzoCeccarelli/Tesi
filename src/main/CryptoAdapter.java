package main;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.security.*;

public class CryptoAdapter {

	private String url;
	private String username;
	private String password;
	private Connection con;
	
	private CryptoAdapter() {}
	
	public static CryptoAdapter build() {
		return new CryptoAdapter();
	}
	
	public CryptoAdapter url(String url) {
		this.url=url;
		return this;
	}
	
	public CryptoAdapter username(String username) {
		this.username = username;
		return this;
	}
	
	public CryptoAdapter password(String password) {
		this.password = password;
		return this;
	}
	
	public void connect() throws SQLException {
		con = DriverManager.getConnection(url,username,password);
	}
	
	public void executeQuery(String query) throws SQLException {
		if(con==null) throw new RuntimeException("You must connect to db");
		Statement stmt = con.createStatement();
		stmt.execute(query);
		
	}
				
}
