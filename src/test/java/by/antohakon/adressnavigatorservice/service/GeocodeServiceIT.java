package by.antohakon.adressnavigatorservice.service;

import by.antohakon.adressnavigatorservice.dto.RequestAddressDto;
import by.antohakon.adressnavigatorservice.entity.AddressDistantionEntity;
import by.antohakon.adressnavigatorservice.repository.AddressNavigationRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.net.http.HttpClient;

@Testcontainers
@SpringBootTest
public class GeocodeServiceIT {


    // почитать про эти тест контейнеры подробней
    // почему не подтягивается экземпляр репозитория

    @Autowired
    private GeocodeService geocodeService;
    @Autowired
    private AddressNavigationRepository addressNavigationRepository;

    @MockitoBean
    private HttpClient httpClient;
    @MockitoBean
    private ObjectMapper objectMapper;

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

        String jsonResponse = """
                [{
                    "result": "Спб, Олеко Дундича 5",
                    "geo_lat": "60.0",
                    "geo_lon": "30.0",
                    "qc_geo": "0"
                }]
                """;

    //    addressNavigationRepository.save(addressDistantionEntity);
//
//        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
//                .thenReturn(jsonResponse);

    }

}
