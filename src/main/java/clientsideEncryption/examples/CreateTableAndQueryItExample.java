package clientsideEncryption.examples;

import clientsideEncryption.CryptoDatabaseAdapter;
import clientsideEncryption.core.crypto.CryptoUtils;
import clientsideEncryption.core.database.Tuple;
import clientsideEncryption.core.exceptions.ConfigurationFileError;
import clientsideEncryption.core.exceptions.InitializationError;
import clientsideEncryption.core.exceptions.InvalidQueryException;
import clientsideEncryption.core.exceptions.QueryExecutionError;

import java.util.Set;

public class CreateTableAndQueryItExample {
    public static void main(String[] args){
        try {
            CryptoDatabaseAdapter cda = new CryptoDatabaseAdapter.Builder()
                    .buildByFile("src/main/resources/config.properties");
            cda.init();
            cda.newQueryBuilder("create table users(" +
                                                    "id varchar(255) primary key, " +
                                                    "name varchar(255) not null, " +
                                                    "creditCardNumber varchar(255) not null)")
                                                    .run();

            cda.newQueryBuilder("insert into users(id,name,creditCardNumber)" +
                                "values(?,?,?)")
                    .setParameter(1,"1")
                    .setParameter(2,"Josh")
                    .setCipherParameter(3,"54589720575", CryptoUtils.Algorithm.AES192)
                    .run();
            cda.newQueryBuilder("insert into users(id,name,creditCardNumber)" +
                    "values(?,?,?)")
                    .setParameter(1,"2")
                    .setParameter(2,"Mary")
                    .setCipherParameter(3,"786787575", CryptoUtils.Algorithm.AES128)
                    .run();
            cda.newQueryBuilder("insert into users(id,name,creditCardNumber)" +
                    "values(?,?,?)")
                    .setParameter(1,"3")
                    .setParameter(2,"Luke")
                    .setCipherParameter(3,"90904534", CryptoUtils.Algorithm.AES128)
                    .run();

            Set<Tuple> result = cda.newQueryBuilder("select * from users").runSelect();
            result.forEach(System.out::println);
        } catch (ConfigurationFileError | QueryExecutionError | InitializationError | InvalidQueryException configurationFileError) {
            configurationFileError.printStackTrace();
        }
    }
}
