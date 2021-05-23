package core.examples;

import core.CryptoDatabaseAdapter;
import core.crypto.CryptoUtils;
import core.database.Tuple;
import core.exceptions.*;

import java.security.NoSuchAlgorithmException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Set;

public class TransparentUsage {
    public static void main(String[] args)  {

        try {
            CryptoDatabaseAdapter cda = new CryptoDatabaseAdapter.Builder().buildByFile("src/main/resources/config.properties");
            cda.init();
            boolean q = cda.newQueryBuilder("insert into crypto(id) values(?)")
                    .setCipherParameter(1,"VerySecret", CryptoUtils.Algorithm.AES128)
                    .run();
            cda.newQueryBuilder("insert into crypto(id) values(?)")
                    .setParameter(1,"NotSecret")
                    .run();
            Set<Tuple> rs = cda.newQueryBuilder("select * from crypto")
                    .runSelect();
            rs.forEach(System.out::println);
        } catch (ConfigurationFileError | SQLException | KeystoreOperationError | ConnectionParameterNotValid | NoSuchAlgorithmException | InvalidQueryException | DecryptionError | KeyDoesNotExistException configurationFileError) {
            configurationFileError.printStackTrace();
        }
    }
}
