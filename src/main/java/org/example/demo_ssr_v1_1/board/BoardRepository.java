package org.example.demo_ssr_v1_1.board;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.Optional;

public interface BoardRepository extends JpaRepository<Board, Long> {

    // 전체 게시글 조회 (기존에 사용하던 부분 사용 안 할 예정)
    //List<Board> findAllByOrderByCreatedAtDesc();

    // 게시글 전체 조회 (작성자 정보 포함, JOIN FETCH 사용)
//    @Query("SELECT b FROM Board b JOIN FETCH b.user ORDER BY b.createdAt DESC")
//    List<Board> findAllWithUserOrderByCreatedAtDesc();

    // 게시글 전체 조회 (페이징 처리) - 검색어 없을 때 사용
    // - 인수값은 우리가 생성한 Pageable 객체를 넣어 주면 된다.
    // - 리턴 타입은 Page 객체로 반환 된다.
    /**
     *
     * @param pageable 페이징 정보(페이지 번호, 페이지 크기, 정렬)
     * @return 페이징된 BoardList 를 가지고 있다(단 작성자 정보 포함)
     * ** JOIN FETCH 때문에 하이버 네이트가 쿼리를 이상하게 작성하는 것을 막는 처리 **
     * select 절에 DISTINCT 를 사용하면 정확한 count를 가져올 수 있음
     * countQuery - 전체 게시글에 개수를 빠르게 가져오기 위해 사용한다. 성능 문제
     */
    @Query(value = "SELECT DISTINCT b FROM Board b JOIN FETCH b.user ORDER BY b.createdAt DESC",
            countQuery = "SELECT COUNT(DISTINCT b) FROM Board b")
    Page<Board> findAllWithUserOrderByCreatedAtDesc(Pageable pageable);

    /**
     * 게시글 검색(제목 또는 내용, 페이징 포함) - 검색어 있을 때 사용
     */
    @Query(value = "SELECT DISTINCT b FROM Board b JOIN FETCH b.user " +
            "WHERE LOWER(b.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "   OR LOWER(b.content) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "ORDER BY b.createdAt DESC",
            countQuery = "SELECT COUNT(DISTINCT b) FROM Board b " +
                    "WHERE LOWER(b.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
                    "   OR LOWER(b.content) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Board> findByTitleContainingOrContentContaining(@Param("keyword") String keyword, Pageable pageable);

    // 게시글 ID로 조회 (작성자 정보 포함 - JOIN FETCH 사용해야 함)
    @Query("SELECT b FROM Board b JOIN FETCH b.user WHERE b.id = :id")
    Optional<Board> findByIdWithUser(@Param("id") Long id);

}