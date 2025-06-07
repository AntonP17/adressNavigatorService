package by.antohakon.adressnavigatorservice.service;

import by.antohakon.adressnavigatorservice.dto.*;
import by.antohakon.adressnavigatorservice.dto.mapper.AdressNavigationMapper;
import by.antohakon.adressnavigatorservice.entity.AdressDistantionEntity;
import by.antohakon.adressnavigatorservice.exceptions.DuplicateAdressException;
import by.antohakon.adressnavigatorservice.repository.AdressNavigationRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

@Service
@Slf4j
@RequiredArgsConstructor
public class GeocodeService {

    @Value("${yandex.api.key}")
    private String yandexApiKey;

    @Value("${dadata.api.key}")
    private String dadataApiKey;

    @Value("${dadata.secret.key}")
    private String dadataSecretKey;

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final AdressNavigationRepository adressNavigationRepository;
    private final AdressNavigationMapper adressNavigationMapper;

    public Page<AdressNavigationResponseDto> getAllAdresses(Pageable pageable) {

      return adressNavigationRepository.findAll(pageable)
              .map(adress -> AdressNavigationResponseDto.builder()
                      .id(adress.getId())
                      .firstAdress(adress.getFirstAdress())
                      .secondAdress(adress.getSecondAdress())
                      .distantion(adress.getDistantion())
                      .build()
              );
    }

    // пусть основной метод
    public AdressNavigationResponseDto processAddress(requestAdressDto adressDto) throws IOException, InterruptedException {

        String adresStartPoint = adressDto.adressStartPoint();
        String adresEndPoint = adressDto.adressEndPoint();

        // 1. Очистка адреса через DaData
        String cleanedAddressStart = cleanAddressViaDaData(adresStartPoint);
        String cleanedAddressEnd = cleanAddressViaDaData(adresEndPoint);

        // 2. Поиск координат через Yandex
        String coordinatesStart = fetchCoordinatesViaYandex(cleanedAddressStart);
        String coordinatesEnd = fetchCoordinatesViaYandex(cleanedAddressEnd);

        double distantion = getDistance(coordinatesStart,coordinatesEnd);

        if (adressNavigationRepository.existsByFirstAdressAndSecondAdress(cleanedAddressStart, cleanedAddressEnd)) {
          throw new DuplicateAdressException("Такие адреса в БД уже есть = " + cleanedAddressStart + "||" + cleanedAddressEnd);
        }

        AdressDistantionEntity entity = new AdressDistantionEntity();
        entity.setFirstAdress(cleanedAddressStart);
        entity.setSecondAdress(cleanedAddressEnd);
        entity.setDistantion(distantion);

        adressNavigationRepository.save(entity);

//        AdressNavigationResponseDto responseDto = AdressNavigationResponseDto.builder()
//                .id(entity.getId())
//                .firstAdress(entity.getFirstAdress())
//                .secondAdress(entity.getSecondAdress())
//                .distantion(entity.getDistantion())
//                .build();


         return adressNavigationMapper.toDto(entity);
    }

    // парсинг адреса из вакханалии в номлаьный
    private String cleanAddressViaDaData(String dirtyAddress) throws IOException, InterruptedException {

        log.info("зашли в метод cleanAddressViaDaData");
        String url = "https://cleaner.dadata.ru/api/v1/clean/address";
        String requestBody = "[\"" + dirtyAddress + "\"]";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Token " + dadataApiKey)
                .header("X-Secret", dadataSecretKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        // Парсим ответ DaData (пример: [{"result":"г Москва, ул Тверская, д 7"}])
        String json = response.body();
        log.info("ответ = {}", json);


        DaDataCleanResponse[] cleanResponse = objectMapper.readValue(json, DaDataCleanResponse[].class);

        String cleanAdress = cleanResponse[0].getResult();
        log.info("чистый адрес = " + cleanAdress);
        return cleanAdress;

    }

    //поиск координат яндек кратами
    private String fetchCoordinatesViaYandex(String address) throws IOException, InterruptedException {

        log.info("зашли в метод fetchCoordinatesViaYandex");
        String url = "https://geocode-maps.yandex.ru/v1/?apikey=" + yandexApiKey
                + "&geocode=" + URLEncoder.encode(address, StandardCharsets.UTF_8)
                + "&format=json";

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

        YandexGeocodeResponse yandexGeocodeResponse = objectMapper.readValue(json, YandexGeocodeResponse.class);

        String coordinates = yandexGeocodeResponse.getCoordinates().replace(" ", ",");
        log.info("координаты = {}", coordinates);
        return coordinates;

    }


    //подсчет расстояния
    public double getDistance(String startCoords, String endCoords)  {

        // Парсим координаты
        double[] point1 = parseCoordinates(startCoords);
        double[] point2 = parseCoordinates(endCoords);

        // Радиус Земли в метрах
        final double R = 6371e3;

        // Переводим градусы в радианы
        double φ1 = Math.toRadians(point1[0]);
        double φ2 = Math.toRadians(point2[0]);
        double Δφ = Math.toRadians(point2[0] - point1[0]);
        double Δλ = Math.toRadians(point2[1] - point1[1]);

        // Формула Гаверсинуса
        double a = Math.sin(Δφ/2) * Math.sin(Δφ/2) +
                Math.cos(φ1) * Math.cos(φ2) *
                        Math.sin(Δλ/2) * Math.sin(Δλ/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));

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
