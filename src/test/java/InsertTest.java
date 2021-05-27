import clientsideEncryption.CryptoDatabaseAdapter;
import clientsideEncryption.core.crypto.CryptoUtils;
import clientsideEncryption.core.exceptions.ConfigurationFileError;
import clientsideEncryption.core.exceptions.InitializationError;
import clientsideEncryption.core.exceptions.InvalidQueryException;
import clientsideEncryption.core.exceptions.QueryExecutionError;
import com.sun.management.OperatingSystemMXBean;

import javax.management.MBeanServerConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.sql.SQLException;

public class InsertTest {
    public static void main(String[] args){
        try {
            CryptoDatabaseAdapter cda = new CryptoDatabaseAdapter.Builder().buildByFile("src/main/resources/config.properties");
            cda.init();
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            System.out.print("Inserisci il nome della tabella: ");
            String tableName = reader.readLine();
            System.out.print("Inserisci il numero di righe: ");
            String rowsNumber = reader.readLine();
            System.out.println(tableName);
            System.out.println(Integer.parseInt(rowsNumber));
            cda.newQueryBuilder("drop table if exists " + tableName).run();
            cda.newQueryBuilder("create table "+tableName+ "(" +
                    "id varchar(255) primary key, " +
                    "name varchar(255) not null, " +
                    "creditCardNumber varchar(255) not null)")
                    .run();

            MBeanServerConnection mbsc = ManagementFactory.getPlatformMBeanServer();

            OperatingSystemMXBean osMBean = ManagementFactory.newPlatformMXBeanProxy(
                    mbsc, ManagementFactory.OPERATING_SYSTEM_MXBEAN_NAME, OperatingSystemMXBean.class);

            long nanoBefore = System.nanoTime();
            long cpuBefore = osMBean.getProcessCpuTime();

            //long startTime= System.currentTimeMillis();
            for (int i = 0; i<Integer.parseInt(rowsNumber); i++){
                cda.newQueryBuilder("insert into "+tableName+"(id,name,creditCardNumber)" +
                        "values(?,?,?)")
                        .setParameter(1,String.valueOf(i))
                        .setParameter(2,"Josh")
                        .setCipherParameter(3,"54589720575", CryptoUtils.Algorithm.AES192)
                        .addToBatch();
            }
            cda.executeBatch();
            long nanoAfter = System.nanoTime();
            long cpuAfter = osMBean.getProcessCpuTime();
            System.out.println("Elapsed Time(ms): " + (nanoAfter-nanoBefore)/(1000000));
            System.out.println("CPU Time: " + (cpuAfter-cpuBefore)/1000000);

        } catch (ConfigurationFileError | QueryExecutionError | InvalidQueryException | InitializationError | IOException | SQLException configurationFileError) {
            configurationFileError.printStackTrace();
        }

    }
}
