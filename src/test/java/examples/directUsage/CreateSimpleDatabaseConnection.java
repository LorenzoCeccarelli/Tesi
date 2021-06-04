package examples.directUsage;

import clientsideEncryption.core.database.DatabaseManager;
import clientsideEncryption.core.exceptions.ConnectionParameterNotValid;

import java.sql.SQLException;

public class CreateSimpleDatabaseConnection {
    public static void main(String[] args){

        final String url = "jdbc:mysql://localhost:6789/tesi";
        final String username = "YourUsername";
        final String password = "YourPassword";

        DatabaseManager dm = new DatabaseManager(url,username,password);
        try {
            dm.connect();
            System.out.println("Connection created at "+ url);
        } catch (SQLException | ConnectionParameterNotValid error) {
            error.printStackTrace();
        }
    }
}
