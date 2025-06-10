package by.antohakon.adressnavigatorservice.service;

import by.antohakon.adressnavigatorservice.dto.*;
import by.antohakon.adressnavigatorservice.entity.AddressDistantionEntity;
import by.antohakon.adressnavigatorservice.repository.AddressNavigationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class GeocodeServiceTest {

    @Mock
    private AddressNavigationRepository addressNavigationRepository;

    @Mock
    private HttpClient httpClient;

    @Mock
    private HttpResponse<String> httpResponse;

    @InjectMocks
    private GeocodeService geocodeService;

    @Test
    void whenValidAddress_thenProcessSuccessfully() throws Exception {

        requestAddressDto request = new requestAddressDto("ул. Тверская, 7");

        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(httpResponse);

        when(httpResponse.body())
                .thenReturn("[{\"result\":\"г Москва, ул Тверская, д 7\", \"lat\":55.7602, \"lon\":37.6056}]") // DaData
                .thenReturn("{\"response\":{\"GeoObjectCollection\":{\"featureMember\":[{\"GeoObject\":{\"Point\":{\"pos\":\"55.7602 37.6056\"}}}]}}}"); // Yandex

        when(addressNavigationRepository.findByAddress(any()))
                .thenReturn(Optional.empty());

        AddressNavigationResponseDto response = geocodeService.processAddress(request);

        assertThat(response).isNotNull();
        assertThat(response.getAddress()).contains("Тверская");
        assertThat(response.getDistantion()).isGreaterThan(0);

        verify(addressNavigationRepository, times(1)).save(any());
    }

    @Test
    void whenAddressExistsInDb_thenReturnExisting() throws Exception {

        requestAddressDto request = new requestAddressDto("ул. Тверская, 7");

        AddressDistantionEntity existingEntity = new AddressDistantionEntity();
        existingEntity.setAddress("г Москва, ул Тверская, д 7");
        existingEntity.setDistantion(1000.0);

        when(addressNavigationRepository.findByAddress(any()))
                .thenReturn(Optional.of(existingEntity));

        AddressNavigationResponseDto response = geocodeService.processAddress(request);

        assertThat(response.getAddress()).isEqualTo(existingEntity.getAddress());
        assertThat(response.getDistantion()).isEqualTo(existingEntity.getDistantion());

        verify(addressNavigationRepository, never()).save(any());
    }

    @Test
    void whenDaDataRequestFails_thenThrowException() throws Exception {

        when(httpClient.send(any(), any()))
                .thenThrow(new IOException("Ошибка сети"));

        assertThatThrownBy(() -> geocodeService.processAddress(new requestAddressDto("ул. Тверская")))
                .isInstanceOf(IOException.class);
    }



}
