package org.example.demo_ssr_v1_1.payment;

import jakarta.servlet.http.HttpSession;
import org.example.demo_ssr_v1_1.user.User;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class PaymentApiController {

    private final PaymentService paymentService;

    public PaymentApiController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    // /api/payment/prepare
    @PostMapping("/api/payment/prepare")
    public ResponseEntity<?> preparePayment(@RequestBody PaymentRequest.PrepareDTO reqDTO,
                                            HttpSession session) {
        reqDTO.validate();
        // 누가 요청 한지 - 세션에서 추출
        User sessionUser = (User)session.getAttribute("sessionUser");
        if(sessionUser == null) {
            return ResponseEntity
                    .status(401)
                    .body(Map.of("message", "로그인이 필요합니다"));
        }
        // 결제 서비스 - 결정 사전 요청 생성 (주문번호표 생성 및 중복 확인)
        PaymentResponse.PrepareDTO prepareDTO = paymentService
                .결제요청생성(sessionUser.getId(), reqDTO.getAmount());
        // js -> 성공 응답 반화
        return  ResponseEntity.ok()
                .body(Map.of("merchant_uid", prepareDTO.getMerchantUid(),
                        "amount", prepareDTO.getAmount(),
                        "imp_key", prepareDTO.getImpKey()));
    }
}