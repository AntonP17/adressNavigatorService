package by.antohakon.adressnavigatorservice.mapper;

import by.antohakon.adressnavigatorservice.dto.AdressNavigationResponseDto;
import by.antohakon.adressnavigatorservice.dto.responseAdressDto;
import by.antohakon.adressnavigatorservice.entity.AdressDistantionEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AdressNavigationMapper {

  AdressNavigationResponseDto toDto(AdressDistantionEntity adressDistantionEntity);
  AdressDistantionEntity toEntity(AdressNavigationResponseDto adressNavigationResponseDto);
}
