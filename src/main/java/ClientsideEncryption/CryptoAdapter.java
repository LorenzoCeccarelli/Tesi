package ClientsideEncryption;

import ClientsideEncryption.exceptions.ConnectionParameterNotValid;
import ClientsideEncryption.exceptions.InvalidQueryException;

import java.io.IOException;
import java.lang.annotation.Documented;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.sql.*;

public class CryptoAdapter {

	private DatabaseManager dbManager;
	private KeystoreManager ksManager;

	private CryptoAdapter() {
		dbManager = new DatabaseManager();
		ksManager = new KeystoreManager();
	}
	
	public static CryptoAdapter newBuilder() {
		//TODO("Cambiare tipo di ritorno in CryptoadapterBuilder che offre .build() per tornare un CryptoAdapter");
		return new CryptoAdapter();
	}
	
	public CryptoAdapter url(String url) {
		dbManager.setUrl(url);
		return this;
	}
	
	public CryptoAdapter username(String username) {
		dbManager.setUsername(username);
		return this;
	}
	
	public CryptoAdapter password(String password) {
		dbManager.setPassword(password);
		return this;
	}
	
	public void connect() throws SQLException, ConnectionParameterNotValid {
		dbManager.connect();
	}


	public CryptoAdapter prepareQuery(String query) throws InvalidQueryException, SQLException, ConnectionParameterNotValid {

		if(!isValid(query)) throw new InvalidQueryException("Only SELECT, UPDATE, DELETE and INSERT operations are supported");
		dbManager.prepareQuery(query);
		return this;
	}

	private boolean isValid(String query){
		String[] split = query.split(" ");
		String first = split[0].toUpperCase();
		return first.equals("UPDATE") || first.equals("DELETE") || first.equals("INSERT") || first.equals("SELECT");
	}

	public boolean  executeMutableQuery() throws SQLException {
		return dbManager.executeMutableQuery();
	}

	public ResultSet executeImmutableQuery() throws SQLException {
		return dbManager.executeImmutableQuery();
	}

	public CryptoAdapter createKeystore(String keystoreName) throws CertificateException, KeyStoreException, IOException, NoSuchAlgorithmException {
		ksManager.createKeystore(keystoreName);
		return this;
	}

}
