package com.terragrid.mapper;

import com.terragrid.dto.UserCreateRequest;
import com.terragrid.dto.UserResponse;
import com.terragrid.model.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public User toEntity(UserCreateRequest request) {
        return User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .role(request.getRole())
                .build();
    }

    public UserResponse toResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .isActive(user.getIsActive())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}