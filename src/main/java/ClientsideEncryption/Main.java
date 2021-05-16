package ClientsideEncryption;

import ClientsideEncryption.exceptions.ConnectionParameterNotValid;
import ClientsideEncryption.exceptions.InvalidQueryException;
import ClientsideEncryption.keystore.KeyStoreInfo;
import ClientsideEncryption.keystore.KeystoreManager;
import ClientsideEncryption.utils.Query;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;
import java.sql.*;
import java.util.Arrays;

public class Main {

	public static void main (String[] args) {
		//System.out.println("Ciao!");
		//Class.forName("com.mysql.cj.jdbc.Driver");
		// variables
		final String url = "jdbc:mysql://localhost:6789/tesi";
		final String username = "root";
		final String password = "lorenzo97";

		DatabaseManager dbManager = new DatabaseManager(url,username,password);
		KeystoreManager ksManager = new KeystoreManager();
		EncryptionManager encManager = new EncryptionManager();
		try {
			dbManager.connect();
			//KeyStoreInfo ksi = ksManager.createKeystore("password");
			//ksManager.saveKeystore(ksi,"prova.p12");
			//SecretKey sk = encManager.createSymKey(Algorithm.AES128);
			//ksManager.insertKey(ksi,sk,"prova");
			//ksManager.saveKeystore(ksi,"prova.p12");
			KeyStoreInfo ksi = ksManager.loadKeystore("password","prova.p12");
			SecretKey sk = ksManager.getKey(ksi,"prova");
			//System.out.println(Arrays.toString(sk.getEncoded()));
			Query q = new Query("insert into crypto(id) values(?)");
			q.setParameter(1,encManager.encryptData(sk,"ciao",Algorithm.AES128));
			dbManager.runMutableQuery(q);
			Query q2 = new Query("select * from crypto");
			ResultSet rs = dbManager.runImmutableQuery(q2);
			while(rs.next()) {
				String a = rs.getString("id");
				System.out.println("ho ricevuto da db " +a );
				System.out.println(encManager.decryptData(sk,a,Algorithm.AES128));
			}

		} catch (SQLException throwables) {
			throwables.printStackTrace();
		} catch (ConnectionParameterNotValid connectionParameterNotValid) {
			connectionParameterNotValid.printStackTrace();
		} catch (CertificateException e) {
			e.printStackTrace();
		} catch (KeyStoreException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			e.printStackTrace();
		} catch (BadPaddingException e) {
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (UnrecoverableKeyException e) {
			e.printStackTrace();
		}
	}
}
