package by.antohakon.adressnavigatorservice.service;

import by.antohakon.adressnavigatorservice.dto.*;
import by.antohakon.adressnavigatorservice.entity.AddressDistantionEntity;
import by.antohakon.adressnavigatorservice.mapper.AddressNavigationMapper;
import by.antohakon.adressnavigatorservice.repository.AddressNavigationRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class GeocodeServiceTest {

    @Mock
    private HttpClient httpClient;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private AddressNavigationRepository addressNavigationRepository;

    @Mock
    private AddressNavigationMapper addressNavigationMapper;

    @InjectMocks
    private GeocodeService geocodeService;

    private requestAddressDto requestAddressDto;
    private DaDataApiResponse daDataApiResponse;
    private YandexApiResponse yandexApiResponse;
    private AddressDistantionEntity addressDistantionEntity;
    private AddressNavigationResponseDto addressNavigationResponseDto;

    @BeforeEach
    void setUp() {
        requestAddressDto = new requestAddressDto("Test Address");
        daDataApiResponse = new DaDataApiResponse();
        yandexApiResponse = new YandexApiResponse();
        addressDistantionEntity = new AddressDistantionEntity();
        addressDistantionEntity.setId(1L);
        addressDistantionEntity.setAddress("Formatted Address");
        addressDistantionEntity.setDistantion(100.0);
        addressNavigationResponseDto = new AddressNavigationResponseDto(1L, "Formatted Address", 100.0);
    }

    @Test
    void testGetAllAddresses() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<AddressDistantionEntity> page = new PageImpl<>(List.of(addressDistantionEntity), pageable, 1);
        when(addressNavigationRepository.findAll(pageable)).thenReturn(page);
        when(addressNavigationMapper.toDto(any(AddressDistantionEntity.class))).thenReturn(addressNavigationResponseDto);

        Page<AddressNavigationResponseDto> result = geocodeService.getAllAddresses(pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void testProcessAddress_ExistingEntity() throws IOException, InterruptedException {
        when(addressNavigationRepository.findByAddress(anyString())).thenReturn(Optional.of(addressDistantionEntity));
        when(addressNavigationMapper.toDto(any(AddressDistantionEntity.class))).thenReturn(addressNavigationResponseDto);

        AddressNavigationResponseDto result = geocodeService.processAddress(requestAddressDto);

        assertNotNull(result);
        assertEquals(addressNavigationResponseDto.getId(), result.getId());
    }

    @Test
    void testProcessAddress_NewEntity() throws IOException, InterruptedException {
        when(addressNavigationRepository.findByAddress(anyString())).thenReturn(Optional.empty());
        when(addressNavigationRepository.save(any(AddressDistantionEntity.class))).thenReturn(addressDistantionEntity);
        when(addressNavigationMapper.toDto(any(AddressDistantionEntity.class))).thenReturn(addressNavigationResponseDto);

        AddressNavigationResponseDto result = geocodeService.processAddress(requestAddressDto);

        assertNotNull(result);
        assertEquals(addressNavigationResponseDto.getId(), result.getId());
    }

    @Test
    void testFindAddressFromDaDataApi() throws IOException, InterruptedException {
        HttpResponse mockResponse = mock(HttpResponse.class);
        when(httpClient.send(any(), any())).thenReturn(mockResponse);
        when(mockResponse.body()).thenReturn("[{\"result\":\"Formatted Address\", \"geo_lat\":\"10.0\", \"geo_lon\":\"20.0\"}]");
        when(objectMapper.readValue(anyString(), (Class<Object>) any())).thenReturn(List.of(daDataApiResponse));

        DaDataApiResponse result = geocodeService.findAddressFromDaDataApi("Test Address");

        assertNotNull(result);
        assertEquals("Formatted Address", result.getFormattedAddress());
    }

    @Test
    void testFindAddressFromYandexApi() throws IOException, InterruptedException {
        HttpResponse mockResponse = mock(HttpResponse.class);
        when(httpClient.send(any(), any())).thenReturn(mockResponse);
        when(mockResponse.body()).thenReturn("{\"pos\":\"10.0,20.0\"}");
        when(objectMapper.readValue(anyString(), eq(YandexApiResponse.class))).thenReturn(yandexApiResponse);

        YandexApiResponse result = geocodeService.findAddressFromYandexApi("Test Address");

        assertNotNull(result);
        assertEquals("10.0,20.0", result.getCoordinates());
    }

    @Test
    void testGetDistance() {
        double distance = geocodeService.getDistance("10.0,20.0", "10.0,20.0");
        assertEquals(0.0, distance);
    }

}
