package com.hamkkebu.authservice.data.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 비밀번호 변경 요청 DTO
 *
 * <p>현재 비밀번호 확인 후 새 비밀번호로 변경합니다.</p>
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PasswordChangeRequest {

    @NotBlank(message = "현재 비밀번호는 필수입니다")
    private String currentPassword;

    @NotBlank(message = "새 비밀번호는 필수입니다")
    @Size(min = 8, max = 100, message = "새 비밀번호는 8-100자 사이여야 합니다")
    private String newPassword;

    @NotBlank(message = "새 비밀번호 확인은 필수입니다")
    private String confirmPassword;
}
