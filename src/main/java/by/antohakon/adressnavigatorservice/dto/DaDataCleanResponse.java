package by.antohakon.adressnavigatorservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

@Data
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class DaDataCleanResponse {
    private String result;

    public String getResult() {
        return result;
    }
}
