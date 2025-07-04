package by.antohakon.adressnavigatorservice.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Check;

@Entity
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "adress_navigation", indexes = {
        @Index(columnList = "address", name = "address_index")
})
public class AddressDistantionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String address;

    @Column(nullable = false )
    @Check(constraints = "distantion >= 0")
    private double distantion;

}
