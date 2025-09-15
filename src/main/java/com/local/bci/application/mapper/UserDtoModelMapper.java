package com.local.bci.application.mapper;

import com.local.bci.application.dto.PhoneDTO;
import com.local.bci.application.dto.SignUpRequestDTO;
import com.local.bci.application.dto.UserResponseDTO;
import com.local.bci.domain.model.PhoneModel;
import com.local.bci.domain.model.UserModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.springframework.stereotype.Component;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
@Component
public interface UserDtoModelMapper {

    @Mapping(target = "phones", source = "phones")
    UserModel dtoToModel(SignUpRequestDTO dto);

    SignUpRequestDTO modelToDto(UserModel model);

    UserResponseDTO modelToResponseDto(UserModel model);

    PhoneModel phoneDtoToModel(PhoneDTO dto);

    PhoneDTO phoneModelToDto(PhoneModel model);

}
