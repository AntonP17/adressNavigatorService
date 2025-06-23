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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GeocodeServiceTest {

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

    private final String testDadataUrl = "https://cleaner.dadata.ru/api/v1/clean/address";
    private final String testYandexUrl = "https://geocode-maps.yandex.ru/1.x/";

    @BeforeEach
    void setUp()
    {
        ReflectionTestUtils.setField(geocodeService, "dadataApiURL", "https://test.dadata.url");
        ReflectionTestUtils.setField(geocodeService, "dadataApiKey", "test-api-key");
        ReflectionTestUtils.setField(geocodeService, "dadataSecretKey", "test-secret-key");
    }


        @SneakyThrows
        @Test
        @DisplayName("возврат из БД")
        void processAddress_Positive1() { //из БД возвра

            RequestAddressDto requestAddressDto = new RequestAddressDto("Спб, Олеко Дундича 5");

            when(addressNavigationRepository.findByAddress(requestAddressDto.address()))
                    .thenReturn(Optional.of(AddressDistantionEntity
                            .builder()
                            .address(requestAddressDto.address())
                            .distantion(2.2)
                            .build()));

            daDataResponseMock(requestAddressDto.address(), "60.0", "30.0");

            AddressNavigationResponseDto response = geocodeService.processAddress(requestAddressDto);

            assertNotNull(response);
            assertEquals(requestAddressDto.address(), response.getAddress());

        }

    @SneakyThrows
    @Test
    @DisplayName("В АПИ")
    void processAddress_Positive2() {

        RequestAddressDto requestAddressDto = new RequestAddressDto("Спб, Олеко Дундича 5");

        HttpResponse<String> response = Mockito.mock(HttpResponse.class);

        when(response.body()).thenReturn("json");
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(response);
        when(addressNavigationRepository.findByAddress(requestAddressDto.address()))
                .thenReturn(Optional.empty());

    }

//    @SneakyThrows
//    // аргументы передать (координаты адрес и тд что в запросе к АПИ)
//    private void yandexResponseMock(){
//
//        HttpResponse<String> response = Mockito.mock(HttpResponse.class);
//
//        when(response.body()).thenReturn("РЕАЛЬНЫЙ JSON YANDEX"!!!!!!!!!!!!!!!!); // ДОДЕЛТЬ
//        YandexApiResponse yandexApiResponse = new YandexApiResponse();
//        yandexApiResponse.; // засетить либо создать норм респонс от яндекса
//        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
//                .thenReturn(response);
//        when(objectMapper.readValue(anyString(), eq(YandexApiResponse.class))).thenReturn(yandexApiResponse);
//
//    }
//
    @SneakyThrows
    // аргументы передать (координаты адрес и тд что в запросе к АПИ)
    private void daDataResponseMock(String address, String lat, String lon){

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

//        when(objectMapper.readValue(anyString(), eq(DaDataApiResponse.class))).thenReturn(daDataResponse);
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(response);
        when(objectMapper.readValue(anyString(), any(TypeReference.class)))
                .thenReturn(List.of(daDataResponse));


    }
//
//    // тест негативный , замокать так же но подобрать неожидаемы данные, тест должен сломаться
//    //
//
//
//
//
























//
//
//
//
//    @Test
//    void findAddressFromDaDataApi_ShouldReturnResponse_WhenValidAddress() throws Exception {
//        // Arrange
//        String testAddress = "Москва, Ленина 1";
//        String jsonResponse = "[{\"result\":\"г Москва, ул Ленина, д 1\",\"geo_lat\":\"55.123\",\"geo_lon\":\"37.456\"}]";
//
//        DaDataApiResponse expectedResponse = new DaDataApiResponse();
//        expectedResponse.setFormattedAddress("г Москва, ул Ленина, д 1");
//        expectedResponse.setLatitude("55.123");
//        expectedResponse.setLongitude("37.456");
//
//        // Настройка моков
//        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
//                .thenReturn(httpResponse);
//        when(httpResponse.body()).thenReturn(jsonResponse);
//        when(objectMapper.readValue(eq(jsonResponse), any(JavaType.class)))
//                .thenReturn(List.of(expectedResponse));
//
//        // Act
//        DaDataApiResponse result = geocodeService.findAddressFromDaDataApi(testAddress);
//
//        // Assert
//        assertNotNull(result);
//        assertEquals("г Москва, ул Ленина, д 1", result.getFormattedAddress());
//        assertEquals("37.456,55.123", result.getCoordinates());
//    }
//
//    @Test
//    void findAddressFromYandexApi_ShouldReturnCoordinates_WhenValidAddress() throws Exception {
//        // Arrange
//        String testAddress = "Москва, Красная площадь";
//        String jsonResponse = """
//                {
//                    "response": {
//                        "GeoObjectCollection": {
//                            "featureMember": [
//                                {
//                                    "GeoObject": {
//                                        "metaDataProperty": {
//                                            "GeocoderMetaData": {
//                                                "text": "Россия, Москва, Красная площадь"
//                                            }
//                                        },
//                                        "Point": {
//                                            "pos": "37.617635 55.755814"
//                                        }
//                                    }
//                                }
//                            ]
//                        }
//                    }
//                }""";
//
//        // Настройка моков только для этого теста
//        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
//                .thenReturn(httpResponse);
//        when(httpResponse.body()).thenReturn(jsonResponse);
//
//        // Создаем ожидаемый объект ответа
//        YandexApiResponse expectedResponse = new YandexApiResponse();
//        YandexApiResponse.Response response = new YandexApiResponse.Response();
//        YandexApiResponse.GeoObjectCollection collection = new YandexApiResponse.GeoObjectCollection();
//        YandexApiResponse.FeatureMember member = new YandexApiResponse.FeatureMember();
//        YandexApiResponse.GeoObject geoObject = new YandexApiResponse.GeoObject();
//        YandexApiResponse.Point point = new YandexApiResponse.Point();
//        point.setPosition("37.617635 55.755814");
//        YandexApiResponse.MetaDataProperty metaDataProperty = new YandexApiResponse.MetaDataProperty();
//        YandexApiResponse.GeocoderMetaData geocoderMetaData = new YandexApiResponse.GeocoderMetaData();
//        geocoderMetaData.setFormattedAddress("Россия, Москва, Красная площадь");
//        metaDataProperty.setGeocoderMetaData(geocoderMetaData);
//        geoObject.setPoint(point);
//        geoObject.setMetaDataProperty(metaDataProperty);
//        member.setGeoObject(geoObject);
//        collection.setFeatureMember(List.of(member));
//        response.setGeoObjectCollection(collection);
//        expectedResponse.setResponse(response);
//
//        when(objectMapper.readValue(jsonResponse, YandexApiResponse.class))
//                .thenReturn(expectedResponse);
//
//        // Act
//        YandexApiResponse result = geocodeService.findAddressFromYandexApi(testAddress);
//
//        // Assert
//        assertNotNull(result, "Результат не должен быть null");
//        assertEquals("Россия, Москва, Красная площадь", result.getFormattedAddress());
//        assertEquals("37.617635,55.755814", result.getCoordinates());
//    }
}