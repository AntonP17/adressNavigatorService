package by.antohakon.adressnavigatorservice.service;

import by.antohakon.adressnavigatorservice.dto.AddressNavigationResponseDto;
import by.antohakon.adressnavigatorservice.dto.DaDataApiResponse;
import by.antohakon.adressnavigatorservice.dto.RequestAddressDto;
import by.antohakon.adressnavigatorservice.entity.AddressDistantionEntity;
import by.antohakon.adressnavigatorservice.mapper.AddressNavigationMapper;
import by.antohakon.adressnavigatorservice.repository.AddressNavigationRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest
@Testcontainers
public class GeocodeServiceIT {

    // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    // почитать про эти тест контейнеры подробней
    // почему не подтягивается экземпляр репозитория
    // другую либру
    // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!

    @Autowired
    private GeocodeService geocodeService;
    @Autowired
    private AddressNavigationRepository addressNavigationRepository;

    @MockitoBean
    private HttpClient httpClient;
    @MockitoBean
    private ObjectMapper objectMapper;
    @MockitoBean
    private AddressNavigationMapper addressNavigationMapper;


    @Container
    static PostgreSQLContainer<?> postgreSQLContainer =
            new PostgreSQLContainer<>("postgres:13")
                    .withDatabaseName("TestDB")
                    .withUsername("postgres")
                    .withPassword("123456");

    @DynamicPropertySource
    static void overrideProperty(DynamicPropertyRegistry registry) {

        registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
        registry.add("spring.datasource.password", postgreSQLContainer::getPassword);
        registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "update");

    }

    @Test
    @SneakyThrows
    public void processAddress_Positive1() {

        RequestAddressDto requestAddressDto = new RequestAddressDto("Спб, Олеко Дундича 5");

        AddressDistantionEntity addressDistantionEntity = AddressDistantionEntity
                .builder()
                .id(1L)
                .address(requestAddressDto.address())
                .distantion(2.2)
                .build();

        addressNavigationRepository.findByAddress(requestAddressDto.address());

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

        Assertions.assertNotNull(response);
        Assertions.assertEquals(requestAddressDto.address(), response.getAddress());
        Assertions.assertEquals(addressDistantionEntity.getDistantion(), response.getDistantion());

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
