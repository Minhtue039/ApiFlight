package flight.example.flightapi.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import flight.example.flightapi.entity.FlightDelayDeparture;

public interface FlightDelayDepRepository extends JpaRepository<FlightDelayDeparture, Long> {
   Optional<FlightDelayDeparture> findByFlightIcaoAndDepTimeUtc(String flightIcao, String depTimeUtc);
}