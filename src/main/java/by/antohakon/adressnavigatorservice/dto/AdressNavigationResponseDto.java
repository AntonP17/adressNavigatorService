package by.antohakon.adressnavigatorservice.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdressNavigationResponseDto {

    private Long id;
    private String firstAdress;
    private String secondAdress;
    private double distantion;

}
