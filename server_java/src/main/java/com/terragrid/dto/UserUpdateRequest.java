package com.terragrid.dto;

import com.terragrid.model.Role;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UserUpdateRequest {
    private Role role;
    
    @NotNull(message = "isActive status cannot be null")
    private Boolean isActive;
}