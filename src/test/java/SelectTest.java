import clientsideEncryption.CryptoDatabaseAdapter;
import clientsideEncryption.core.database.Tuple;
import clientsideEncryption.core.exceptions.ConfigurationFileError;
import clientsideEncryption.core.exceptions.InitializationError;
import clientsideEncryption.core.exceptions.QueryExecutionError;
import com.sun.management.OperatingSystemMXBean;

import javax.management.MBeanServerConnection;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.Set;

public class SelectTest {
    public static void main(String[] args) throws InitializationError {
        try {
            CryptoDatabaseAdapter cda = new CryptoDatabaseAdapter.Builder().buildByFile("src/main/resources/config.properties");
            cda.init();
            MBeanServerConnection mbsc = ManagementFactory.getPlatformMBeanServer();

            OperatingSystemMXBean osMBean = ManagementFactory.newPlatformMXBeanProxy(
                    mbsc, ManagementFactory.OPERATING_SYSTEM_MXBEAN_NAME, OperatingSystemMXBean.class);

            long nanoBefore = System.nanoTime();
            long cpuBefore = osMBean.getProcessCpuTime();
            Set<Tuple> results = cda.newQueryBuilder("select * from users10k").runSelect();
            long nanoAfter = System.nanoTime();
            long cpuAfter = osMBean.getProcessCpuTime();
            System.out.println("Elapsed Time(ms): " + (nanoAfter-nanoBefore)/(1000000));
            System.out.println("CPU Time: " + (cpuAfter-cpuBefore)/1000000);
            results.forEach(System.out::println);
            System.out.println(results.size());
        } catch (ConfigurationFileError | QueryExecutionError | IOException configurationFileError) {
            configurationFileError.printStackTrace();
        }
    }
    }
