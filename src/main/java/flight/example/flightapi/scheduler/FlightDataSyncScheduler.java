package flight.example.flightapi.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import flight.example.flightapi.service.DataFetchService;
import flight.example.flightapi.service.DelayFlightService;

@Component
public class FlightDataSyncScheduler {

   private static final Logger log = LoggerFactory.getLogger(FlightDataSyncScheduler.class);

   private final DataFetchService dataFetchService;
   private final DelayFlightService delayFlightService;

   public FlightDataSyncScheduler(
         DataFetchService dataFetchService,
         DelayFlightService delayFlightService) {
      this.dataFetchService = dataFetchService;
      this.delayFlightService = delayFlightService;
   }

   /**
    * Auto fetch live flights (toàn bộ) và delayed flights (cả departures +
    * arrivals)
    * mỗi 30 phút.
    *
    * fixedRate = 1800000 ms = 30 phút
    * initialDelay = 60000 ms = chờ 1 phút sau khi app start để tránh fetch ngay
    * lập tức
    */
   @Scheduled(fixedRate = 1_800_000, initialDelay = 60_000)
   public void syncAllFlightData() {
      log.info("Starting scheduled sync for all flight data...");

      try {
         // 1. Fetch & save live flights (toàn bộ, không filter airline)
         log.info("→ Syncing LIVE flights...");
         dataFetchService.fetchLiveFlights(""); // truyền empty string để lấy full data

         // 2. Fetch & save delayed departures
         log.info("→ Syncing DELAYED departures...");
         delayFlightService.fetchDelayedFlights("departures");

         // 3. Fetch & save delayed arrivals
         log.info("→ Syncing DELAYED arrivals...");
         delayFlightService.fetchDelayedFlights("arrivals");

         log.info("Scheduled sync completed successfully.");
      } catch (Exception e) {
         log.error("Error during scheduled flight data sync", e);
      }
   }
}