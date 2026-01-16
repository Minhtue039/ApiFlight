package flight.example.flightapi.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import flight.example.flightapi.entity.FlightDelayArrival;
import flight.example.flightapi.entity.FlightDelayDeparture;
import flight.example.flightapi.repository.FlightDelayArrRepository;
import flight.example.flightapi.repository.FlightDelayDepRepository;

@Service
public class DelayPersistenceService {

   private static final Logger log = LoggerFactory.getLogger(DelayPersistenceService.class);

   private final FlightDelayDepRepository departureRepository;
   private final FlightDelayArrRepository arrivalRepository;

   public DelayPersistenceService(
         FlightDelayDepRepository departureRepository,
         FlightDelayArrRepository arrivalRepository) {
      this.departureRepository = departureRepository;
      this.arrivalRepository = arrivalRepository;
   }

   @Async
   public void saveDeparturesAsync(List<FlightDelayDeparture> departures) {
      if (departures == null || departures.isEmpty()) {
         return;
      }

      java.util.concurrent.atomic.AtomicInteger newCount = new java.util.concurrent.atomic.AtomicInteger(0);
      java.util.concurrent.atomic.AtomicInteger updateCount = new java.util.concurrent.atomic.AtomicInteger(0);

      for (FlightDelayDeparture incoming : departures) {
         departureRepository.findByFlightIcaoAndDepTimeUtc(
               incoming.getFlightIcao(),
               incoming.getDepTimeUtc()).ifPresentOrElse(
                     existing -> {
                        // Update các field (trừ id, flightIcao, depTimeUtc)
                        copyDepartureFields(incoming, existing);
                        existing.setFetchedAt(System.currentTimeMillis());
                        departureRepository.save(existing);
                        updateCount.incrementAndGet();
                        log.debug("Updated existing departure flight: {} - {}",
                              incoming.getFlightIcao(), incoming.getDepTimeUtc());
                     },
                     () -> {
                        departureRepository.save(incoming);
                        newCount.incrementAndGet();
                        log.debug("Saved new departure flight: {} - {}",
                              incoming.getFlightIcao(), incoming.getDepTimeUtc());
                     });
      }

      log.info("Async save DEPARTURES completed | New: {} | Updated: {} | Total: {}",
            newCount.get(), updateCount.get(), departures.size());
   }

   @Async
   public void saveArrivalsAsync(List<FlightDelayArrival> arrivals) {
      if (arrivals == null || arrivals.isEmpty()) {
         return;
      }

      java.util.concurrent.atomic.AtomicInteger newCount = new java.util.concurrent.atomic.AtomicInteger(0);
      java.util.concurrent.atomic.AtomicInteger updateCount = new java.util.concurrent.atomic.AtomicInteger(0);

      for (FlightDelayArrival incoming : arrivals) {
         arrivalRepository.findByFlightIcaoAndDepTimeUtc(
               incoming.getFlightIcao(),
               incoming.getDepTimeUtc()).ifPresentOrElse(
                     existing -> {
                        // Update các field (trừ id, flightIcao, depTimeUtc)
                        copyArrivalFields(incoming, existing);
                        existing.setFetchedAt(System.currentTimeMillis());
                        arrivalRepository.save(existing);
                        updateCount.incrementAndGet();
                        log.debug("Updated existing arrival flight: {} - {}",
                              incoming.getFlightIcao(), incoming.getDepTimeUtc());
                     },
                     () -> {
                        arrivalRepository.save(incoming);
                        newCount.incrementAndGet();
                        log.debug("Saved new arrival flight: {} - {}",
                              incoming.getFlightIcao(), incoming.getDepTimeUtc());
                     });
      }

      log.info("Async save ARRIVALS completed | New: {} | Updated: {} | Total: {}",
            newCount.get(), updateCount.get(), arrivals.size());
   }

   private void copyDepartureFields(FlightDelayDeparture src, FlightDelayDeparture target) {
      target.setAirlineIata(src.getAirlineIata());
      target.setAirlineIcao(src.getAirlineIcao());
      target.setFlightIata(src.getFlightIata());
      target.setFlightNumber(src.getFlightNumber());
      target.setDepIata(src.getDepIata());
      target.setDepIcao(src.getDepIcao());
      target.setDepTerminal(src.getDepTerminal());
      target.setDepGate(src.getDepGate());
      target.setDepTime(src.getDepTime());
      target.setDepEstimated(src.getDepEstimated());
      target.setDepActual(src.getDepActual());
      target.setArrIata(src.getArrIata());
      target.setArrIcao(src.getArrIcao());
      target.setArrTerminal(src.getArrTerminal());
      target.setArrGate(src.getArrGate());
      target.setArrTime(src.getArrTime());
      target.setArrEstimated(src.getArrEstimated());
      target.setArrActual(src.getArrActual());
      target.setStatus(src.getStatus());
      target.setDuration(src.getDuration());
      target.setDelayed(src.getDelayed());
      target.setDepDelayed(src.getDepDelayed());
      target.setAircraftIcao(src.getAircraftIcao());
      // fetchedAt được set riêng ở trên
   }

   private void copyArrivalFields(FlightDelayArrival src, FlightDelayArrival target) {
      target.setAirlineIata(src.getAirlineIata());
      target.setAirlineIcao(src.getAirlineIcao());
      target.setFlightIata(src.getFlightIata());
      target.setFlightNumber(src.getFlightNumber());
      target.setDepIata(src.getDepIata());
      target.setDepIcao(src.getDepIcao());
      target.setDepTerminal(src.getDepTerminal());
      target.setDepGate(src.getDepGate());
      target.setDepTime(src.getDepTime());
      target.setDepEstimated(src.getDepEstimated());
      target.setDepActual(src.getDepActual());
      target.setArrIata(src.getArrIata());
      target.setArrIcao(src.getArrIcao());
      target.setArrTerminal(src.getArrTerminal());
      target.setArrGate(src.getArrGate());
      target.setArrTime(src.getArrTime());
      target.setArrEstimated(src.getArrEstimated());
      target.setArrActual(src.getArrActual());
      target.setStatus(src.getStatus());
      target.setDuration(src.getDuration());
      target.setDelayed(src.getDelayed());
      target.setArrDelayed(src.getArrDelayed());
      target.setAircraftIcao(src.getAircraftIcao());
      // fetchedAt được set riêng ở trên
   }
}