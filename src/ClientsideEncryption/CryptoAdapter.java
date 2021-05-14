package ClientsideEncryption;

import ClientsideEncryption.exceptions.InvalidQueryException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.StringTokenizer;

public class CryptoAdapter {

	private DatabaseManager dbManager;

	private CryptoAdapter() {
		dbManager = new DatabaseManager();
	}
	
	public static CryptoAdapter builder() {

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
	
	public void connect() throws SQLException {
		dbManager.connect();
	}
	
	public CryptoAdapter createQuery(String query) throws InvalidQueryException, SQLException {
		QueryStatus qs = queryParser(query);
		if(qs == QueryStatus.IMMUTABLE)
			dbManager.createImmutableQuery(query);
		else dbManager.createMutableQuery(query);

		return this;
	}
	private QueryStatus queryParser(String query) throws InvalidQueryException {
		String split[] = query.split(" ");
		String first = split[0].toUpperCase();
		if (first.equals("SELECT"))
			return QueryStatus.IMMUTABLE;
		else if (first.equals("UPDATE") || first.equals("DELETE") || first.equals("INSERT"))
			return QueryStatus.MUTABLE;
		else throw new InvalidQueryException("Only SELECT, UPDATE, DELETE and INSERT operations are supported");
	}

	public void executeQuery() throws SQLException {
		dbManager.executeQuery();
	}
}
