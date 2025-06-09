package by.antohakon.adressnavigatorservice.service;

import by.antohakon.adressnavigatorservice.dto.*;
import by.antohakon.adressnavigatorservice.entity.AdressDistantionEntity;
import by.antohakon.adressnavigatorservice.mapper.AdressNavigationMapper;
import by.antohakon.adressnavigatorservice.repository.AdressNavigationRepository;
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
    private final AdressNavigationRepository adressNavigationRepository;
    private final AdressNavigationMapper adressNavigationMapper;




    public Page<AdressNavigationResponseDto> getAllAdresses(Pageable pageable) {

        return adressNavigationRepository.findAll(pageable)
                .map(GeocodeService::addressNavigationResponseDtoBuild);
    }

    private static AdressNavigationResponseDto addressNavigationResponseDtoBuild(AdressDistantionEntity adress) {
        return AdressNavigationResponseDto.builder()
                .id(adress.getId())
                .address(adress.getAddress())
                .distantion(adress.getDistantion())
                .build();
    }

    // пусть основной метод
    public AdressNavigationResponseDto processAddress(requestAdressDto addressDto) throws IOException, InterruptedException {

          DaDataApiResponse daDataApiResponse = cleanAddressViaDaData(addressDto.address()); //достали из DadataAPI адрес и координаты

        //2. Проверка существования записи в БД
        Optional<AdressDistantionEntity> existingEntity = adressNavigationRepository
                .findByAddress(daDataApiResponse.getFormattedAddress());

        if (existingEntity.isPresent()) {
            // Если запись существует - возвращаем её
            log.info("Запись найдена в БД: {}", existingEntity.get());
            return adressNavigationMapper.toDto(existingEntity.get());
        }

          YandexApiResponse yandexApiResponse = fetchCoordinatesViaYandex(addressDto.address()); // достаом из YAndexAPI адрес и координаты

          double distance = getDistance(daDataApiResponse.getCoordinates(), yandexApiResponse.getCoordinates());

        AdressDistantionEntity entity = new AdressDistantionEntity();
        entity.setAddress(daDataApiResponse.getFormattedAddress());
        entity.setDistantion(distance);

        adressNavigationRepository.save(entity);

        return adressNavigationMapper.toDto(entity);


    }

    // парсинг адреса из вакханалии в номлаьный
    private DaDataApiResponse cleanAddressViaDaData(String address) throws IOException, InterruptedException {

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

        // Парсим ответ DaData (пример: [{"result":"г Москва, ул Тверская, д 7"}])
        String json = response.body();
        log.info("ответ = {}", json);

        // Десериализация JSON-массива в список объектов
        List<DaDataApiResponse> responses = objectMapper.readValue(json,
                objectMapper.getTypeFactory().constructCollectionType(List.class, DaDataApiResponse.class));

        // Обработка первого элемента массива
        if (!responses.isEmpty()) {
            DaDataApiResponse daDataApiResponse = responses.get(0);
            log.info("ответ = {} {}", daDataApiResponse.getFormattedAddress(), daDataApiResponse.getCoordinates());
            return daDataApiResponse;
        } else {
            log.error("Получен пустой ответ или ответ не содержит ожидаемых данных");
            return null;
        }
    }

    //поиск координат яндек кратами
    private YandexApiResponse fetchCoordinatesViaYandex(String address) throws IOException, InterruptedException {

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

        // Парсим ответ Yandex (пример: "pos":"37.6056 55.7602")
        String json = response.body();
        log.info("ответ = {}", json);

        YandexApiResponse yandexGeocodeResponse = objectMapper.readValue(json, YandexApiResponse.class);
        log.info("парсинг = {} {}", yandexGeocodeResponse.getFormattedAddress(), yandexGeocodeResponse.getCoordinates());
        return yandexGeocodeResponse;
    }


    //подсчет расстояния
    public double getDistance(String startCoords, String endCoords) {

        // Парсим координаты
        double[] point1 = parseCoordinates(startCoords);
        double[] point2 = parseCoordinates(endCoords);

        // Радиус Земли в метрах
        final double R = 6371e3;

        // Переводим градусы в радианы
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
        try {
            String[] parts = coord.split(",");
            if (parts.length != 2) {
                throw new IllegalArgumentException("Неверный формат координат. Ожидается 'широта,долгота'");
            }
            return new double[]{
                    Double.parseDouble(parts[0].trim()), // Широта
                    Double.parseDouble(parts[1].trim())  // Долгота
            };
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Координаты должны быть числами", e);
        }
    }
}
