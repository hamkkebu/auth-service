package com.hamkkebu.authservice.data.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 로그인 응답 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

    private String username;
    private String firstName;
    private String lastName;
    private String nickname;
    private String email;
    private TokenResponse token;

    public static LoginResponse of(UserResponse user, TokenResponse token) {
        return LoginResponse.builder()
            .username(user.getUsername())
            .firstName(user.getFirstName())
            .lastName(user.getLastName())
            .nickname(null)  // User entity doesn't have nickname field
            .email(user.getEmail())
            .token(token)
            .build();
    }
}
