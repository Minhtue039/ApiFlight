package flight.example.flightapi.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import flight.example.flightapi.entity.AirlineFlightSnapshot;

public interface AirlineFlightSnapshotRepository extends JpaRepository<AirlineFlightSnapshot, Long> {

   Optional<AirlineFlightSnapshot> findByAirlineNameAndSnapshotDateAndHex(
         String airlineName, LocalDate snapshotDate, String hex);

   @Query("SELECT s FROM AirlineFlightSnapshot s " +
         "WHERE s.airlineName = :airlineName " +
         "ORDER BY s.snapshotDate DESC")
   List<AirlineFlightSnapshot> findByAirlineNameOrderByDateDesc(String airlineName);
}