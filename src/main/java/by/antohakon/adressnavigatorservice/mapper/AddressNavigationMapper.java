package by.antohakon.adressnavigatorservice.mapper;

import by.antohakon.adressnavigatorservice.dto.AddressNavigationResponseDto;
import by.antohakon.adressnavigatorservice.entity.AddressDistantionEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AddressNavigationMapper {

  AddressNavigationResponseDto toDto(AddressDistantionEntity addressDistantionEntity);
  AddressDistantionEntity toEntity(AddressNavigationResponseDto addressNavigationResponseDto);
}
