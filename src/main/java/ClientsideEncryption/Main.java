package ClientsideEncryption;

import ClientsideEncryption.exceptions.ConnectionParameterNotValid;
import ClientsideEncryption.exceptions.InvalidQueryException;

import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.sql.*;

public class Main {

	public static void main (String[] args) {
		//System.out.println("Ciao!");
		//Class.forName("com.mysql.cj.jdbc.Driver");
		// variables
	      final String url = "jdbc:mysql://localhost:6789/tesi";
	      final String user = "root";
	      final String password = "lorenzo97";

		CryptoAdapter ca = null;
		//try {
			ca = CryptoAdapter.newBuilder()
					.url(url)
					.username(user)
					.password(password);
					//.createKeystore("ciao.p12");
		/*} catch (CertificateException e) {
			e.printStackTrace();
		} catch (KeyStoreException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}*/
		// establish the connection
	      try {
			  ca.connect();
	    	   ca.prepareQuery("insert into prova values (3)")
			  .executeMutableQuery();

		} catch (SQLException e) {
			System.out.println(e.getMessage());
		} catch(RuntimeException e) {
			System.out.println(e.getMessage());
		} catch (InvalidQueryException e) {
			  e.printStackTrace();
		  } catch (ConnectionParameterNotValid connectionParameterNotValid) {
			  connectionParameterNotValid.printStackTrace();
		  }

	}

}
