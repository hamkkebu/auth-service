package com.hamkkebu.authservice.data.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 사용자 프로필 수정 요청 DTO
 *
 * <p>username과 password는 수정 대상이 아니므로 포함하지 않습니다.</p>
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileUpdateRequest {

    @NotBlank(message = "이메일은 필수입니다")
    @Size(max = 100, message = "이메일은 100자 이하여야 합니다")
    private String email;

    @Size(max = 50, message = "이름은 50자 이하여야 합니다")
    private String firstName;

    @Size(max = 50, message = "성은 50자 이하여야 합니다")
    private String lastName;

    @Size(max = 50, message = "닉네임은 50자 이하여야 합니다")
    private String nickname;

    @JsonProperty("phone")
    @Size(max = 20, message = "전화번호는 20자 이하여야 합니다")
    private String phoneNumber;

    @Size(max = 50, message = "국가는 50자 이하여야 합니다")
    private String country;

    @Size(max = 50, message = "도시는 50자 이하여야 합니다")
    private String city;

    @Size(max = 50, message = "주/도는 50자 이하여야 합니다")
    private String state;

    @JsonProperty("street1")
    @Size(max = 200, message = "주소는 200자 이하여야 합니다")
    private String streetAddress;

    @JsonProperty("street2")
    @Size(max = 200, message = "추가 주소는 200자 이하여야 합니다")
    private String streetAddress2;

    @JsonProperty("zip")
    @Size(max = 20, message = "우편번호는 20자 이하여야 합니다")
    private String postalCode;
}
