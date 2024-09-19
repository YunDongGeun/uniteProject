//public class Main {
//    public static void main(String[] args) {
//        System.out.println("실행시작");
//        MySqlDBConnection mySqlDBConnection = new MySqlDBConnection();
//
//        mySqlDBConnection.getConnection();
//        System.out.println("종료");
//    }
//}

import uniteProject.persistence.dao.BoardDao;
import uniteProject.persistence.dto.BoardDto;
import uniteProject.service.BoardService;
import uniteProject.view.BoardView;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        BoardDao boardDao = new BoardDao();
        BoardDto boardDto = new BoardDto();
        BoardService boardService = new BoardService(boardDao);
        BoardView view = new BoardView();

        view.printAll(boardService.findAll());
    }
}