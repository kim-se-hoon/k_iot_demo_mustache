package org.example.demo_ssr_v1_1.board;

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
    @Query("SELECT b FROM Board b JOIN FETCH b.user ORDER BY b.createdAt DESC")
    List<Board> findAllWithUserOrderByCreatedAtDesc();

    // 게시글 ID로 조회 (작성자 정보 포함 - JOIN FETCH 사용해야 함)
    @Query("SELECT b FROM Board b JOIN FETCH b.user WHERE b.id = :id")
    Optional<Board> findByIdWithUser(@Param("id") Long id);

}