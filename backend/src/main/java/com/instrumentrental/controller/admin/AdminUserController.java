package com.instrumentrental.controller.admin;

import com.instrumentrental.domain.enums.UserStatus;
import com.instrumentrental.domain.model.User;
import com.instrumentrental.domain.repository.UserRepository;
import com.instrumentrental.dto.ApiResponse;
import com.instrumentrental.dto.PageResponse;
import com.instrumentrental.dto.user.UserDTO;
import com.instrumentrental.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class AdminUserController {

    private final UserRepository userRepository;
    private final UserService userService;

    @GetMapping
    public ApiResponse<PageResponse<UserDTO>> getUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<User> userPage = userRepository.findAll(PageRequest.of(page, size));
        List<UserDTO> dtos = userPage.getContent().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());

        PageResponse<UserDTO> response = PageResponse.<UserDTO>builder()
                .content(dtos)
                .totalPages(userPage.getTotalPages())
                .totalElements(userPage.getTotalElements())
                .page(userPage.getNumber())
                .size(userPage.getSize())
                .build();

        return ApiResponse.success(response);
    }

    @PutMapping("/{id}/status")
    public ApiResponse<Void> updateStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        userService.updateStatus(id, UserStatus.valueOf(status));
        return ApiResponse.success(null);
    }

    private UserDTO toDTO(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .phone(user.getPhone())
                .nickname(user.getNickname())
                .role(user.getRole().name())
                .status(user.getStatus().name())
                .createdAt(user.getCreatedAt())
                .build();
    }
}