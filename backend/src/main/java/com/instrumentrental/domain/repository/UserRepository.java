package com.instrumentrental.domain.repository;

import com.instrumentrental.domain.enums.UserStatus;
import com.instrumentrental.domain.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByOpenid(String openid);

    Optional<User> findByPhone(String phone);

    Page<User> findByStatus(UserStatus status, Pageable pageable);

    Page<User> findByPhoneContainingOrNicknameContaining(String phone, String nickname, Pageable pageable);
}