package clientsideEncryption.core.logger;

import java.io.IOException;
import java.util.logging.*;

public class AdapterLogger {
    static private FileHandler fileTxt;
    static private Formatter formatterTxt;

    static public void setup() throws IOException {
        Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
        Logger rootLogger = Logger.getLogger("");
        rootLogger.removeHandler(rootLogger.getHandlers()[0]);
        logger.setLevel(Level.INFO);
        fileTxt = new FileHandler("src/main/resources/log.txt");
        formatterTxt = new CustomFormatter();
        fileTxt.setFormatter(formatterTxt);
        logger.addHandler(fileTxt);

    }
}
