package examples.directUsage;

import clientsideEncryption.core.crypto.CryptoUtils;
import clientsideEncryption.core.database.DatabaseManager;
import clientsideEncryption.core.database.Query;
import clientsideEncryption.core.exceptions.ConnectionParameterNotValid;
import clientsideEncryption.core.exceptions.DecryptionError;
import clientsideEncryption.core.exceptions.EncryptionError;
import clientsideEncryption.core.exceptions.KeystoreOperationError;
import clientsideEncryption.core.keystore.KeyStoreInfo;
import clientsideEncryption.core.keystore.KeystoreUtils;
import clientsideEncryption.core.token.ClearToken;
import clientsideEncryption.core.token.EncryptedToken;
import clientsideEncryption.core.token.Token;
import clientsideEncryption.core.token.TokenParser;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.NoSuchAlgorithmException;
import java.sql.*;

public class ClientsideEncryptionExample {

	public static void main(String[] args) {

		final String url = "jdbc:mysql://localhost:6789/tesi";
		final String username = "YourUsername";
		final String password = "YourPassword";

		DatabaseManager dbManager = new DatabaseManager(url, username, password);
		try {
			//Connect to database
			dbManager.connect();
			//Create or load a keystore (see KeystoreUsage in this folder)
			KeyStoreInfo ksi = KeystoreUtils.createKeystore("password");
			KeystoreUtils.saveKeystore(ksi,"prova.p12");

			//Create or load the master encryption key
			SecretKey sk = CryptoUtils.createSymKey(CryptoUtils.Algorithm.AES256);

			//In case of creation, add the MEK to keystore and save it into the filesystem
			KeystoreUtils.insertKey(ksi,sk,"MEK");
			KeystoreUtils.saveKeystore(ksi,"prova.p12");

			//Create on-the-fly the encryption key
			SecretKey ek = CryptoUtils.createSymKey(CryptoUtils.Algorithm.AES128);

			//Create the new query
			Query q = new Query("insert into crypto(id) values(?)"); //The DB must contain the table called "crypto" otherwise an error occurs

			//Encrypt the data with the encryption key
			String data = "VerySecret";
			System.out.println("Original data (before to send it to db) : " + data);
			byte[] ciphertext = CryptoUtils.encryptDataWithPrefixIV(ek, data.getBytes());

			//Encrypt the encryption key with the MEK
			byte[] cipherKey = CryptoUtils.encryptDataWithPrefixIV(sk, ek.getEncoded());

			//Create the token (the db saves this token)
			EncryptedToken c = new EncryptedToken(cipherKey, ciphertext);

			//Set the token into the query
			q.setParameter(1, c.generateToken());

			//Run the query (INSERT modifies the DB)
			dbManager.runMutableQuery(q);

			//Retrieve tokens from the db
			Query q2 = new Query("select * from crypto");
			ResultSet rs = dbManager.runImmutableQuery(q2);

			//Retrieve the original data from the token
			while (rs.next()) {

				String a = rs.getString("id"); //The table crypto must have a  column called "id"

				//Parse token
				Token tk = TokenParser.parseToken(a);
				//If token is an EncryptedToken
				if(tk instanceof EncryptedToken) {
					//Decrypt the key with MEK in order to retrieve the original key
					byte[] key = CryptoUtils.decryptDataWithPrefixIV(sk, ((EncryptedToken) tk).getEncryptedKey());
					//Reconstruct the original key from byte[] to SecretKey object
					SecretKey originalKey = new SecretKeySpec(key, 0, key.length, "AES");

					//Decrypt the original data e print it
					System.out.println("Original data retrived by the db: " + new String(CryptoUtils.decryptDataWithPrefixIV(originalKey, ((EncryptedToken) tk).getCiphertext())));
				}
				//If token is a ClearToken print the data
				if(tk instanceof ClearToken){
					System.out.println(((ClearToken) tk).getData());
				}
			}
		}catch (EncryptionError | DecryptionError | KeystoreOperationError | SQLException | ConnectionParameterNotValid | NoSuchAlgorithmException  encryptionError) {
			encryptionError.printStackTrace();
		}
	}
}
