package by.antohakon.adressnavigatorservice.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "adress_navigation")
public class AdressDistantionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String firstAdress;

    private String secondAdress;

    private double distantion;

}
