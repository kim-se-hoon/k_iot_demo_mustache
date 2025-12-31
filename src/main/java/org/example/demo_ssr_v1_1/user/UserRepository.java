package org.example.demo_ssr_v1_1.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;


public interface UserRepository extends JpaRepository<User, Long> {


    Optional<User> findByUsername(String username);
    // 이메일 존재 여부 확인 쿼리 네임드 메서드 설계
    Optional<User> findByEmail(String email);

    @Query("SELECT distinct u from User u left join fetch u.roles r " +
            "WHERE u.username = :username ")
    Optional<User> findByUsernameWithRoles(@Param("username") String username);

}