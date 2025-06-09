package by.antohakon.adressnavigatorservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.Getter;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class YandexGeocodeResponse {

    private Response response;

    public String getCoordinates() {
        if (response == null || response.getGeoObjectCollection() == null ||
                response.getGeoObjectCollection().getFeatureMember() == null ||
                response.getGeoObjectCollection().getFeatureMember().isEmpty()) {
            return null;
        }

        return response
                .getGeoObjectCollection()
                .getFeatureMember()
                .get(0)
                .getGeoObject()
                .getPoint()
                .getPos(); // "37.617 55.755"
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Response {
        @JsonProperty("GeoObjectCollection")
        private GeoObjectCollection geoObjectCollection;

    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class GeoObjectCollection {
        @JsonProperty("metaDataProperty")
        private MetaDataProperty metaDataProperty;

        @JsonProperty("featureMember")
        private List<FeatureMember> featureMember;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class MetaDataProperty {
        @JsonProperty("GeocoderResponseMetaData")
        private GeocoderResponseMetaData geocoderResponseMetaData;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class GeocoderResponseMetaData {
        private String request;
        private String results;
        private String found;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FeatureMember {
        @JsonProperty("GeoObject")
        private GeoObject geoObject;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class GeoObject {
        @JsonProperty("metaDataProperty")
        private GeoObjectMetaDataProperty metaDataProperty;

        @JsonProperty("Point")
        private Point point;

        private String name;
        private String description;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class GeoObjectMetaDataProperty {
        @JsonProperty("GeocoderMetaData")
        private GeocoderMetaData geocoderMetaData;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class GeocoderMetaData {
        private String precision;
        private String text;
        private String kind;
        private Address address;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Address {
        private String country_code;
        private String formatted;
        private List<AddressComponent> Components;

    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AddressComponent {
        private String kind;
        private String name;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Point {
        @JsonProperty("pos")
        private String pos;
    }
}
