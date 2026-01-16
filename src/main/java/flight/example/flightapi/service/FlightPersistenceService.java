package flight.example.flightapi.service;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import flight.example.flightapi.entity.FlightData;
import flight.example.flightapi.repository.FlightDataRepository;

@Service
public class FlightPersistenceService {
   private static final Logger log = LoggerFactory.getLogger(FlightPersistenceService.class);

   @Autowired
   private FlightDataRepository repository;

   // Asynchronous handling methods to avoid blocking the main thread.
   @Async
   public void saveFlightsAsync(List<FlightData> flights) {
      int newCount = 0, updateCount = 0;

      for (FlightData data : flights) {
         String hex = data.getHex();
         Optional<FlightData> opt = repository.findByHex(hex);

         if (opt.isPresent()) {
            FlightData existing = opt.get();
            Long apiTs = data.getUpdated();
            Long dbTs = existing.getUpdated();

            if (apiTs != null && (dbTs == null || apiTs > dbTs)) {
               existing.setLat(data.getLat());
               existing.setLng(data.getLng());
               existing.setAlt(data.getAlt());
               existing.setDir(data.getDir());
               existing.setSpeed(data.getSpeed());
               existing.setvSpeed(data.getvSpeed());
               existing.setFlightNumber(data.getFlightNumber());
               existing.setFlightIcao(data.getFlightIcao());
               existing.setFlightIata(data.getFlightIata());
               existing.setDepIcao(data.getDepIcao());
               existing.setDepIata(data.getDepIata());
               existing.setArrIcao(data.getArrIcao());
               existing.setArrIata(data.getArrIata());
               existing.setAirlineIcao(data.getAirlineIcao());
               existing.setAirlineIata(data.getAirlineIata());
               existing.setAircraftIcao(data.getAircraftIcao());
               existing.setUpdated(apiTs);
               existing.setStatus(data.getStatus());
               existing.setName(data.getName());

               repository.save(existing);
               updateCount++;
               log.info("Updated DB hex={}", hex);
            }
         } else {
            repository.save(data);
            newCount++;
            log.info("Saved new hex={}", hex);
         }
      }

      log.info("Async save completed | New: {} | Updated: {} | Total: {}",
            newCount, updateCount, flights.size());
   }
}
