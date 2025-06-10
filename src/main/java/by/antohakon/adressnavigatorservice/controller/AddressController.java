package by.antohakon.adressnavigatorservice.controller;

import by.antohakon.adressnavigatorservice.dto.AddressNavigationResponseDto;
import by.antohakon.adressnavigatorservice.dto.requestAddressDto;
import by.antohakon.adressnavigatorservice.service.GeocodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;


@RestController
@RequestMapping("api/adress")
@RequiredArgsConstructor
public class AddressController {

    private final GeocodeService geocodeService;

    @PostMapping("/process")
    public AddressNavigationResponseDto processAddress(@RequestBody requestAddressDto addressDto) throws IOException, InterruptedException {
      return geocodeService.processAddress(addressDto);
    }

    @GetMapping("/all")
    public Page<AddressNavigationResponseDto> getAddresses(Pageable pageable) {
        return geocodeService.getAllAddresses(pageable);
    }
}
