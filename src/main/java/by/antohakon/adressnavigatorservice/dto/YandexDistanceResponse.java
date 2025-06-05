package by.antohakon.adressnavigatorservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class YandexDistanceResponse {

    private List<Row> rows;

    public int getDistanceMeters() {
        return rows.get(0).getElements().get(0).getDistance().getValue();
    }

    @Data
    public static class Row {
        private List<Element> elements;
    }

    @Data
    public static class Element {
        private Distance distance;
    }

    @Data
    public static class Distance {
        private int value;
    }

}
