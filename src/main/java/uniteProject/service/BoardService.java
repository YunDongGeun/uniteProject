package uniteProject.service;

import uniteProject.persistence.dao.BoardDao;
import uniteProject.persistence.dto.BoardDto;

import java.util.List;
public class BoardService {
    private final BoardDao boardDAO;
    public BoardService(BoardDao boardDAO) { //의존성 주입
        this.boardDAO = boardDAO;
    }
    public List<BoardDto> findAll(){
        return boardDAO.findAll();
    }
}
