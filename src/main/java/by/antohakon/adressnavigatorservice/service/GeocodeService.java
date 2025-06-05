package by.antohakon.adressnavigatorservice.service;

import by.antohakon.adressnavigatorservice.dto.DaDataCleanResponse;
import by.antohakon.adressnavigatorservice.dto.YandexGeocodeResponse;
import by.antohakon.adressnavigatorservice.dto.requestAdressDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

@Service
public class GeocodeService {

    @Value("${yandex.api.key}")
    private String yandexApiKey;

    @Value("${dadata.api.key}")
    private String dadataApiKey;

    @Value("${dadata.secret.key}")
    private String dadataSecretKey;

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final Logger log = LoggerFactory.getLogger(GeocodeService.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    // пусть основной метод
    public void processAddress(requestAdressDto adressDto)  {

        String adresStartPoint = adressDto.adressStartPoint();
        String adresEndPoint = adressDto.adressEndPoint();

        // 1. Очистка адреса через DaData
        String cleanedAddressStart = cleanAddressViaDaData(adresStartPoint);
        String cleanedAddressEnd = cleanAddressViaDaData(adresEndPoint);

        // 2. Поиск координат через Yandex
        String coordinatesStart = fetchCoordinatesViaYandex(cleanedAddressStart);
        String coordinatesEnd = fetchCoordinatesViaYandex(cleanedAddressEnd);

        String distantion = getDistance(coordinatesStart,coordinatesEnd);

        // 3. Вывод в консоль
//        System.out.printf("""
//            Исходный адрес: %s
//            Очищенный адрес: %s
//            Координаты: %s
//            """, rawAddress, cleanedAddress, coordinates);

    }

    // парсинг адреса из вакханалии в номлаьный
    private String cleanAddressViaDaData(String dirtyAddress) {

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

        HttpResponse<String> response = null;
        try {
            response = httpClient.send(
                    request, HttpResponse.BodyHandlers.ofString()
            );
            log.info("ответ = {}",response.body());
        } catch (IOException | InterruptedException e) {
            log.error("исключение в методе cleanAddressViaDaData = {}", e.getMessage());
        }

        // Парсим ответ DaData (пример: [{"result":"г Москва, ул Тверская, д 7"}])
        String json = response.body();
        log.info("ответ = {}", json);

        DaDataCleanResponse[] cleanResponse = null;
        try {
            cleanResponse = objectMapper.readValue(json, DaDataCleanResponse[].class);
        } catch (JsonProcessingException e) {
            log.error("не получилсоь прочитать json = {}", e.getMessage());
        }


        String cleanAdress = cleanResponse[0].getResult();
        log.info("чистый адрес = " + cleanAdress);
        return cleanAdress;

    }

    //поиск координат яндек кратами
    private String fetchCoordinatesViaYandex(String address)  {

        log.info("зашли в метод fetchCoordinatesViaYandex");
        String url = "https://geocode-maps.yandex.ru/v1/?apikey=" + yandexApiKey
                + "&geocode=" + URLEncoder.encode(address, StandardCharsets.UTF_8)
                + "&format=json";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        HttpResponse<String> response = null;
        try {
            response = httpClient.send(
                    request, HttpResponse.BodyHandlers.ofString()
            );
        } catch (IOException | InterruptedException e) {
            log.error("исключение в методе fetchCoordinatesViaYandex = {}", e.getMessage());
        }

        // Парсим ответ Yandex (пример: "pos":"37.6056 55.7602")
        String json = response.body();
        log.info("ответ = {}", json);

        YandexGeocodeResponse yandexGeocodeResponse = null;
        try {
            yandexGeocodeResponse = objectMapper.readValue(json, YandexGeocodeResponse.class);
        } catch (JsonProcessingException e) {
            log.error("не получилсоь прочитать json = {}", e.getMessage());
        }

        String coordinates = yandexGeocodeResponse.getCoordinates();
        log.info("координаты = " + coordinates);
        return coordinates;

    }


    //подсчет расстояния
    public String getDistance(String startCoords, String endCoords)  {

        log.info("зашли в метод getDistance");
        // 1. Формируем URL для Yandex Matrix API
        String url = "https://api.routing.yandex.net/v2/distancematrix" +
                "?origins=" + startCoords +  // Формат: "широта,долгота"
                "&destinations=" + endCoords +
                "&apikey=" + yandexApiKey;   // Ключ из application.properties

        // 2. Отправляем запрос
        HttpResponse<String> response = null;
        try {
            response = httpClient.send(
                    HttpRequest.newBuilder()
                            .uri(URI.create(url))
                            .GET()
                            .build(),
                    HttpResponse.BodyHandlers.ofString()
            );
        } catch (IOException | InterruptedException e) {
            log.error("исключение в методе getDistance = {}", e.getMessage());
        }

        // 3. Парсим ответ и возвращаем строку с метрами
        String json = response.body();
        String distanceStr = json.split("\"distance\":\\{\"value\":")[1].split("\\}")[0];
        return distanceStr + " м";  // Пример: "634000 м"
    }



}
