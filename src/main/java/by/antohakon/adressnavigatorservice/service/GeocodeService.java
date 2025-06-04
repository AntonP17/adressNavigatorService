package by.antohakon.adressnavigatorservice.service;

import by.antohakon.adressnavigatorservice.dto.AdressDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final Logger log = LoggerFactory.getLogger(GeocodeService.class);


    // пусть основной метод
    public void processAddress(AdressDto adressDto) {

        String rawAddress = adressDto.adress();

        // 1. Очистка адреса через DaData
        String cleanedAddress = cleanAddressViaDaData(rawAddress);

        // 2. Поиск координат через Yandex
        String coordinates = fetchCoordinatesViaYandex(cleanedAddress);

        // 3. Вывод в консоль
        System.out.printf("""
            Исходный адрес: %s
            Очищенный адрес: %s
            Координаты: %s
            """, rawAddress, cleanedAddress, coordinates);
    }

    // парсинг адреса из вакханалии в номлаьный
    private String cleanAddressViaDaData(String dirtyAddress) {

        log.info("зашли в метод cleanAddressViaDaData");
        String url = "https://cleaner.dadata.ru/api/v1/clean/address";
        String requestBody = "[\"" + dirtyAddress + "\"]";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Token " + dadataApiKey)
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

        // Парсим ответ DaData (пример: [{"result":"г Москва, ул Тверская, д 7"}]) НО тут NPE удет
//        String json = response.body();
//        return json.split("\"result\":\"")[1].split("\"")[0];

        // Парсим ответ DaData (пример: [{"result":"г Москва, ул Тверская, д 7"}])
        String json = response != null ? response.body() : null;
        log.info("ответ = {}", json);

        if (json == null || !json.contains("\"result\":\"")) {
            log.error("Неверный формат JSON или пустой ответ от DaData");
            return "Адрес не распознан";
        }

        try {
            String[] parts = json.split("\"result\":\"");
            if (parts.length < 2) {
                log.error("Не найден ключ 'result' в ответе DaData");
                return "Адрес не распознан";
            }

            String cleanAddress = parts[1].split("\"")[0];
            log.info("cleanAddress = {}", cleanAddress);
            return cleanAddress;
        } catch (Exception e) {
            log.error("Ошибка парсинга адреса из DaData", e);
            return "Ошибка обработки адреса";
        }
    }

    //поиск координат яндек кратами
    private String fetchCoordinatesViaYandex(String address)  {
        log.info("зашли в метод fetchCoordinatesViaYandex");
        String url = "https://geocode-maps.yandex.ru/1.x/?apikey=" + yandexApiKey
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

        // Парсим ответ Yandex (пример: "pos":"37.6056 55.7602") АНАЛОГИЧНО NPE
//        String json = response.body();
//        String pos = json.split("\"pos\":\"")[1].split("\"")[0];
//        String[] parts = pos.split(" ");
//        return parts[1] + "°N, " + parts[0] + "°E";

        // Парсим ответ Yandex (пример: "pos":"37.6056 55.7602")
        String json = response != null ? response.body() : null;
        log.info("ответ = {}", json);

        if (json == null || !json.contains("\"pos\":\"")) {
            log.error("Неверный формат JSON или пустой ответ");
            return "Координаты не найдены";
        }

        try {
            String pos = json.split("\"pos\":\"")[1].split("\"")[0];
            log.info("координаты = {}", pos);

            String[] parts = pos.split(" ");
            if (parts.length < 2) {
                log.error("Неверный формат координат");
                return "Координаты не найдены";
            }

            return parts[1] + "°N, " + parts[0] + "°E";
        } catch (Exception e) {
            log.error("Ошибка парсинга координат", e);
            return "Ошибка при обработке координат";
        }
    }




}
