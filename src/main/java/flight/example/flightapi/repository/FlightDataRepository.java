package flight.example.flightapi.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import flight.example.flightapi.entity.FlightData;

public interface FlightDataRepository extends JpaRepository<FlightData, String> {

      Optional<FlightData> findByHex(String hex);
}
