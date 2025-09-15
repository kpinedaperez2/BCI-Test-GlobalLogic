package com.local.bci.infrastructure.mapper;

import com.local.bci.domain.model.PhoneModel;
import com.local.bci.domain.model.UserModel;
import com.local.bci.infrastructure.persistence.entity.PhoneEntity;
import com.local.bci.infrastructure.persistence.entity.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.springframework.stereotype.Component;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
@Component
public interface UserEntityModelMapper {

    @Mapping(target = "password", source = "passwordEncrypted")
    UserEntity modelToEntity(UserModel model);

    @Mapping(target = "passwordEncrypted", source = "password")
    UserModel entityToModel(UserEntity entity);

    PhoneEntity phoneModelToEntity(PhoneModel phoneModel);

    PhoneModel phoneEntityToModel(PhoneEntity phoneEntity);
}
