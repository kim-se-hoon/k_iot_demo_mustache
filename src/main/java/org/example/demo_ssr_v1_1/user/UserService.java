package org.example.demo_ssr_v1_1.user;

import lombok.RequiredArgsConstructor;
import org.example.demo_ssr_v1_1._core.errors.exception.Exception400;
import org.example.demo_ssr_v1_1._core.errors.exception.Exception403;
import org.example.demo_ssr_v1_1._core.errors.exception.Exception404;
import org.example.demo_ssr_v1_1._core.errors.exception.Exception500;
import org.example.demo_ssr_v1_1._core.utils.FileUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;

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

        // User 엔티티에 저장할 때는 String 이어야 하고 null 값도 가질 수 있음
        String profileImageFileName = null;

        // 2. 회원 가입시 파일이 넘어 왔는 확인
        if(joinDTO.getProfileImage() != null) {

            // 2.1 유효성 검사 (이미지 파일 이어야 함)
            try {
                if(!FileUtil.isImageFile(joinDTO.getProfileImage())) {
                    throw new Exception400("이미지 파일만 업로드 가능합니다");
                }
                profileImageFileName = FileUtil.saveFile(joinDTO.getProfileImage());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        }

        User user = joinDTO.toEntity(profileImageFileName);
        return userRepository.save(user);
    }

    public User 로그인(UserRequest.LoginDTO loginDTO) {

        // 사용자가 던진 값과  DB에 있는 사용자 이름과 비밀번호를 확인해 주어야 한다.
        User userEntity = userRepository.findByUsernameAndPasswordWithRoles(loginDTO.getUsername(),
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

        // 추가 - 프로필 이미지 처리
        // 중요 : 우리 프로젝트에서는 이미지 수정도 선택 사항 입니다.
        // 새로운 이미지 파일을 생성하고 기존에 있던 이미지 파일을 삭제해야 한다
        // 추가로 DB 정보도 업데이트 해야 한다.

        String oldProfileImage = userEntity.getProfileImage();
        // 분기 처리 - 이미지명이 있거나 또는 null 값이다.
        if(updateDTO.getProfileImage() != null && !updateDTO.getProfileImage().isEmpty()) {
            // 1. 이미지 파일인지 검증
            if(!FileUtil.isImageFile(updateDTO.getProfileImage())) {
                throw new Exception400("이미지 파일만 업로드 가능합니다");
            }

            // 2. 새 이미지 저장
            try {
                String newProfileImageFilename = FileUtil.saveFile(updateDTO.getProfileImage());
                // 새로 만들어진 파일 이름을 잠시 DTO에 보관 함
                updateDTO.setProfileImageFilename(newProfileImageFilename);

                if(oldProfileImage != null && !oldProfileImage.isEmpty()) {
                    // 기존에 있던 이미지를 삭제 처리 한다.
                    FileUtil.deleteFile(oldProfileImage);
                }
            } catch (IOException e) {
                throw new Exception500("파일 저장에 실패했습니다");
            }
            // end of 파일이 들어 왔을 때 처리
        } else {
            // 새 이미지가 업로드 되지 않았으면 기존 이미지 파일 이름 유지
            updateDTO.setProfileImageFilename(oldProfileImage);
        }
        // 객체 상태값 변경 (트랜잭션이 끝나면 자동으로 commit 및 반영해 줄꺼야)
        userEntity.update(updateDTO);
        return userEntity;
    }

    public User 마이페이지(Long sessionUserId) {

        User user = userRepository.findById(sessionUserId)
                .orElseThrow(() -> new Exception404("사용자를 찾을 수 없습니다"));

        // 인가 처리
        if(!user.isOwner(sessionUserId)) {
            throw new Exception403("권한이 없습니다");
        }
        return user;
    }

    @Transactional
    public User 프로필이미지삭제(Long sessionUserId) {
        // 1. 회원 정보 조회
        // 2. 회원 정보와 세션 id 값이 같은지 판단 -> 인가 처리
        // 3. 프로필 이미지가 있다면 삭제 (FileUtil) 헬퍼 클래스 사용 할 예정 (디스크에서 삭제)
        // 4. DB 에서 프로필 이름 null 로 업데이트 처리
        User userEntity = userRepository.findById(sessionUserId)
                .orElseThrow(() -> new Exception404("사용자를 찾을 수 없습니다"));

        if(!userEntity.isOwner(sessionUserId)) {
            throw new Exception403("프로필 이미지 삭제 권한이 없습니다");
        }

        String profileImage = userEntity.getProfileImage();
        if(profileImage != null && !profileImage.isEmpty()) {
            try {
                FileUtil.deleteFile(profileImage);
            } catch (IOException e) {
                System.err.println("프로필 이미지 파일 삭제 실패");
            }
        }

        // 객체 상태값 변경 (트랜 잭션이 끝나는 시점 더티 체킹 된)
        userEntity.setProfileImage(null);

        return userEntity;
    }

    public User 사용자이름조회(String username) {
        // 사용자 이름 여부만 조회 하는 기능
        return userRepository.findByUsername(username).orElse(null);
    }
    public void 소셜회원가입(User user) {
        userRepository.save(user);
    }
}