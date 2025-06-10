package by.antohakon.adressnavigatorservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class AddressNavigationResponseDto {

    private Long id;
    private String address;
    private double distantion;

}
