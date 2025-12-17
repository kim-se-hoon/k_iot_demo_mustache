package org.example.demo_ssr_v1_1.user;

import lombok.RequiredArgsConstructor;
import org.example.demo_ssr_v1_1._core.errors.exception.Exception400;
import org.example.demo_ssr_v1_1._core.errors.exception.Exception403;
import org.example.demo_ssr_v1_1._core.errors.exception.Exception404;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;

    @Transactional
    public User 회원가입(UserRequest.JoinDTO joinDTO) {

        // 1. 사용자명 중복 체크
        if(userRepository.findByUsername(joinDTO.getUsername()).isPresent()) {
            // isPresent -> 있으면 true 반환 , 없으면 false 반환
            throw new Exception400("이미 존재하는 사용자 이름입니다");
        }
        User user = joinDTO.toEntity();
        return userRepository.save(user);
    }

    public User 로그인(UserRequest.LoginDTO loginDTO) {

        // 사용자가 던진 값과  DB에 있는 사용자 이름과 비밀번호를 확인해 주어야 한다.
        User userEntity = userRepository.findByUsernameAndPassword(loginDTO.getUsername(),
                        loginDTO.getPassword())
                .orElse(null); // 로그인 실패시 null 반환

        if(userEntity == null) {
            throw new Exception400("사용자명 또는 비밀번호가 올바르지 않습니다");
        }

        return userEntity;
    }


    public User 회원정보수정화면(Long userId) {
        User userEntity = userRepository.findById(userId)
                .orElseThrow(() -> new Exception404("사용자를 찾을 수 없습니다"));
        if(!userEntity.isOwner(userId)) {
            throw new Exception403("회원 정보 수정 권한이 없습니다");
        }
        return userEntity;
    }

    // 데이터의 수정 ( 더티 체킹 -> 반드시 먼저 조회 -> 조회된 객체의 상태값 변경 --> 자동 반영 )
    // 1. 회원 정보 조회
    // 2. 인가 검사
    // 3. 엔티티 상태 변경 (더티 체킹)
    // 4. 트랜잭션이 일어나고 변경 된 User 엔티티 반환
    @Transactional
    public User 회원정보수정(UserRequest.UpdateDTO updateDTO, Long userId ) {
        User userEntity = userRepository.findById(userId)
                .orElseThrow(() -> new Exception404("사용자를 찾을 수 없습니다"));
        if(!userEntity.isOwner(userId))  {
            throw new Exception403("회원 정보 수정 권한이 없습니다");
        }
        // 객체 상태값 변경 (트랜잭션이 끝나면 자동으로 commit 및 반영해 줄꺼야)
        userEntity.update(updateDTO);
        return userEntity;
    }

}