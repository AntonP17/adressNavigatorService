package by.antohakon.adressnavigatorservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class DaDataApiResponse {

    @JsonProperty("result")
    private String formattedAddress;

    @JsonProperty("geo_lat")
    private String latitude;

    @JsonProperty("geo_lon")
    private String longitude;

    public String getCoordinates() {
        return this.longitude + "," + this.latitude;
    }
}
