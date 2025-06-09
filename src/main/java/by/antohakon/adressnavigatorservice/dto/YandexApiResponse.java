package by.antohakon.adressnavigatorservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class YandexApiResponse {

    @JsonProperty("response")
    private Response response;

    @Data
    public static class Response {
        @JsonProperty("GeoObjectCollection")
        private GeoObjectCollection geoObjectCollection;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class GeoObjectCollection {
        @JsonProperty("featureMember")
        private List<FeatureMember> featureMember;
    }

    @Data
    public static class FeatureMember {
        @JsonProperty("GeoObject")
        private GeoObject geoObject;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class GeoObject {
        @JsonProperty("metaDataProperty")
        private MetaDataProperty metaDataProperty;

        @JsonProperty("Point")
        private Point point;
    }

    @Data
    public static class MetaDataProperty {
        @JsonProperty("GeocoderMetaData")
        private GeocoderMetaData geocoderMetaData;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class GeocoderMetaData {
        @JsonProperty("text")
        private String formattedAddress; // "Россия, Санкт-Петербург, улица Олеко Дундича, 10к1"

        @JsonProperty("Address")
        private Address address;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Address {
        @JsonProperty("formatted")
        private String fullAddress;

        @JsonProperty("postal_code")
        private String postalCode;
    }

    @Data
    public static class Point {
        @JsonProperty("pos")
        private String position; // "30.392764 59.829161" (долгота и широта)
    }

    // Геттер для чистого адреса (аналог result в DaData)
    public String getFormattedAddress() {
        if (response != null
                && response.geoObjectCollection != null
                && !response.geoObjectCollection.featureMember.isEmpty()) {
            return response.geoObjectCollection.featureMember.get(0)
                    .geoObject.metaDataProperty.geocoderMetaData.formattedAddress;
        }
        return null;
    }

    // Геттер для координат в формате "долгота,широта"
    public String getCoordinates() {
        if (response != null
                && response.geoObjectCollection != null
                && !response.geoObjectCollection.featureMember.isEmpty()) {
            String pos = response.geoObjectCollection.featureMember.get(0)
                    .geoObject.point.position;
            return pos.replace(" ", ","); // Преобразуем "30.392764 59.829161" в "30.392764,59.829161"
        }
        return null;
    }
}
