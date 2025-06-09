package by.antohakon.adressnavigatorservice.repository;

import by.antohakon.adressnavigatorservice.dto.responseAdressDto;
import by.antohakon.adressnavigatorservice.entity.AdressDistantionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AdressNavigationRepository extends JpaRepository<AdressDistantionEntity, Long> {

    boolean existsByFirstAdressAndSecondAdress(String firstAdress, String secondAdress);

    Optional<AdressDistantionEntity> findByFirstAdressAndSecondAdress(String cleanedAddressStart, String cleanedAddressEnd);
}
