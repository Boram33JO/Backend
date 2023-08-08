package com.sparta.i_mu.mapper;

import com.sparta.i_mu.dto.requestDto.PostSaveRequestDto;
import com.sparta.i_mu.entity.Location;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface LocationMapper {

    LocationMapper LOCATION_INSTANCE = Mappers.getMapper(LocationMapper.class);

    Location dtoToEntity(PostSaveRequestDto postSaveRequestDto);
}
