package com.church.churchapp.dto;

import com.church.churchapp.enums.Role;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.util.Set;

@Data
public class UserUpdateRequest {

    @Size(min = 3, max = 20, message = "Username must be between 3 and 20 characters")
    private String username;

    private String password;

    private Set<Role> roles;
}
