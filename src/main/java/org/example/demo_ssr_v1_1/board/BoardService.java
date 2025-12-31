package org.example.demo_ssr_v1_1.board;

import lombok.RequiredArgsConstructor;
import org.example.demo_ssr_v1_1._core.errors.exception.Exception403;
import org.example.demo_ssr_v1_1._core.errors.exception.Exception404;
import org.example.demo_ssr_v1_1.purchase.PurchaseService;
import org.example.demo_ssr_v1_1.reply.ReplyRepository;
import org.example.demo_ssr_v1_1.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BoardService {

    private final BoardRepository boardRepository;
    private final ReplyRepository replyRepository;
    private final PurchaseService purchaseService; // 재활용

    /**
     * 게시글 목록 조회 (페이징 처리)
     * 트랜잭션
     *  - 읽기 전용 트랜잭션 - 성능 최적화
     * @return 게시글 목록 (생성일 기준으로 내림차순)
     */
    public BoardResponse.PageDTO 게시글목록조회(int page, int size, String keyword) {

        //** 상한선 제한 **
        // size 는 기본값 5, 최소 1, 최대 50으로 제한
        // 페이지 번호가 음수가 되는 것을 막습니다.
        int validPage = Math.max(0, page); // 양수값 보장
        // 최대값 제한    // 최대값 제한 50으로 보장
        // 최소값 제한    //  1 , -50 (양수값 보장) 최소값
        int validSize = Math.max(1, Math.min(50, size));

        // 정렬기준
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt" );
        Pageable pageable = PageRequest.of(validPage, validSize, sort);
        // [ ..스프링...  ]  [검색][초기화]

        Page<Board> boardPage;
        if (keyword != null && !keyword.trim().isEmpty()) {
            boardPage = boardRepository.findByTitleContainingOrContentContaining(keyword.trim(), pageable);
        } else {
            boardPage = boardRepository.findAllWithUserOrderByCreatedAtDesc(pageable);
        }


        return new BoardResponse.PageDTO(boardPage);
    }


//    /**
//     * 게시글 목록 조회
//     * 트랜잭션
//     *  - 읽기 전용 트랜잭션 - 성능 최적화
//     * @return 게시글 목록 (생성일 기준으로 내림차순)
//     */
//    public List<BoardResponse.ListDTO> 게시글목록조회() {
//        // 자바문법
//        // 데이터 타입을 변환 해서 맞춰 주어야 한다.
//        List<Board> boardList = boardRepository.findAllWithUserOrderByCreatedAtDesc();
//        // List<Board> ---> List<BoardResponse.ListDTO>
//        // 1. 반복문
////        List<BoardResponse.ListDTO> dotList = new ArrayList<>();
////        for(Board board : boardList) {
////            BoardResponse.ListDTO dto = new BoardResponse.ListDTO(board);
////            dotList.add(dto);
////        }
//
//        // 2. 람다 표현식
    ////        return boardList.stream()
    ////                .map(board -> new BoardResponse.ListDTO(board))
    ////                .collect(Collectors.toList());
//
//        // 3. 참조 메서드
//        return boardList.stream()
//                .map(BoardResponse.ListDTO::new)
//                .collect(Collectors.toList());
//        //return dotList;
//    }

    public BoardResponse.DetailDTO 게시글상세조회(Long boardId, Long userId) {

        Board board = boardRepository.findByIdWithUser(boardId)
                .orElseThrow(() -> new Exception404("게시글을 찾을 수 없어요"));

        // board -> 유료 글 / 무료 글 (저장 되어 있음)

        // 구매 여부 확인 (로그인한 사용자가 있는 경우만 확인 가능)
        boolean isPurchased = false;
        if(userId != null) {
            // 구매했다면 true 값을 반환 / 아니면 false 반환
            isPurchased = purchaseService.구매여부확인(userId, boardId);
        }
        //
        return new BoardResponse.DetailDTO(board, isPurchased);
    }

    // 1. 트랜잭션 처리
    // 2. repository 저장 처리
    @Transactional
    public Board 게시글작성(BoardRequest.SaveDTO saveDTO, User sessionUser) {
        // DTO 에서 직접 new 해서 생성한 Board 객체 일 뿐 아직 영속화 된 객체는 아니다!!
        Board board = saveDTO.toEntity(sessionUser);
        boardRepository.save(board);
        return board;
    }
    // 1. 게시글 조회
    // 2. 인가 처리
    public BoardResponse.UpdateFormDTO 게시글수정화면(Long boardId, Long sessionUserId) {
        // 1
        Board boardEntity = boardRepository.findByIdWithUser(boardId)
                .orElseThrow(() -> new Exception404("게시글을 찾을 수 없습니다"));
        // 2 인가 처리
        if(!boardEntity.isOwner(sessionUserId)) {
            throw new Exception403("게시글 수정 권한이 없습니다");
        }
        return new BoardResponse.UpdateFormDTO(boardEntity);
    }

    // 1. 트랜잭션 처리
    // 2. DB 에서 조회
    // 3. 인가 처리
    // 4. 조회된 board 에 상태값 변경 (더티 체킹)
    @Transactional
    public Board 게시글수정(BoardRequest.UpdateDTO updateDTO, Long boardId, Long sessionUserId) {

        // 2 (조회부터 해야 DB에 있는 Board 에 user_id 값을 확인 할 수 있음)
        Board boardEntity = boardRepository.findById(boardId)
                .orElseThrow(() -> new Exception404("게시글을 찾을 수 없습니다"));

        // 3.
        if(!boardEntity.isOwner(sessionUserId)) {
            throw new Exception403("게시글 수정 권한이 없습니다");
        }
        // 4.
        boardEntity.update(updateDTO); // 상태값 변경
        return boardEntity;
    }

    // 1. 트랜잭션 처리
    // 2. 게시글 조회
    // 3. 인가 처리
    // 4. Repository 에게 삭제 요청
    @Transactional
    public void 게시글삭제(Long boardId, Long sessionUserId) {
        // 2 (조회부터 해야 DB에 있는 Board 에 user_id 값을 확인 할 수 있음)
        Board boardEntity = boardRepository.findById(boardId)
                .orElseThrow(() -> new Exception404("게시글을 찾을 수 없습니다"));
        // 3
        if(!boardEntity.isOwner(sessionUserId)) {
            throw new Exception403("삭제 권한이 없습니다");
        }
        // 게시글 삭제 시 제약 오류 발생 하기 때문에 댓글 부터 삭제 후 게시글 삭제 처리 해야 함
        replyRepository.deleteByBoardId(boardId);
        // 4
        boardRepository.deleteById(boardId);
    }

}