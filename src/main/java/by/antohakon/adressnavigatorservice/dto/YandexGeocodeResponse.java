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
        return response
                .getGeoObjectCollection()
                .getFeatureMember()
                .get(0)
                .getGeoObject()
                .getPoint()
                .getPos(); // "37.617 55.755"
    }

    @Data
    public static class Response {

        @JsonProperty("GeoObjectCollection")
        private GeoObjectCollection geoObjectCollection;

        public GeoObjectCollection getGeoObjectCollection() {
            return geoObjectCollection;
        }
    }

    @Data
    public static class GeoObjectCollection {

        @JsonProperty("featureMember")
        private List<FeatureMember> featureMember;

        public List<FeatureMember> getFeatureMember() {
            return featureMember;
        }
    }

    @Data
    public static class FeatureMember {

        @JsonProperty("GeoObject")
        private GeoObject geoObject;

        public GeoObject getGeoObject() {
            return geoObject;
        }
    }

    @Data
    public static class GeoObject {

        @JsonProperty("Point")
        private Point point;

        public Point getPoint() {
            return point;
        }
    }

    @Data
    public static class Point {

        @JsonProperty("pos")
        private String pos;

        public String getPos() {
            return pos;
        }
    }
}
