package com.howwasit.taste.accountservice.data.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResponseAccount {

    private Long userNum;

    private String userId;

    private String userFname;

    private String userLname;

    private String userNickname;

    private String userCountry;

    private String userCity;

    private String userState;

    private String userStreet1;

    private String userStreet2;

    private String userZip;

    private String userEmail;

    private String userPhone;

    private LocalDateTime userIdCreated;

    private LocalDateTime userIdUpdated;
}
