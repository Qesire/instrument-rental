package com.instrumentrental.controller.api;

import com.instrumentrental.dto.ApiResponse;
import com.instrumentrental.dto.payment.PaymentRequest;
import com.instrumentrental.dto.payment.PaymentResponse;
import com.instrumentrental.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/create")
    public ApiResponse<PaymentResponse> createPayment(@Valid @RequestBody PaymentRequest request) {
        return ApiResponse.success(paymentService.createPayment(request.getReservationId(), request.getChannel()));
    }

    @PostMapping("/callback/wechat")
    public ApiResponse<String> wechatCallback(@RequestBody String body) {
        log.info("WeChat callback received: {}", body);

        String transactionId = extractXmlValue(body, "transaction_id");
        String resultCode = extractXmlValue(body, "result_code");
        boolean success = "SUCCESS".equals(resultCode);

        paymentService.handleCallback("WECHAT", transactionId, success);
        return ApiResponse.success(success ? "SUCCESS" : "FAIL");
    }

    @PostMapping("/callback/alipay")
    public ApiResponse<String> alipayCallback(
            @RequestParam("trade_no") String tradeNo,
            @RequestParam("trade_status") String tradeStatus) {

        boolean success = "TRADE_SUCCESS".equals(tradeStatus);
        paymentService.handleCallback("ALIPAY", tradeNo, success);
        return ApiResponse.success(success ? "SUCCESS" : "FAIL");
    }

    private String extractXmlValue(String xml, String tag) {
        String openTag = "<" + tag + ">";
        String closeTag = "</" + tag + ">";
        String openCdata = "<![CDATA[";
        String closeCdata = "]]>";

        int start = xml.indexOf(openTag);
        if (start < 0) return null;
        start += openTag.length();

        int end = xml.indexOf(closeTag, start);
        if (end < 0) return null;

        String value = xml.substring(start, end);

        if (value.startsWith(openCdata) && value.endsWith(closeCdata)) {
            value = value.substring(openCdata.length(), value.length() - closeCdata.length());
        }

        return value.trim();
    }
}