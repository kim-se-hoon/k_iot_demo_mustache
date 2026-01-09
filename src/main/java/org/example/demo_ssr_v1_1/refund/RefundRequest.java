package org.example.demo_ssr_v1_1.refund;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.demo_ssr_v1_1.payment.Payment;
import org.example.demo_ssr_v1_1.user.User;
import org.hibernate.annotations.CreationTimestamp;

import java.sql.Timestamp;

@Data
@NoArgsConstructor
@Table(name = "refund_request_tb")
@Entity
public class RefundRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 원래는 우리 프로젝트 비즈니스 로직에서는 전체 환불 정책 1:1 구조이다.
    // 추후 확장성을 위해서 부분 환불을 도입한다면 1:N 설계 되어야 한다.
    // @onToOne 대신 @ManyToOne 에 unique 제약 조건을 걸어 1:1 구현 했다.
    // 추후 변경이 일어나다면 unique = true 만 제거 하면 된다.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id", nullable = false, unique = true)
    private Payment payment;

    @Column(length = 500)
    private String reason;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private RefundStatus status = RefundStatus.PENDING; // 기본값 대기

    // 관리자 환불 거절 사유
    @Column(length = 500)
    private String rejectReason;

    @CreationTimestamp
    private Timestamp createdAt;

    @CreationTimestamp
    private Timestamp updatedAt;

    // 사용자가 먼저 환불 요청에 의해성 -> row 생성 되기 때문 (reason <- 사용자 환불 사유)
    @Builder
    public RefundRequest(User user, Payment payment, String reason) {
        this.user = user;
        this.payment = payment;
        this.reason = reason;
        this.status = RefundStatus.PENDING;
    }

    ///// 편의 기능  ////

    // 환불 승인 처리
    public void approve() {
        this.status = RefundStatus.APPROVED;
    }

    // 환불을 거절 합니다.
    public void reject(String rejectReason) {
        this.status = RefundStatus.REJECTED;
        this.rejectReason = rejectReason;
    }

    // 대기중인 상태인지 확인
    public boolean isPending() {
        return this.status == RefundStatus.PENDING;
    }

    // 승인된 상태인지 확인
    public boolean isApproved() {
        return this.status == RefundStatus.APPROVED;
    }

    // 거절된 상태인지 확인
    public boolean isRejected() {
        return this.status == RefundStatus.REJECTED;
    }

}