import uniteProject.persistence.dao.BoardDao;
import uniteProject.persistence.dto.BoardDto;
import uniteProject.service.BoardService;
import uniteProject.view.BoardView;

public class Main {
    public static void main(String[] args) {
        BoardDao boardDao = new BoardDao();
        BoardDto boardDto = new BoardDto();
        BoardService boardService = new BoardService(boardDao);
        BoardView view = new BoardView();

        view.printAll(boardService.findAll());
    }
}