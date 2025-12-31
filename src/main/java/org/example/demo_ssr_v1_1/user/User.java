package org.example.demo_ssr_v1_1.user;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.demo_ssr_v1_1._core.errors.exception.Exception400;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@Data
@Table(name = "user_tb")
@Entity
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String username;
    private String password;
    // 이메일 유니크 설정
    @Column(unique = true)
    private String email;

    // 자신의 포인트 추가
    @Column(nullable = false)
    @ColumnDefault("0")
    private Integer point = 0;

    @CreationTimestamp
    private Timestamp createdAt;

    //@Column(nullable = false)
    private String profileImage; // 추가

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    private List<UserRole> roles = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false) // null 허용 안함
    @ColumnDefault("'LOCAL'") // 문자열이므로 작은 따옴표 필수 !
    private OAuthProvider provider;

    @Builder
    public User(Long id, String username, String password,
                String email, Timestamp createdAt, String profileImage,
                OAuthProvider provider, Integer point) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.email = email;
        this.createdAt = createdAt;
        this.profileImage = profileImage;  // 추가

        // 방어적 코드 작성 : 만약 null 값이면 기본값 LOCAL 로 저장
        if(provider == null) {
            this.provider = OAuthProvider.LOCAL;
        } else {
            this.provider = provider;
        }

        this.point = (point != null) ? point : 0;
    }

    // 회원정보 수정 비즈니스 로직 추가
    // 추후 DTO  설계
    public void update(UserRequest.UpdateDTO updateDTO) {
        // 유효성 검사
        updateDTO.validate();
        this.password = updateDTO.getPassword();
        // 추가
        this.profileImage = updateDTO.getProfileImageFilename();
        // 더티 체킹 (변경 감지)
        // 트랜잭션이 끝나면 자동으로 update 쿼리 진행
    }

    // 회원 정보 소유자 확인 로직
    public boolean isOwner(Long userId) {
        return this.id.equals(userId);
    }

    // 새로운 역할을 추가하는 기능
    public void addRole(Role role) {
        this.roles.add(UserRole.builder().role(role).build());
    }

    // 해당 역할을 가지고 있는지 확인 하는 기능
    public boolean hasRole(Role role) {
        // roles (리스트)에 컬렉션이 없거나 비어 있는면 역할이 없는 것
        if(this.roles == null || this.roles.isEmpty()) {
            return false;
        }
        // 즉시 로딩이라서 바로 사용해도 LAZY 초기화 예외 안 터짐
        // any(어떤 것이든), Match(일치하다) 즉, 리스트 안에 있는 것들 중 단 하나라도 조건이 맞는게 있다면
        // true 를 반환해라!!
        return this.roles.stream()
                .anyMatch(r -> r.getRole() == role);
    }

    // 관리자 인지 여부를 반환 합니다.
    public boolean isAdmin() {
        return hasRole(Role.ADMIN);
    }

    // 템플릿에서 {{#isAdmin}}... {{/isAdmin}} 형태로 사용하는 편의 메서드 설계
    public boolean getIsAdmin() {
        return isAdmin();
    }

    // 화면에 표시할 역할 문자열 제공
    // - ADMIN 이면 "ADMIN" 제공
    public String getRoleDisplay() {
        return isAdmin() ? "ADMIN" : "USER";
    }

    // 분기 처리 (머스태치 화면에서는 서버에 저장된 이미지든, URL 이미지 이든 그냥
    // getProfilePath 변수를 호출하면 알아서 셋팅 되게 하고 싶다.
    public String getProfilePath() {
        if(this.profileImage == null) {
            return null;
        }
        // https 로 시작하면 소셜 이미지 URL 그대로 리턴
        if (this.profileImage.startsWith("http")) {
            return  this.profileImage;
        }
        // 아니면 (로컬 이미지) 폴더 경로 붙여서 리턴
        return "/images/" + this.profileImage;
    }

    // true, false
    public boolean isLocal() {
        // LOCAL -> true
        // KAKAO -> false
        return this.provider == OAuthProvider.LOCAL;
    }


    /**
     * 포인트 차감
     * @param amount (차감할 포인트 값)
     * @throws Exception400 포인트가 부족할 경우
     */
    public void deductPoint(Integer amount) {
        if(amount == null || amount <= 0) {
            throw new Exception400("차감할 포인트는 0보다 커야 합니다");
        }
        if(this.point < amount) {
            throw new Exception400("포인트가 부족합니다 현재 포인트: " + this.point);
        }
        this.point -=  amount;
    }

    /**
     * 포인트 차감
     * @param amount (차감할 포인트 값)
     * @throws Exception400 포인트가 부족할 경우
     */
    public void chargePoint(Integer amount) {
        if(amount == null || amount <= 0) {
            throw new Exception400("차감할 포인트는 0보다 커야 합니다");
        }
        this.point +=  amount;
    }

}
