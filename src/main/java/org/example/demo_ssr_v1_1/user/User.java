package org.example.demo_ssr_v1_1.user;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.domain.Page;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

// 엔티티 화면 보고 설계해 보세요.
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
    private String email;

    @CreationTimestamp
    private Timestamp createdAt;

    //@Column(nullable = false)
    private String profileImage; // 추가

    /**
     * User (1) : UserRole(N)
     * User 가 UserRole 리스트를 관리합니다 (단방향)
     * 실제 DB 의 'user_role_tb' 테이블에 user_id 라는 fK 컬럼 생깁니다.
     *
     * CascadeType.ALL
     * - 운명 공동체 User 를 저장하면 Role 로 자동 저장되고,
     * User 를 삭제하면 가지고 있던 Role 들도 다 같이 삭제됩니다.
     * (홍길동 (관리자, 일반사용자) 삭제하면 userRole 2가지 row도 자동 삭제 됩니다.
     *
     * orphanRemoval = true
     * 리스와 DB의 동기화 입니다.
     * Java의 roles 리스에서 요소(Role)를 .remove() 하거나 .clear() 하면
     * DB에서도 해당 데이터( DELETE)가 실제도 처리 됩니다.
     */
    // 나중에 다른 개발자가 findById(쿼리 메서드 호출할 때 신경 쓸 필요없이 전부 role 까지 반환 해줌)
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    private List<UserRole> roles = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false) // null 허용 안함
    @ColumnDefault("'LOCAL'") // 문자열이므로 작은 따옴표 필수
    private OAuthProvider provider;

    @Builder
    public User(Long id, String username, String password,
                String email, Timestamp createdAt, String profileImage,
                OAuthProvider provider) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.email = email;
        this.createdAt = createdAt;
        this.profileImage = profileImage;  // 추가
        this.provider = provider;

        // 방어적 코드 작성: 만약 null 값이면 기본값 LOCAL 로 저장
        if (provider == null) {
            this.provider = OAuthProvider.LOCAL;
        } else {
            this.provider = provider;
        }
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
        if (this.profileImage == null) {
            return null;
        }
        // https 로 시작하면 소셜 이미지  URL 그대로 리턴
        if (this.profileImage.startsWith("http")) {
            return this.profileImage;
        }
        // 아니면 (로컬 이미지) 폴더 경로 붙여서 리턴
        return "/images/" + this.profileImage;
    }


}