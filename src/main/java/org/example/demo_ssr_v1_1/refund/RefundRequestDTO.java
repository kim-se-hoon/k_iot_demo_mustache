package org.example.demo_ssr_v1_1.refund;

import lombok.Data;
import org.example.demo_ssr_v1_1._core.errors.exception.Exception400;

public class RefundRequestDTO {

    @Data
    public static class RequestDTO {

        private Long paymentId;
        private String reason;

        public void validate() {
            if (paymentId == null) {
                throw new Exception400("결제 ID는 필수 입니다");
            }
            if(reason == null || reason.trim().isEmpty()) {
                throw new Exception400("환불 사유는 필수 입니다");
            }
            if(reason.length() > 500) {
                throw new Exception400("환불 사유는 500자 이하여하 합니다");
            }
        }
    }

}