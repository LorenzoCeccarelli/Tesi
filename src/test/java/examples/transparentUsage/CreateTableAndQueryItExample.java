package examples.transparentUsage;

import clientsideEncryption.CryptoDatabaseAdapter;
import clientsideEncryption.core.crypto.CryptoUtils;
import clientsideEncryption.core.database.Tuple;
import clientsideEncryption.core.exceptions.ConfigurationFileError;
import clientsideEncryption.core.exceptions.InitializationError;
import clientsideEncryption.core.exceptions.InvalidQueryException;
import clientsideEncryption.core.exceptions.QueryExecutionError;
import com.sun.management.OperatingSystemMXBean;

import javax.management.MBeanServerConnection;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.sql.SQLException;
import java.util.Set;

public class CreateTableAndQueryItExample {
    public static void main(String[] args){
        try {
            CryptoDatabaseAdapter cda = new CryptoDatabaseAdapter.Builder()
                    .buildByFile("src/main/resources/config.properties");
            cda.init();

            MBeanServerConnection mbsc = ManagementFactory.getPlatformMBeanServer();

            OperatingSystemMXBean osMBean = ManagementFactory.newPlatformMXBeanProxy(
                    mbsc, ManagementFactory.OPERATING_SYSTEM_MXBEAN_NAME, OperatingSystemMXBean.class);

            long nanoBefore = System.nanoTime();
            long cpuBefore = osMBean.getProcessCpuTime();
            System.out.println(nanoBefore/1000000);
            System.out.println(cpuBefore/1000000);
            cda.newQueryBuilder("drop table if exists users").run();
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
                    .addToBatch();
            cda.newQueryBuilder("insert into users(id,name,creditCardNumber)" +
                    "values(?,?,?)")
                    .setParameter(1,"2")
                    .setParameter(2,"Mary")
                    .setCipherParameter(3,"786787575", CryptoUtils.Algorithm.AES128)
                    .addToBatch();
            cda.newQueryBuilder("insert into users(id,name,creditCardNumber)" +
                    "values(?,?,?)")
                    .setParameter(1,"3")
                    .setParameter(2,"Luke")
                    .setCipherParameter(3,"90904534", CryptoUtils.Algorithm.AES128)
                    .addToBatch();
            cda.executeBatch();

            long nanoAfter = System.nanoTime();
            long cpuAfter = osMBean.getProcessCpuTime();
            System.out.println("Elapsed Time(ms): " + (nanoAfter-nanoBefore)/(1000000));
            System.out.println("CPU Time: " + (cpuAfter-cpuBefore)/1000000);
            Set<Tuple> result = cda.newQueryBuilder("select * from users").runSelect();
            result.forEach(System.out::println);
        } catch (ConfigurationFileError | QueryExecutionError | InitializationError | InvalidQueryException | IOException | SQLException configurationFileError) {
            configurationFileError.printStackTrace();
        }
    }
}
