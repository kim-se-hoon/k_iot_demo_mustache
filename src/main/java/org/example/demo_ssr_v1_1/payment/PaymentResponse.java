package org.example.demo_ssr_v1_1.payment;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;
import org.example.demo_ssr_v1_1._core.errors.exception.Exception400;
import org.example.demo_ssr_v1_1._core.utils.MyDateUtil;

public class PaymentResponse {

    @Data
    public static class PrepareDTO {
        private String merchantUid; // 생성된 우리 서버 주문 번호
        private Integer amount;  // 결제 금액
        private String impKey; // 포트원 RESET API 키 (필수)

        public PrepareDTO(String merchantUid, Integer amount, String impKey) {
            this.merchantUid = merchantUid;
            this.amount = amount;
            this.impKey = impKey;
        }
    }

    // 결제 검증 응답 DTO - JS로 내려줄 데이터
    @Data
    public static class VerifyDTO {
        private Integer amount;
        private Integer currentPoint;

        public VerifyDTO(Integer amount, Integer currentPoint) {
            this.amount = amount;
            this.currentPoint = currentPoint;
        }
    }


    // 포트원 액세스 토큰 응답 DTO 설계
    @Data
    public static class PortOneTokenResponse {
        private int code;
        private String message;
        private ResponseData response;

        @Data
        @JsonNaming(value = PropertyNamingStrategies.SnakeCaseStrategy.class)
        public static class ResponseData {
            // access_token --> @JsonNaming --> accessToken
            private String accessToken;
            private int now;
            private int expiredAt;
        }
    }

    //포트원 결제(포트원 서버에 DB 저장되어 있음) 조회 응답 DTO
    @Data
    public static class PortOnePaymentResponse {
        private int code;
        private String message;
        private PaymentData response;

        @Data
        @JsonNaming(value = PropertyNamingStrategies.SnakeCaseStrategy.class)
        public static class PaymentData {
            private Integer amount;
            private String impUid;
            private String merchantUid;
            private String status;
            private Long paidAt;
        }
    }

    // 결제 내역 리스트 응답 DTO
    @Data
    public static class ListDTO {
        private Long id;
        private String impUid;  // 포트원 결제 고유 번호
        private String merchantUid; // 주문번호
        private Integer amount;
        private String paidAt;
        // 화면에 보여질 상태 표시명
        private String status;
        private String statusDisplay;

        private Boolean isRefundable; // 환불 가능 여부 (화면에 표시 여부)

        public ListDTO(Payment payment, Boolean isRefundable) {
            this.id = payment.getId();
            this.impUid = payment.getImpUid();
            this.merchantUid = payment.getMerchantUid();
            this.amount = payment.getAmount();
            this.status = payment.getStatus();
            this.isRefundable = isRefundable != null ? isRefundable : false;

            // 상태 표시명 변환
            if ("paid".equals(payment.getStatus())) {
                this.statusDisplay = "결제완료";
            } else {
                this.statusDisplay = "환불완료";
            }

            // 날자 포멧팅
            if (payment.getCreatedAt() != null) {
                this.paidAt = MyDateUtil.timestampFormat(payment.getCreatedAt());
            }
        }

        public ListDTO(Payment payment) {
            //  [] ,  *(true, false)
            this(payment, "paid".equals(payment.getStatus()));
        }
    }
}