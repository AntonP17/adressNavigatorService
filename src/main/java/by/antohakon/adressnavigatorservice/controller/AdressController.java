package by.antohakon.adressnavigatorservice.controller;

import by.antohakon.adressnavigatorservice.dto.requestAdressDto;
import by.antohakon.adressnavigatorservice.dto.responseAdressDto;
import by.antohakon.adressnavigatorservice.service.GeocodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/adress")
@RequiredArgsConstructor
public class AdressController {

    private final GeocodeService geocodeService;

    @PostMapping("/process")
    public responseAdressDto processAddress(@RequestBody requestAdressDto adressDto) {
       return geocodeService.processAddress(adressDto);
    }


}
