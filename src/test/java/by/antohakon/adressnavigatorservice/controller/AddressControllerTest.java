package by.antohakon.adressnavigatorservice.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for the {@link AddressController}
 */
@WebMvcTest({AddressController.class})
public class AddressControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    public void setup() {

    }

    @Test
    public void processAddress() throws Exception {
        String addressDto = """
                {
                    "address": "Спб Олеко Дундича 5"
                }""";

        mockMvc.perform(post("/api/adress/process")
                        .content(addressDto)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    public void getAddresses() throws Exception {
        mockMvc.perform(get("/api/adress/all")
                        .param("pageNumber", "0")
                        .param("pageSize", "0")
                        .param("sort", ""))
                .andExpect(status().isOk())
                .andDo(print());
    }
}
