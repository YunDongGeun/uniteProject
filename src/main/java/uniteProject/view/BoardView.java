package uniteProject.view;

import uniteProject.persistence.dto.BoardDto;

import java.util.List;
public class BoardView {
    public void printAll(List<BoardDto> dtoes){
        System.out.println("모든 게시글");
        for(BoardDto dto:dtoes){
            System.out.println("dto.toString() = " + dto.toString());
        }
    }
}