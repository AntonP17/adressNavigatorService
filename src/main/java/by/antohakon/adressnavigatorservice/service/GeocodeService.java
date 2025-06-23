package by.antohakon.adressnavigatorservice.service;

import by.antohakon.adressnavigatorservice.dto.*;
import by.antohakon.adressnavigatorservice.entity.AddressDistantionEntity;
import by.antohakon.adressnavigatorservice.mapper.AddressNavigationMapper;
import by.antohakon.adressnavigatorservice.repository.AddressNavigationRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class GeocodeService {

    @Value("${yandex.api.key}")
    private String yandexApiKey;
    @Value("${yandex.api.url}")
    private String yandexApiURL;

    @Value("${dadata.api.key}")
    private String dadataApiKey;
    @Value("${dadata.secret.key}")
    private String dadataSecretKey;
    @Value("${dadata.api.url}")
    private String dadataApiURL;

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final AddressNavigationRepository addressNavigationRepository;
    private final AddressNavigationMapper addressNavigationMapper;

    public Page<AddressNavigationResponseDto> getAllAddresses(Pageable pageable) {

        return addressNavigationRepository.findAll(pageable)
                .map(GeocodeService::addressNavigationResponseDtoBuild);
    }

    private static AddressNavigationResponseDto addressNavigationResponseDtoBuild(AddressDistantionEntity adress) {
        return AddressNavigationResponseDto.builder()
                .id(adress.getId())
                .address(adress.getAddress())
                .distantion(adress.getDistantion())
                .build();
    }


    public AddressNavigationResponseDto processAddress(RequestAddressDto addressDto) throws IOException, InterruptedException {

        DaDataApiResponse daDataApiResponse = findAddressFromDaDataApi(addressDto.address());

        Optional<AddressDistantionEntity> existingEntity = addressNavigationRepository
                .findByAddress(daDataApiResponse.getFormattedAddress());

        if (existingEntity.isPresent()) {
            log.info("Запись найдена в БД: {}", existingEntity.get());
            return addressNavigationMapper.toDto(existingEntity.get());
        }

        YandexApiResponse yandexApiResponse = findAddressFromYandexApi(addressDto.address());

        double distance = getDistance(daDataApiResponse.getCoordinates(), yandexApiResponse.getCoordinates());

        AddressDistantionEntity entity = new AddressDistantionEntity();
        entity.setAddress(daDataApiResponse.getFormattedAddress());
        entity.setDistantion(distance);

        addressNavigationRepository.save(entity);

        return addressNavigationMapper.toDto(entity);


    }


    private DaDataApiResponse findAddressFromDaDataApi(String address) throws IOException, InterruptedException {

        log.info("зашли в метод cleanAddressViaDaData");
        String requestBody = "[\"" + address + "\"]";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(dadataApiURL))
                .header("Authorization", "Token " + dadataApiKey)
                .header("X-Secret", dadataSecretKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        String json = response.body();
        log.info("ответ = {}", json);

        List<DaDataApiResponse> responses = objectMapper.readValue(json,
                objectMapper.getTypeFactory().constructCollectionType(List.class, DaDataApiResponse.class));

        if (!responses.isEmpty()) {
            DaDataApiResponse daDataApiResponse = responses.get(0);
            log.info("ответ = {} {}", daDataApiResponse.getFormattedAddress(), daDataApiResponse.getCoordinates());
            return daDataApiResponse;
        } else {
            log.error("Получен пустой ответ или ответ не содержит ожидаемых данных");
            return null;
        }
    }


    private YandexApiResponse findAddressFromYandexApi(String address) throws IOException, InterruptedException {

        log.info("зашли в метод fetchCoordinatesViaYandex");
        String url = String.format(
                yandexApiURL,
                yandexApiKey,
                URLEncoder.encode(address, StandardCharsets.UTF_8)
        );

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(
                request, HttpResponse.BodyHandlers.ofString()
        );

        String json = response.body();
        log.info("ответ = {}", json);

        YandexApiResponse yandexGeocodeResponse = objectMapper.readValue(json, YandexApiResponse.class);
        log.info("парсинг = {} {}", yandexGeocodeResponse.getFormattedAddress(), yandexGeocodeResponse.getCoordinates());
        return yandexGeocodeResponse;
    }


    private double getDistance(String startCoords, String endCoords) {

        double[] point1 = parseCoordinates(startCoords);
        double[] point2 = parseCoordinates(endCoords);

        final double R = 6371e3;

        double startLatRad = Math.toRadians(point1[0]);
        double endLatRad = Math.toRadians(point2[0]);
        double deltaLatRad = Math.toRadians(point2[0] - point1[0]);
        double deltaLongRad = Math.toRadians(point2[1] - point1[1]);

        // Формула Гаверсинуса
        double a = Math.sin(deltaLatRad / 2) * Math.sin(deltaLatRad / 2) +
                Math.cos(startLatRad) * Math.cos(endLatRad) *
                        Math.sin(deltaLongRad / 2) * Math.sin(deltaLongRad / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        double distance = Math.round((R * c) * 100) / 100.0;
        log.info("distantion = {} metres", distance);

        return distance;

    }

    private static double[] parseCoordinates(String coord) {
        String[] parts = coord.split(",");
        return new double[]{
                Double.parseDouble(parts[0].trim()), // Широта
                Double.parseDouble(parts[1].trim())  // Долгота
        };
    }
}
