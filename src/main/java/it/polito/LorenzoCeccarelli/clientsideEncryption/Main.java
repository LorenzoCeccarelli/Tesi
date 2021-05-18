package it.polito.LorenzoCeccarelli.clientsideEncryption;

import it.polito.LorenzoCeccarelli.clientsideEncryption.crypto.CryptoUtils;
import it.polito.LorenzoCeccarelli.clientsideEncryption.exceptions.ConnectionParameterNotValid;
import it.polito.LorenzoCeccarelli.clientsideEncryption.exceptions.DecryptionError;
import it.polito.LorenzoCeccarelli.clientsideEncryption.exceptions.EncryptionError;
import it.polito.LorenzoCeccarelli.clientsideEncryption.exceptions.KeystoreOperationError;
import it.polito.LorenzoCeccarelli.clientsideEncryption.keystore.KeyStoreInfo;
import it.polito.LorenzoCeccarelli.clientsideEncryption.keystore.KeystoreUtils;
import it.polito.LorenzoCeccarelli.clientsideEncryption.utils.Cookie;
import it.polito.LorenzoCeccarelli.clientsideEncryption.utils.Query;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;
import java.sql.*;
import java.util.Arrays;

public class Main {

	public static void main(String[] args) {
		//System.out.println("Ciao!");
		//Class.forName("com.mysql.cj.jdbc.Driver");
		// variables
		final String url = "jdbc:mysql://localhost:6789/tesi";
		final String username = "root";
		final String password = "lorenzo97";

		DatabaseManager dbManager = new DatabaseManager(url, username, password);
		try {
			dbManager.connect();
			//KeyStoreInfo ksi = ksManager.createKeystore("password");
			//ksManager.saveKeystore(ksi,"prova.p12");
			//SecretKey sk = encManager.createSymKey(Algorithm.AES128);
			//ksManager.insertKey(ksi,sk,"prova");
			//ksManager.saveKeystore(ksi,"prova.p12");
			KeyStoreInfo ksi = KeystoreUtils.loadKeystore("password", "prova.p12");
			//ksManager.insertKey(ksi,encManager.createSymKey(Algorithm.AES128),"MasterKey"); //devi salvarlo
			//ksManager.saveKeystore(ksi,"prova.p12");
			SecretKey sk = KeystoreUtils.getKey(ksi, "prova");
			System.out.println("Chiave di cifratura " + Arrays.toString(sk.getEncoded()));
			SecretKey masterKey = KeystoreUtils.getKey(ksi, "MasterKey");
			System.out.println("Master key " + Arrays.toString(masterKey.getEncoded()));
			Query q = new Query("insert into crypto(id) values(?)");
			byte[] ciphertext = CryptoUtils.encryptDataWithPrefixIV(sk, "ciao".getBytes());
			byte[] cipherKey = CryptoUtils.encryptDataWithPrefixIV(masterKey, sk.getEncoded());
			System.out.println("Stringa cifrata " + Arrays.toString(ciphertext));
			System.out.println("Chiave cifrata " + Arrays.toString(cipherKey));
			Cookie c = new Cookie(cipherKey, ciphertext);
			System.out.println("Invio al db " + c);
			q.setParameter(1, c.toString());
			dbManager.runMutableQuery(q);
			Query q2 = new Query("select * from crypto");
			ResultSet rs = dbManager.runImmutableQuery(q2);
			while (rs.next()) {
				String a = rs.getString("id");
				System.out.println("ho ricevuto da db " + a);
				Cookie ck = new Cookie(a);
				byte[] key = CryptoUtils.decryptDataWithPrefixIV(masterKey, ck.getEncryptedKey());
				System.out.println("La chiave con cui è stato cifrato: " + Arrays.toString(key));
				SecretKey originalKey = new SecretKeySpec(key, 0, key.length, "AES");
				System.out.println("Dati originali: " + new String(CryptoUtils.decryptDataWithPrefixIV(originalKey, ck.getCiphertext())));
				//System.out.println(encManager.decryptDataWithPrefixIV(sk,a,Algorithm.AES128));
			}
		}catch (EncryptionError | DecryptionError | KeystoreOperationError | SQLException | ConnectionParameterNotValid encryptionError) {
			encryptionError.printStackTrace();
		}
	}
}
