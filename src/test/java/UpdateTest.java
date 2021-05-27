import clientsideEncryption.CryptoDatabaseAdapter;
import clientsideEncryption.core.crypto.CryptoUtils;
import clientsideEncryption.core.exceptions.ConfigurationFileError;
import clientsideEncryption.core.exceptions.InitializationError;
import clientsideEncryption.core.exceptions.InvalidQueryException;
import com.sun.management.OperatingSystemMXBean;

import javax.management.MBeanServerConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.sql.SQLException;

public class UpdateTest {
    public static void main(String[] args) throws ConfigurationFileError {
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
            MBeanServerConnection mbsc = ManagementFactory.getPlatformMBeanServer();

            OperatingSystemMXBean osMBean = ManagementFactory.newPlatformMXBeanProxy(
                    mbsc, ManagementFactory.OPERATING_SYSTEM_MXBEAN_NAME, OperatingSystemMXBean.class);

            long nanoBefore = System.nanoTime();
            long cpuBefore = osMBean.getProcessCpuTime();

            for (int i = 0; i<Integer.parseInt(rowsNumber);i++){
                cda.newQueryBuilder("UPDATE "+ tableName
                                    + " SET creditCardNumber = ?")
                        .setCipherParameter(1,"5699969", CryptoUtils.Algorithm.AES192)
                        .addToBatch();
            }

            cda.executeBatch();

            long nanoAfter = System.nanoTime();
            long cpuAfter = osMBean.getProcessCpuTime();
            System.out.println("Elapsed Time(ms): " + (nanoAfter-nanoBefore)/(1000000));
            System.out.println("CPU Time: " + (cpuAfter-cpuBefore)/1000000);

        } catch (InitializationError | IOException | InvalidQueryException | SQLException initializationError) {
            initializationError.printStackTrace();
        }
    }
}
