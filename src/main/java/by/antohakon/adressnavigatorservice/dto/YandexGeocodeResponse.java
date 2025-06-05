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

    public Response getResponse() {
        return response;
    }

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

        public GeoObjectCollection getGeoObjectCollection() {
            return geoObjectCollection;
        }
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class GeoObjectCollection {
        @JsonProperty("metaDataProperty")
        private MetaDataProperty metaDataProperty;

        @JsonProperty("featureMember")
        private List<FeatureMember> featureMember;

        public MetaDataProperty getMetaDataProperty() {
            return metaDataProperty;
        }

        public List<FeatureMember> getFeatureMember() {
            return featureMember;
        }
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class MetaDataProperty {
        @JsonProperty("GeocoderResponseMetaData")
        private GeocoderResponseMetaData geocoderResponseMetaData;

        public GeocoderResponseMetaData getGeocoderResponseMetaData() {
            return geocoderResponseMetaData;
        }
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class GeocoderResponseMetaData {
        private String request;
        private String results;
        private String found;

        public String getRequest() {
            return request;
        }

        public String getResults() {
            return results;
        }

        public String getFound() {
            return found;
        }
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FeatureMember {
        @JsonProperty("GeoObject")
        private GeoObject geoObject;

        public GeoObject getGeoObject() {
            return geoObject;
        }
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

        public GeoObjectMetaDataProperty getMetaDataProperty() {
            return metaDataProperty;
        }

        public Point getPoint() {
            return point;
        }

        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class GeoObjectMetaDataProperty {
        @JsonProperty("GeocoderMetaData")
        private GeocoderMetaData geocoderMetaData;

        public GeocoderMetaData getGeocoderMetaData() {
            return geocoderMetaData;
        }
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class GeocoderMetaData {
        private String precision;
        private String text;
        private String kind;
        private Address address;

        public String getPrecision() {
            return precision;
        }

        public String getText() {
            return text;
        }

        public String getKind() {
            return kind;
        }

        public Address getAddress() {
            return address;
        }
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Address {
        private String country_code;
        private String formatted;
        private List<AddressComponent> Components;

        public String getCountry_code() {
            return country_code;
        }

        public String getFormatted() {
            return formatted;
        }

        public List<AddressComponent> getComponents() {
            return Components;
        }
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AddressComponent {
        private String kind;
        private String name;

        public String getKind() {
            return kind;
        }

        public String getName() {
            return name;
        }
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Point {
        @JsonProperty("pos")
        private String pos;

        public String getPos() {
            return pos;
        }
    }
}
