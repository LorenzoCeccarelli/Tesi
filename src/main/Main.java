package main;

import java.sql.*;

public class Main {

	public static void main (String[] args) {
		System.out.println("Ciao!");
		//Class.forName("com.mysql.cj.jdbc.Driver");
		// variables
	      final String url = "jdbc:mysql://localhost:6789/tesi";
	      final String user = "root";
	      final String password = "lorenzo97";

	      CryptoAdapter ca = CryptoAdapter.build()
	    		  .url(url)
	    		  .username(user)
	    		  .password(password);
	      // establish the connection
	      try {
			  ca.connect();
	    	  ca.executeQuery("create table prova(a int)");
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		} catch(RuntimeException e) {
			System.out.println(e.getMessage());
		}

		}

}
