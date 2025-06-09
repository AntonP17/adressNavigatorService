package by.antohakon.adressnavigatorservice.controller;

import by.antohakon.adressnavigatorservice.dto.AdressNavigationResponseDto;
import by.antohakon.adressnavigatorservice.dto.requestAdressDto;
import by.antohakon.adressnavigatorservice.service.GeocodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;


@RestController
@RequestMapping("api/adress")
@RequiredArgsConstructor
public class AdressController {

    private final GeocodeService geocodeService;

    @PostMapping("/process")
    public AdressNavigationResponseDto processAddress(@RequestBody requestAdressDto adressDto) throws IOException, InterruptedException {
      return geocodeService.processAddress(adressDto);
    }

    @GetMapping("/all")
    public Page<AdressNavigationResponseDto> getAdresses(Pageable pageable) {
        return geocodeService.getAllAdresses(pageable);
    }
}
