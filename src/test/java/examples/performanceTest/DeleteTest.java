package examples.performanceTest;

import clientsideEncryption.CryptoDatabaseAdapter;
import clientsideEncryption.core.database.Tuple;
import clientsideEncryption.core.exceptions.ConfigurationFileError;
import clientsideEncryption.core.exceptions.InitializationError;
import clientsideEncryption.core.exceptions.QueryExecutionError;
import com.sun.management.OperatingSystemMXBean;

import javax.management.MBeanServerConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.util.Set;

public class DeleteTest {
    public static void main(String[] args){
        try {
            CryptoDatabaseAdapter cda = new CryptoDatabaseAdapter.Builder().buildByFile("src/main/resources/config.properties");
            cda.init();
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            System.out.print("Inserisci il nome della tabella: ");
            String tableName = reader.readLine();
            MBeanServerConnection mbsc = ManagementFactory.getPlatformMBeanServer();

            OperatingSystemMXBean osMBean = ManagementFactory.newPlatformMXBeanProxy(
                    mbsc, ManagementFactory.OPERATING_SYSTEM_MXBEAN_NAME, OperatingSystemMXBean.class);

            long nanoBefore = System.nanoTime();
            long cpuBefore = osMBean.getProcessCpuTime();
            cda.newQueryBuilder("delete from "+tableName).run();
            long nanoAfter = System.nanoTime();
            long cpuAfter = osMBean.getProcessCpuTime();
            System.out.println("Elapsed Time(ms): " + (nanoAfter - nanoBefore) / (1000000));
            System.out.println("CPU Time: " + (cpuAfter - cpuBefore) / 1000000);
        } catch (InitializationError | IOException | ConfigurationFileError | QueryExecutionError initializationError) {
            initializationError.printStackTrace();
        }
    }
}
