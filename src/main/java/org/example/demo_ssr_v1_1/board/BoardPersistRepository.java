package org.example.demo_ssr_v1_1.board;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.ResponseBody;
// DB -- CRUD
@RequiredArgsConstructor
@Repository // IoC
public class BoardPersistRepository {

    // DI
    private final EntityManager entityManager;

    @Transactional
    public  Board save(Board board) {
        // 엔티티 매니저 자동으로 insert 쿼리 만들어 던진다.
        entityManager.persist(board);

        return board;
    }
}
