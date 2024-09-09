import dormitoryMS.global.jdbc.MySqlDBConnection;

public class Main {
    public static void main(String[] args) {
        MySqlDBConnection mySqlDBConnection = new MySqlDBConnection();

        mySqlDBConnection.getConnection();
    }
}