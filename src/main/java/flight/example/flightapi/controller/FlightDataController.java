package flight.example.flightapi.controller;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import flight.example.flightapi.dto.DelayFlightResponse;
import flight.example.flightapi.entity.FlightData;
import flight.example.flightapi.service.DataFetchService;
import flight.example.flightapi.service.DelayFlightService;
import io.micrometer.common.util.StringUtils;
import jakarta.servlet.http.HttpSession;

@Controller
public class FlightDataController {

   private final DataFetchService dataFetchService;
   private final DelayFlightService delayFlightService;

   public FlightDataController(DataFetchService dataFetchService, DelayFlightService delayFlightService) {
      this.dataFetchService = dataFetchService;
      this.delayFlightService = delayFlightService;
   }

   @GetMapping("/")
   public String showFlightData(
         @RequestParam(value = "airline", required = false, defaultValue = "") String airlineName,
         @RequestParam(value = "tab", required = false, defaultValue = "live") String tab,
         @RequestParam(value = "delayType", required = false, defaultValue = "departures") String delayType,
         @RequestParam(value = "action", required = false, defaultValue = "") String action,
         HttpSession session,
         Model model) {

      String effectiveAirline = airlineName.trim();

      if ("delayed".equals(tab)) {
         String sessionKey = "delayedFlights_" + delayType;
         List<DelayFlightResponse> delayedFlights = (List<DelayFlightResponse>) session.getAttribute(sessionKey);

         if ("fetch".equals(action) || delayedFlights == null) {
            delayedFlights = delayFlightService.fetchDelayedFlights(delayType);
            session.setAttribute(sessionKey, delayedFlights);
         }

         model.addAttribute("delayedFlights", delayedFlights);
         model.addAttribute("delayType", delayType);
         model.addAttribute("totalDelayed", delayedFlights != null ? delayedFlights.size() : 0);
         model.addAttribute("tab", "delayed");

      } else {

         List<FlightData> fullFlights = (List<FlightData>) session.getAttribute("fullFlights");
         List<FlightData> displayedFlights;

         if ("fetch".equals(action) || fullFlights == null) {
            fullFlights = dataFetchService.fetchLiveFlights("");// empty to get full
            session.setAttribute("fullFlights", fullFlights);
            effectiveAirline = ""; // reset search
         }

         if ("search".equals(action) && !effectiveAirline.isEmpty()) {
            // Filter in-memory on fullFlights
            String search = effectiveAirline.toLowerCase();
            displayedFlights = fullFlights.stream()
                  .filter(f -> f.getName() != null && f.getName().toLowerCase().contains(search))
                  .collect(Collectors.toList());
         } else if ("clear".equals(action)) {
            displayedFlights = fullFlights;
            effectiveAirline = "";
         } else {
            displayedFlights = fullFlights; // default to full
         }

         List<String> airlineNames = fullFlights.stream()
               .map(FlightData::getName)
               .filter(StringUtils::isNotBlank)
               .distinct()
               .sorted()
               .collect(Collectors.toList());

         // Statistics by status
         Map<String, Long> statusCount = displayedFlights.stream()
               .filter(f -> f.getStatus() != null)
               .collect(Collectors.groupingBy(
                     FlightData::getStatus,
                     Collectors.counting()));

         model.addAttribute("flights", displayedFlights);
         model.addAttribute("searchAirline", effectiveAirline);
         model.addAttribute("totalFlights", displayedFlights.size());
         model.addAttribute("airlineNames", airlineNames);
         model.addAttribute("statusEnRoute", statusCount.getOrDefault("en-route", 0L));
         model.addAttribute("statusLanded", statusCount.getOrDefault("landed", 0L));
         model.addAttribute("statusScheduled", statusCount.getOrDefault("scheduled", 0L));
         model.addAttribute("tab", "live");

      }

      return "flight-list";
   }
}
