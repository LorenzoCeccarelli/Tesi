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

	      // establish the connection
	      try {
			Connection con = DriverManager.getConnection(url, user, password);
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}

		}

}
