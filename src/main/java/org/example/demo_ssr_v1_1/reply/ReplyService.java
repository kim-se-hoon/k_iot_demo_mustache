package org.example.demo_ssr_v1_1.reply;

import lombok.RequiredArgsConstructor;
import org.example.demo_ssr_v1_1._core.errors.exception.Exception403;
import org.example.demo_ssr_v1_1._core.errors.exception.Exception404;
import org.example.demo_ssr_v1_1.board.Board;
import org.example.demo_ssr_v1_1.board.BoardRepository;
import org.example.demo_ssr_v1_1.user.User;
import org.example.demo_ssr_v1_1.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReplyService {

    private final ReplyRepository replyRepository;
    private final BoardRepository boardRepository;
    private final UserRepository userRepository;

    // 댓글 목록 조회
    /**
     *  OSIV 대을하기 위해 DTO 설계, 계층간 결합도를 줄이기 위해 설계
     *  - JOIN FETCH 로 한번에 User를 들고 왔다.
     *
     */
    public List<ReplyResponse.ListDTO> 댓글목록조회(Long boardId, Long sessionUserId) {

        // 1. 조회 -- List<Reply>
        // 2. 인가 처리(X)
        // 3. 데이터 변환 (DTO 생성) List<ReplyResponse.ListDTO>

       // 조회를 했기 때문에 1차 캐쉬에 들어간 상태 -> 즉 영속화 되어 있음
       List<Reply> replyList = replyRepository.findByBoardIdWithUser(boardId);
       return replyList.stream()
               .map(reply -> new ReplyResponse.ListDTO(reply, sessionUserId))
               .collect(Collectors.toList());

    }

    // 댓글 작성
    @Transactional
    public Reply 댓글작성(ReplyRequest.SaveDTO saveDTO, Long sessionUserId) {
        // 1. 게시글 존재 유무 확인
        // 2. 현재 로그인 여부 확인
        // 3. 인가 처리 필요 없음
        // 4. 요청 DTO를 엔티티로 변환처리
        // 5. 저장 요청

        // 조회 했으니깐 board는 영속화 상태
        Board boardEntity = boardRepository.findById(saveDTO.getBoardId())
                .orElseThrow(() -> new Exception404("게시글을 찾을수 없습니다"));

        User userEntity = userRepository.findById(sessionUserId)
                .orElseThrow(() -> new Exception404("사용자를 찾을 수 없습니다"));

        // 비영속상태 입니다. (개발자가 직접 new 해서 객체를 생성함)
        Reply reply = saveDTO.toEntity(boardEntity, userEntity);

        return replyRepository.save(reply);
    }

    // 댓글 삭제
    @Transactional
    public Long 댓글삭제(Long replyId, Long sessionUserId) {
        // 댓글 조회 (findById(replyId) --> LAZY 때문에 댓글 작성자 정보는 없어요.
        Reply replyEntity = replyRepository.findByIdWithUser(replyId)
                .orElseThrow(() -> new Exception404("댓글을 찾을 수 없습니다"));

        if (!replyEntity.isOwner(sessionUserId)) {
            throw new Exception403("댓글 삭제 권한이 없습니다.");
        }

        Long boardId = replyEntity.getBoard().getId();

        replyRepository.delete(replyEntity);
        // 컨트롤러 단에서 리다이렉트 처리해서 다시 게시글 상세보기 호출 하기 위함 board id 값 임
        return boardId;

    }

}
