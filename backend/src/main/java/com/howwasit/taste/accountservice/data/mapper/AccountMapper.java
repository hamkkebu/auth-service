package com.howwasit.taste.accountservice.data.mapper;

import com.howwasit.taste.accountservice.data.entity.Account;
import com.howwasit.taste.accountservice.data.dto.RequestAccount;
import com.howwasit.taste.accountservice.data.dto.ResponseAccount;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AccountMapper {

    Account toEntity(RequestAccount dto);

    ResponseAccount toDto(Account entity);
}
