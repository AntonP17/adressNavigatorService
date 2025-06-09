package by.antohakon.adressnavigatorservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class DaDataApiResponse {

    @JsonProperty("result")
    private String formattedAddress;  // "г Санкт-Петербург, ул Олеко Дундича, д 5"

    @JsonProperty("geo_lat")
    private String latitude;         // "59.827906"

    @JsonProperty("geo_lon")
    private String longitude;        // "30.389505"

    // Геттер для координат в формате "долгота,широта"
    public String getCoordinates() {
        return this.longitude + "," + this.latitude;
    }
}
