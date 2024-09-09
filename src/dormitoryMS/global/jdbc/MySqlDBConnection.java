package dormitoryMS.global.jdbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class MySqlDBConnection {

    private final String DB_URL;
    private final String USER;
    private final String PASSWORD;
    private Connection connection;

    public MySqlDBConnection() {
        DB_URL = "jdbc:mysql://127.0.0.1:3306/uniteDB";
        USER = "ydg";
        PASSWORD = "Ehdrms1601@";
    }

    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                try {
                    connection = DriverManager.getConnection(DB_URL, USER, PASSWORD);
                    System.out.println("DB Connection [성공]");
                } catch (SQLException e) {
                    System.out.println("DB Connection [실패]");
                    throw e;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return connection;
    }

    public void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                System.out.println("DB Connection closed");
            } catch (SQLException e) {
                System.out.println("Failed to close DB Connection");
                e.printStackTrace();
            }
        }
    }
}
