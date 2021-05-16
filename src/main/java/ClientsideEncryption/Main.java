package ClientsideEncryption;

import ClientsideEncryption.exceptions.ConnectionParameterNotValid;
import ClientsideEncryption.exceptions.InvalidQueryException;

import javax.crypto.SecretKey;
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
		try {
			ca = CryptoAdapter.newBuilder()
					.url(url)
					.username(user)
					.password(password)
			.createAndSaveKeystore("password","./file.p12");

			SecretKey sk = ca.createKey(Algorithm.AES128);
			ca.addAndSaveNewKey(sk,"prova");
		} catch (CertificateException e) {
			e.printStackTrace();
		} catch (KeyStoreException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
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
			  //ca.createTableByQuery("create table crypto (id binary(255) primary key)");



		} catch (SQLException | RuntimeException e) {
			System.out.println(e.getMessage());
		} catch (ConnectionParameterNotValid connectionParameterNotValid) {
			  connectionParameterNotValid.printStackTrace();
		}

	}

}
