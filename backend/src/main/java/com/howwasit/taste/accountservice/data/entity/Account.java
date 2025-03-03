package com.howwasit.taste.accountservice.data.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "tbl_account_taste")
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "taste_account_num", nullable = false)
    private Long userNum;

    @Column(name = "taste_account_id", nullable = false)
    private String userId;

    @Column(name = "taste_account_fname")
    private String userFname;

    @Column(name = "taste_account_lname")
    private String userLname;

    @Column(name = "taste_account_nickname")
    private String userNickname;

    @Column(name = "taste_account_country")
    private String userCountry;

    @Column(name = "taste_account_city")
    private String userCity;

    @Column(name = "taste_account_state")
    private String userState;

    @Column(name = "taste_account_street1")
    private String userStreet1;

    @Column(name = "taste_account_street2")
    private String userStreet2;

    @Column(name = "taste_account_zip")
    private String userZip;

    @Column(name = "taste_account_email")
    private String userEmail;

    @Column(name = "taste_account_phone")
    private String userPhone;

    @Column(name = "taste_account_id_created", nullable = false)
    private LocalDateTime userIdCreated;

    @Column(name = "taste_account_id_updated", nullable = false)
    private LocalDateTime userIdUpdated;
}
