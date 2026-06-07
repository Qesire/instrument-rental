package com.instrumentrental.controller.api;

import com.instrumentrental.domain.model.User;
import com.instrumentrental.dto.ApiResponse;
import com.instrumentrental.dto.auth.LoginRequest;
import com.instrumentrental.dto.auth.LoginResponse;
import com.instrumentrental.dto.auth.WechatLoginRequest;
import com.instrumentrental.exception.BusinessException;
import com.instrumentrental.exception.ErrorCode;
import com.instrumentrental.security.JwtTokenProvider;
import com.instrumentrental.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserService userService;

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        User user = userService.findByPhone(request.getPhone());
        if (!userService.validatePassword(request.getPassword(), user.getPasswordHash())) {
            throw new BusinessException(ErrorCode.PASSWORD_ERROR);
        }

        String token = jwtTokenProvider.generateToken(user.getId(), user.getRole().name());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId());

        LoginResponse response = LoginResponse.builder()
                .token(token)
                .refreshToken(refreshToken)
                .userId(user.getId())
                .nickname(user.getNickname())
                .role(user.getRole().name())
                .phone(user.getPhone())
                .build();

        return ApiResponse.success(response);
    }

    @PostMapping("/wechat-login")
    public ApiResponse<LoginResponse> wechatLogin(@Valid @RequestBody WechatLoginRequest request) {
        User user = userService.createOrUpdateByWechat(request.getCode());

        String token = jwtTokenProvider.generateToken(user.getId(), user.getRole().name());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId());

        LoginResponse response = LoginResponse.builder()
                .token(token)
                .refreshToken(refreshToken)
                .userId(user.getId())
                .nickname(user.getNickname())
                .role(user.getRole().name())
                .phone(user.getPhone())
                .build();

        return ApiResponse.success(response);
    }

    @PostMapping("/refresh")
    public ApiResponse<LoginResponse> refresh(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        if (!jwtTokenProvider.validateToken(token)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        Long userId = jwtTokenProvider.getUserId(token);
        String role = jwtTokenProvider.getRole(token);

        String newToken = jwtTokenProvider.generateToken(userId, role);
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(userId);

        LoginResponse response = LoginResponse.builder()
                .token(newToken)
                .refreshToken(newRefreshToken)
                .userId(userId)
                .build();

        return ApiResponse.success(response);
    }
}