package ClientsideEncryption;

import ClientsideEncryption.exceptions.ConnectionParameterNotValid;
import ClientsideEncryption.exceptions.InvalidQueryException;
import ClientsideEncryption.keystore.KeyStoreInfo;
import ClientsideEncryption.keystore.KeystoreManager;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.sql.*;

public class CryptoAdapter {

	private final  DatabaseManager dbManager;
	private final  KeystoreManager ksManager;
	private final EncryptionManager encManager;
	private KeyStoreInfo ksi;

	private CryptoAdapter() {
		dbManager = new DatabaseManager();
		ksManager = new KeystoreManager();
		encManager = new EncryptionManager();
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

	public void createTableByQuery(String query) throws SQLException, ConnectionParameterNotValid {
		dbManager.prepareQuery(query);
		dbManager.executeMutableQuery();
	}


	public CryptoAdapter createAndSaveKeystore(String password,String path) throws CertificateException, KeyStoreException, IOException, NoSuchAlgorithmException {
		ksi = ksManager.createKeystore(password);
		ksi.setPath(path);
		ksManager.saveKeystore(ksi);
		return this;
	}

	public CryptoAdapter loadKeystore(String path, String password) throws CertificateException, KeyStoreException, IOException, NoSuchAlgorithmException {
		ksi = new KeyStoreInfo(null,path,password);
		ksi.setKeystore(ksManager.loadKeystore(ksi).getKeystore());
		return this;
	}

	public void addAndSaveNewKey(SecretKey sk, String keyName/*, String password,String path*/) throws KeyStoreException, CertificateException, IOException, NoSuchAlgorithmException {
		ksManager.insertKey(ksi,sk,keyName);
		ksManager.saveKeystore(ksi);
	}

	public SecretKey createKey(Algorithm alg) throws NoSuchAlgorithmException {
		return encManager.createSymKey(alg);
	}


}
