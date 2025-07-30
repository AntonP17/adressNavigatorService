package by.antohakon.adressnavigatorservice.service;

import by.antohakon.adressnavigatorservice.dto.AddressNavigationResponseDto;
import by.antohakon.adressnavigatorservice.dto.DaDataApiResponse;
import by.antohakon.adressnavigatorservice.dto.YandexApiResponse;
import by.antohakon.adressnavigatorservice.dto.RequestAddressDto;
import by.antohakon.adressnavigatorservice.entity.AddressDistantionEntity;
import by.antohakon.adressnavigatorservice.mapper.AddressNavigationMapper;
import by.antohakon.adressnavigatorservice.repository.AddressNavigationRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.internal.matchers.Null;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpStatusCodeException;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class GeocodeServiceTest {

    @Mock
    private HttpClient httpClient;
    @Mock
    private ObjectMapper objectMapper;
    private ObjectMapper realObjectMapper = new ObjectMapper();
    @Mock
    private AddressNavigationRepository addressNavigationRepository;
    @Mock
    private AddressNavigationMapper addressNavigationMapper;
    @Mock
    private YandexApiResponse yandexApiResponse;

    @InjectMocks
    private GeocodeService geocodeService;

    private final String testDadataUrl = "https://cleaner.dadata.ru/api/v1/clean/address";
    private final String testYandexUrl = "https://geocode-maps.yandex.ru/1.1/";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(geocodeService, "dadataApiURL", "https://test.dadata.url");
        ReflectionTestUtils.setField(geocodeService, "dadataApiKey", "test-api-key");
        ReflectionTestUtils.setField(geocodeService, "dadataSecretKey", "test-secret-key");
        ReflectionTestUtils.setField(geocodeService, "yandexApiURL", testYandexUrl);
        ReflectionTestUtils.setField(geocodeService, "yandexApiKey", testDadataUrl);
    }


    @SneakyThrows
    @Test
    @DisplayName("возврат из БД")
    void processAddress_Positive1() {

        RequestAddressDto requestAddressDto = new RequestAddressDto("Спб, Олеко Дундича 5");

        AddressDistantionEntity addressDistantionEntity = AddressDistantionEntity
                .builder()
                .id(1L)
                .address(requestAddressDto.address())
                .distantion(2.2)
                .build();

        when(addressNavigationRepository.findByAddress(requestAddressDto.address()))
                .thenReturn(Optional.of(addressDistantionEntity));

        daDataResponseMock(requestAddressDto.address(), "60.0", "30.0");

        when(addressNavigationMapper.toDto(addressDistantionEntity))
                .thenReturn(
                        AddressNavigationResponseDto.builder()
                                .id(addressDistantionEntity.getId())
                                .address(addressDistantionEntity.getAddress())
                                .distantion(addressDistantionEntity.getDistantion())
                                .build()
                );

        AddressNavigationResponseDto response = geocodeService.processAddress(requestAddressDto);

        assertNotNull(response);
        assertEquals(requestAddressDto.address(), response.getAddress());
        assertEquals(addressDistantionEntity.getDistantion(), response.getDistantion());

    }

    @SneakyThrows
    @Test
    @DisplayName("В АПИ")
    void processAddress_Positive2() {

        RequestAddressDto requestAddressDto = new RequestAddressDto("Спб, Олеко Дундича 5");

        when(addressNavigationRepository.findByAddress(anyString()))
                .thenReturn(Optional.empty());

        daDataResponseMock(requestAddressDto.address(), "60.0", "30.0");
        yandexResponseMock(requestAddressDto.address(), "170.0", "302.0");

        when(addressNavigationRepository.save(any()))
                .thenAnswer(invocation -> {
                    AddressDistantionEntity e = invocation.getArgument(0);
                    return AddressDistantionEntity.builder()
                            .id(1L)
                            .address(e.getAddress())
                            .distantion(e.getDistantion())
                            .build();
                });

        when(addressNavigationMapper.toDto(any(AddressDistantionEntity.class)))
                .thenAnswer(invocation -> {
                    AddressDistantionEntity e = invocation.getArgument(0);
                    return AddressNavigationResponseDto.builder()
                            .id(e.getId())
                            .address(e.getAddress())
                            .distantion(e.getDistantion())
                            .build();
                });

        AddressNavigationResponseDto response = geocodeService.processAddress(requestAddressDto);

        assertNotNull(response);
        assertEquals(requestAddressDto.address(), response.getAddress());
        // расчитать вручную координаты которые возвращает response и сравнить (P.s использовать класс Math) в телеге пример
        // (Math.abs(polyLen - segSum) < 1e-6) погуглить если что посмотреть

    }

    @SneakyThrows
    @Test
    @DisplayName("Dadata не отвечает")
    void processAddress_Negative1() {

        RequestAddressDto requestAddressDto = new RequestAddressDto("Спб, Олеко Дундича 5");

        when(addressNavigationRepository.findByAddress(anyString()))
                .thenReturn(Optional.empty());

        when(httpClient.send(any(HttpRequest.class) , any()))
                .thenThrow(new IOException("DaData недоступна"));

        assertThrows(IOException.class, () -> {
            geocodeService.processAddress(requestAddressDto);
        });
    }

    @SneakyThrows
    @Test
    @DisplayName("YandexApi не овтечает")
    void processAdress_Negative2() {

        RequestAddressDto requestAddressDto = new RequestAddressDto("Спб, Олеко Дундича 5");

        when(addressNavigationRepository.findByAddress(anyString()))
                .thenReturn(Optional.empty());

        daDataResponseMock(requestAddressDto.address(), "60.0", "30.0");

        when(httpClient.send(any(HttpRequest.class) , any()))
                .thenReturn(mock(HttpResponse.class))
                .thenThrow(new NullPointerException("Yandex Api недоступно"));

        assertThrows(NullPointerException.class, () -> {
            geocodeService.processAddress(requestAddressDto);
        });

    }

    @SneakyThrows
    @Test
    @DisplayName("ключ апи прокис")
    void processAdress_Negative3() {

        // пытался сделать нормлаьные загловки и тело ответа, но получилась фигня

        RequestAddressDto requestAddressDto = new RequestAddressDto("Спб, Олеко Дундича 5");

        when(addressNavigationRepository.findByAddress(anyString()))
                .thenReturn(Optional.empty());

        HttpClientErrorException exception = HttpClientErrorException.create(
                HttpStatus.FORBIDDEN,
                "Forbidden",
                null,
                null,
                null
        );

        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenThrow(exception);

        // 4. Проверка
        assertThrows(HttpClientErrorException.class,
                () -> geocodeService.processAddress(requestAddressDto));

    }

    @SneakyThrows
    private void yandexResponseMock(String address, String lat, String lon) {

        HttpResponse<String> response = Mockito.mock(HttpResponse.class);
        String jsonResponse = String.format("""
                {
                    "response": {
                        "GeoObjectCollection": {
                            "featureMember": [
                                {
                                    "GeoObject": {
                                        "Point": {
                                            "pos": "%s %s"
                                        },
                                        "metaDataProperty": {
                                            "GeocoderMetaData": {
                                                "text": "%s",
                                                "Address": {
                                                    "formatted": "%s"
                                                }
                                            }
                                        }
                                    }
                                }
                            ]
                        }
                    }
                }
                """, lon, lat, address, address);

        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(response);
        when(response.body()).thenReturn(jsonResponse);
        YandexApiResponse yandexApiResponse1 = realObjectMapper.readValue(jsonResponse, YandexApiResponse.class);

        when(objectMapper.readValue(anyString(), eq(YandexApiResponse.class)))
                .thenReturn(yandexApiResponse1);

    }

    @SneakyThrows
    private void daDataResponseMock(String address, String lat, String lon) {

        HttpResponse<String> response = Mockito.mock(HttpResponse.class);
        String jsonResponse = String.format("""
                [{
                    "result": "%s",
                    "geo_lat": "%s",
                    "geo_lon": "%s",
                    "qc_geo": "0"
                }]
                """, address, lat, lon);

        when(response.body()).thenReturn(jsonResponse);

        DaDataApiResponse daDataResponse = new DaDataApiResponse();
        daDataResponse.setFormattedAddress(address);
        daDataResponse.setLatitude(lat);
        daDataResponse.setLongitude(lon);

        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(response);
        when(objectMapper.readValue(anyString(), ArgumentMatchers.<TypeReference<List<DaDataApiResponse>>>any()))
                .thenReturn(List.of(daDataResponse));


    }
}