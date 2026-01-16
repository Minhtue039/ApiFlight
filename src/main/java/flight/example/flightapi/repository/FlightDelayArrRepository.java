package flight.example.flightapi.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import flight.example.flightapi.entity.FlightDelayArrival;

public interface FlightDelayArrRepository extends JpaRepository<FlightDelayArrival, Long> {
   Optional<FlightDelayArrival> findByFlightIcaoAndDepTimeUtc(String flightIcao, String depTimeUtc);
}
