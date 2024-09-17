package uniteProject.global.jdbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MySqlDBConnection {
    private final String USER;
    private final String PASSWORD;
    private Connection connection;

    public MySqlDBConnection() {
        USER = "root";
        PASSWORD = "12341234";
    }

    public Connection getConnection() {
        Connection conn = null;
        try{
            //Class.forName("com.mysql.jdbc.Driver");
            // will be deprecated
            Class.forName("com.mysql.cj.jdbc.Driver");
            String url = "jdbc:mysql://localhost/uniteDB?characterEncoding=utf8&serverTimezone=UTC&useSSL=false";
            conn = DriverManager.getConnection(url, USER, PASSWORD);
            System.out.println("DB connection 완료");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch(SQLException e){
            System.out.println("error : " + e);
        }
        return conn;
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
