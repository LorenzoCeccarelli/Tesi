package clientsideEncryption.examples;

import clientsideEncryption.CryptoDatabaseAdapter;
import clientsideEncryption.core.crypto.CryptoUtils;
import clientsideEncryption.core.database.Tuple;
import clientsideEncryption.core.exceptions.*;

import java.util.Set;

public class SimpleUsage {
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
        } catch (ClientsideEncryptionError error) {
            error.printStackTrace();
        }
    }
}
