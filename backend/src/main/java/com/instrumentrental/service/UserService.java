package com.instrumentrental.service;

import com.instrumentrental.domain.enums.UserStatus;
import com.instrumentrental.domain.model.User;
import com.instrumentrental.domain.model.OverdueRecord;
import com.instrumentrental.domain.enums.UserRole;
import com.instrumentrental.domain.repository.UserRepository;
import com.instrumentrental.domain.repository.OverdueRecordRepository;
import com.instrumentrental.exception.BusinessException;
import com.instrumentrental.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final OverdueRecordRepository overdueRecordRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 通过微信 openid 创建或查找用户。
     */
    @Transactional
    public User createOrUpdateByWechat(String openid) {
        return userRepository.findByOpenid(openid)
                .orElseGet(() -> {
                    User newUser = User.builder()
                            .openid(openid)
                            .nickname("微信用户")
                            .role(UserRole.ROLE_USER)
                            .status(UserStatus.ACTIVE)
                            .build();
                    User saved = userRepository.save(newUser);
                    log.info("Created new user via WeChat: openid={}, userId={}", openid, saved.getId());
                    return saved;
                });
    }

    /**
     * 通过手机号查找用户。
     */
    @Transactional(readOnly = true)
    public User findByPhone(String phone) {
        return userRepository.findByPhone(phone)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

    /**
     * 通过 openid 查找用户。
     */
    @Transactional(readOnly = true)
    public User findByOpenid(String openid) {
        return userRepository.findByOpenid(openid)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

    /**
     * 更新用户状态。
     */
    @Transactional
    public void updateStatus(Long userId, UserStatus status) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        user.setStatus(status);
        userRepository.save(user);
        log.info("User {} status updated to {}", userId, status);
    }

    /**
     * 获取用户逾期记录分页。
     */
    @Transactional(readOnly = true)
    public Page<OverdueRecord> getOverdueHistory(Long userId, Pageable pageable) {
        return overdueRecordRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }

    /**
     * 验证密码。
     */
    public boolean validatePassword(String raw, String encoded) {
        return passwordEncoder.matches(raw, encoded);
    }
}