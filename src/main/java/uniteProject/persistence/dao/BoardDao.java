package uniteProject.persistence.dao;

import uniteProject.persistence.dto.BoardDto;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class BoardDao {

    public List<BoardDto> findAll(){
        Connection conn = null;
        String sql = "SELECT * FROM board";
        Statement stmt= null;
        ResultSet rs = null;

        List<BoardDto> boardDTOs = new ArrayList<>();
        try {
            String url = "jdbc:mysql://localhost/uniteDB?characterEncoding=utf8&serverTimezone=UTC&useSSL=false";
            conn = DriverManager.getConnection(url, "root", "12341234");
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
            while (rs.next()) {
                BoardDto boardDTO = new BoardDto();
                Long id = rs.getLong("id");
                String title = rs.getString("title");
                String writer = rs.getString("writer");
                String contents = rs.getString("contents");
                LocalDateTime regdate = rs.getTimestamp("regdate").toLocalDateTime();
                int hit = rs.getInt(6);
                boardDTO.setId(id);
                boardDTO.setTitle(title);
                boardDTO.setWriter(writer);
                boardDTO.setContents(contents);
                boardDTO.setRegdate(regdate);
                boardDTO.setHit(hit);
                boardDTOs.add(boardDTO);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }finally {
            try{
                if(conn != null && !rs.isClosed()){
                    rs.close();
                }
                if(conn != null && !stmt.isClosed()){
                    rs.close();
                }
                if(conn != null && !conn.isClosed()){
                    conn.close();
                }
            }
            catch(SQLException e){
                e.printStackTrace();
            }
        }
        return boardDTOs;
    }
}