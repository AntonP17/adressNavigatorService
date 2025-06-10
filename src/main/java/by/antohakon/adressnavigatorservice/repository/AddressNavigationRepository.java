package by.antohakon.adressnavigatorservice.repository;

import by.antohakon.adressnavigatorservice.entity.AddressDistantionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AddressNavigationRepository extends JpaRepository<AddressDistantionEntity, Long> {

    Optional<AddressDistantionEntity> findByAddress(String address);
}
