package clientsideEncryption.examples;

import clientsideEncryption.CryptoDatabaseAdapter;
import clientsideEncryption.core.crypto.CryptoUtils;
import clientsideEncryption.core.exceptions.ConfigurationFileError;
import clientsideEncryption.core.exceptions.InitializationError;
import clientsideEncryption.core.exceptions.InvalidQueryException;
import clientsideEncryption.core.exceptions.QueryExecutionError;

public class Create10kEntriesTest {
    public static void main(String[] args){
        try {
            CryptoDatabaseAdapter cda = new CryptoDatabaseAdapter.Builder().buildByFile("src/main/resources/config.properties");
            cda.init();
            cda.newQueryBuilder("create table users(" +
                    "id varchar(255) primary key, " +
                    "name varchar(255) not null, " +
                    "creditCardNumber varchar(255) not null)")
                    .run();

            long startTime= System.currentTimeMillis();
            for (int i = 0; i<10000; i++){
                cda.newQueryBuilder("insert into users(id,name,creditCardNumber)" +
                        "values(?,?,?)")
                        .setParameter(1,String.valueOf(i))
                        .setParameter(2,"Josh")
                        .setCipherParameter(3,"54589720575", CryptoUtils.Algorithm.AES192)
                        .run();
            }
            long endTime = System.currentTimeMillis();
            long duration = (endTime-startTime) / 1000;
            System.out.println("Duration(seconds): "+ duration);

        } catch (ConfigurationFileError | QueryExecutionError | InvalidQueryException | InitializationError configurationFileError) {
            configurationFileError.printStackTrace();
        }

    }
}
