package flight.example.flightapi.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import flight.example.flightapi.entity.AirlineFlightSnapshot;
import flight.example.flightapi.entity.FlightData;
import flight.example.flightapi.repository.AirlineFlightSnapshotRepository;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Service
public class SnapshotService {

   private static final Logger log = LoggerFactory.getLogger(SnapshotService.class);

   private final AirlineFlightSnapshotRepository repository;
   private final ObjectMapper objectMapper;

   public SnapshotService(AirlineFlightSnapshotRepository repository, ObjectMapper objectMapper) {
      this.repository = repository;
      this.objectMapper = objectMapper;
   }

   @Async
   public void saveOrUpdateSnapshot(String airlineName, List<FlightData> flights) {
      if (airlineName == null || airlineName.trim().isEmpty() || flights == null || flights.isEmpty()) {
         return;
      }

      LocalDate today = LocalDate.now();
      long now = System.currentTimeMillis();
      int newCount = 0;
      int updateCount = 0;

      for (FlightData flight : flights) {
         String hex = flight.getHex();
         if (hex == null || hex.trim().isEmpty())
            continue;
         hex = hex.trim().toUpperCase();

         Optional<AirlineFlightSnapshot> existingOpt = repository.findByAirlineNameAndSnapshotDateAndHex(
               airlineName.trim(), today, hex);

         if (existingOpt.isPresent()) {
            AirlineFlightSnapshot existing = existingOpt.get();
            if (!existing.getStatus().equals(flight.getStatus())) { // update chỉ nếu status change
               existing.setStatus(flight.getStatus());
               existing.setFetchedAt(flight.getFetchedAt());
               existing.setUpdatedAt(now);
               try {
                  existing.setFlightJson(objectMapper.writeValueAsString(flight));
               } catch (JacksonException e) {
                  log.warn("JSON serialization failed for hex: {}", hex, e);
                  existing.setFlightJson("{}");
               }
               repository.save(existing);
               updateCount++;
               log.debug("Updated snapshot for airline '{}' hex '{}' on {} (status changed)", airlineName, hex, today);
            } else {
               log.debug("Skipped update for airline '{}' hex '{}' on {} (no status change)", airlineName, hex, today);
            }
         } else {
            AirlineFlightSnapshot snapshot = new AirlineFlightSnapshot();
            snapshot.setAirlineName(airlineName.trim());
            snapshot.setSnapshotDate(today);
            snapshot.setHex(hex);
            snapshot.setFlightIcao(flight.getFlightIcao());
            snapshot.setFlightIata(flight.getFlightIata());
            snapshot.setStatus(flight.getStatus());
            snapshot.setFetchedAt(flight.getFetchedAt());
            snapshot.setUpdatedAt(now);
            try {
               snapshot.setFlightJson(objectMapper.writeValueAsString(flight));
            } catch (JacksonException e) {
               log.warn("JSON serialization failed for hex: {}", hex, e);
               snapshot.setFlightJson("{}");
            }
            repository.save(snapshot);
            newCount++;
            log.debug("Saved new snapshot for airline '{}' hex '{}' on {}", airlineName, hex, today);
         }
      }

      log.info("Snapshot save completed for '{}' on {} | New: {} | Updated: {} | Total processed: {}",
            airlineName, today, newCount, updateCount, flights.size());
   }

   public List<AirlineFlightSnapshot> getSnapshotsForAirline(String airlineName) {
      if (airlineName == null || airlineName.trim().isEmpty()) {
         return List.of();
      }
      return repository.findByAirlineNameOrderByDateDesc(airlineName.trim());
   }

   // Mới: Merge current + historical today, dedup by hex, update if status change
   public List<FlightData> mergeWithHistorical(List<FlightData> currentFlights, List<AirlineFlightSnapshot> snapshots,
         String airlineName) {
      LocalDate today = LocalDate.now();
      List<FlightData> combined = new ArrayList<>(currentFlights);

      // Get historical today
      List<AirlineFlightSnapshot> todaySnapshots = snapshots.stream()
            .filter(s -> s.getSnapshotDate().equals(today))
            .collect(Collectors.toList());

      Map<String, AirlineFlightSnapshot> historicalMap = todaySnapshots.stream()
            .collect(Collectors.toMap(AirlineFlightSnapshot::getHex, s -> s));

      // Merge: add historical not in current, update if status change
      for (FlightData current : currentFlights) {
         String hex = current.getHex();
         if (historicalMap.containsKey(hex)) {
            AirlineFlightSnapshot historical = historicalMap.get(hex);
            if (!historical.getStatus().equals(current.getStatus())) {
               // Update historical
               historical.setStatus(current.getStatus());
               historical.setFetchedAt(current.getFetchedAt());
               historical.setUpdatedAt(System.currentTimeMillis());
               repository.save(historical);
               log.debug("Merged update for hex {} (status changed)", hex);
            }
            historicalMap.remove(hex); // remove to avoid adding duplicate
         }
      }

      // Add remaining historical (not in current)
      for (AirlineFlightSnapshot historical : historicalMap.values()) {
         FlightData converted = convertSnapshotToFlightData(historical);
         combined.add(converted);
         log.debug("Added historical flight for hex {}", historical.getHex());
      }

      return combined;
   }

   // Helper: Convert snapshot back to FlightData for display
   private FlightData convertSnapshotToFlightData(AirlineFlightSnapshot snapshot) {
      FlightData flight = new FlightData();
      flight.setHex(snapshot.getHex());
      flight.setFlightIcao(snapshot.getFlightIcao());
      flight.setFlightIata(snapshot.getFlightIata());
      flight.setStatus(snapshot.getStatus());
      flight.setName(snapshot.getAirlineName());
      flight.setUpdated(snapshot.getUpdatedAt());
      flight.setFetchedAt(snapshot.getFetchedAt());
      // Add more fields if needed from flightJson if you parse it
      return flight;
   }
}