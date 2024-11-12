package uniteProject.persistence.dao;

import uniteProject.persistence.PooledDataSource;
import uniteProject.persistence.dto.MemberDto;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class MemberDao {
    private final DataSource ds = PooledDataSource.getDataSource();

    public List<MemberDto> findAll(){
        Connection conn = null;
        String sql = "SELECT * FROM members";
        Statement stmt= null;
        ResultSet rs = null;

        List<MemberDto> memberDtoList = new ArrayList<>();
        try {
            conn = ds.getConnection();
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
            while (rs.next()) {
                MemberDto memberDto = new MemberDto();
                Long id = rs.getLong("id");
                String username = rs.getString("username");
                String password = rs.getString("password");
                String role = rs.getString("role");
                LocalDateTime createdAt = rs.getTimestamp("created_at").toLocalDateTime();
                memberDto.setId(id);
                memberDto.setUsername(username);
                memberDto.setPassword(password);
                memberDto.setRole(role);
                memberDto.setCreateAt(createdAt);
                memberDtoList.add(memberDto);
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
        return memberDtoList;
    }
}
