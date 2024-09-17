import dormitoryMS.global.jdbc.MySqlDBConnection;

//public class Main {
//    public static void main(String[] args) {
//        System.out.println("실행시작");
//        MySqlDBConnection mySqlDBConnection = new MySqlDBConnection();
//
//        mySqlDBConnection.getConnection();
//        System.out.println("종료");
//    }
//}

import java.sql.*;
import java.time.LocalDateTime;

public class Main {
    public static void main(String args[]){
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;

        try{
            //Class.forName("com.mysql.jdbc.Driver"); //java 7이후 생략 가능
            Class.forName("com.mysql.cj.jdbc.Driver");
            //String url = "jdbc:mysql://localhost/mydb?characterEncoding=utf8&serverTimezone=UTC&useSSL=false";
            String url = "jdbc:mysql://localhost/uniteDB";
            conn = DriverManager.getConnection(url, "root", "12341234");

            String query = "SELECT * FROM BOARD";

            stmt = conn.createStatement();
            rs = stmt.executeQuery(query);
            while(rs.next()) {
                String id = rs.getString("id");
                String title = rs.getString("title");
                String writer = rs.getString("writer");
                String contents = rs.getString("contents");
                LocalDateTime regdate = rs.getTimestamp("regdate").toLocalDateTime();
                int hit = rs.getInt("hit");
                System.out.printf("%s | %s | %s | %s | %s | %d \n", id,title,writer,contents,regdate.toString(),hit);
                System.out.println("-------------------------------------");
            }
            PreparedStatement pstmt = null;
            String preQuery = "UPDATE BOARD SET contents=?,hit=? WHERE id = ?";
            pstmt = conn.prepareStatement(preQuery);
            pstmt.setString(1, "welcome");
            pstmt.setInt(2, 90);
            pstmt.setInt(3, 1);
            pstmt.executeUpdate();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch(SQLException e){
            System.out.println("error : " + e);
        }  finally{
            try{
                if(conn != null && !rs.isClosed()){
                    rs.close();
                }
                if(conn != null && !stmt.isClosed()){
                    stmt.close();
                }
                if(conn != null && !conn.isClosed()){
                    conn.close();
                }
            }
            catch(SQLException e){
                e.printStackTrace();
            }
        }
    }
}