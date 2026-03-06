package com.devpro.devlearningroadmapmanager.securities.mappers;

import com.devpro.devlearningroadmapmanager.securities.dtos.UserDto;
import com.devpro.devlearningroadmapmanager.securities.entities.User;
import org.mapstruct.*;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserMapper {

    User toEntity(UserDto.UserSaveDto userSaveDto);

    UserDto toDto(User user);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void partialUpdate(UserDto.UserSaveDto userSaveDto, @MappingTarget User user);

}