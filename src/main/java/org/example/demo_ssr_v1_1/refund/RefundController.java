package org.example.demo_ssr_v1_1.refund;


import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.example.demo_ssr_v1_1._core.errors.exception.Exception401;
import org.example.demo_ssr_v1_1.payment.Payment;
import org.example.demo_ssr_v1_1.payment.PaymentResponse;
import org.example.demo_ssr_v1_1.user.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@RequiredArgsConstructor
@Controller
public class RefundController {

    private final RefundService refundService;

    // 사용자 단 - > 환불 요청 화면 (검증 코드)
    @GetMapping("/refund/request/{paymentId}")
    public String refundRequestForm(@PathVariable Long paymentId,
                                    Model model, HttpSession session) {
        User sessionUser = (User) session.getAttribute("sessionUser");
        Payment payment = refundService.환불요청폼화면검증(paymentId, sessionUser.getId());
        PaymentResponse.ListDTO paymentDTO = new PaymentResponse.ListDTO(payment);
        model.addAttribute("payment", paymentDTO);
        return "refund/request-form";
    }

    // 사용자 - 환불 요청 기능
    @PostMapping("/refund/request")
    public String refundRequest(RefundRequestDTO.RequestDTO reqDTO,
                                HttpSession session) {
        User sessionUser = (User) session.getAttribute("sessionUser");
        if (sessionUser == null) {
            throw new Exception401("로그인이 필요 합니다");
        }
        reqDTO.validate();

        refundService.환불요청(sessionUser.getId(), reqDTO);
        return "redirect:/refund/list";
    }


    // /refund/list
    // 나의 환불 요청 목록 조회
    @GetMapping("/refund/list")
    public String refundList(Model model, HttpSession session) {
        User sessionUser = (User) session.getAttribute("sessionUser");
        if (sessionUser == null) {
            throw new Exception401("로그인이 필요 합니다");
        }
        List<RefundResponse.ListDTO> refundList = refundService.환불요청목록조회(sessionUser.getId());
        model.addAttribute("refundList", refundList);

        return "refund/list";
    }



}