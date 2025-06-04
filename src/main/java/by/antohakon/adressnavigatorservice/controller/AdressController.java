package by.antohakon.adressnavigatorservice.controller;

import by.antohakon.adressnavigatorservice.dto.AdressDto;
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
    public void processAddress(@RequestBody AdressDto adressDto) {
        geocodeService.processAddress(adressDto);
    }


}
