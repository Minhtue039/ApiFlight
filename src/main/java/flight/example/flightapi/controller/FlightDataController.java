package flight.example.flightapi.controller;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import flight.example.flightapi.dto.DelayFlightResponse;
import flight.example.flightapi.entity.AirlineFlightSnapshot;
import flight.example.flightapi.entity.FlightData;
import flight.example.flightapi.service.DataFetchService;
import flight.example.flightapi.service.DelayFlightService;
import flight.example.flightapi.service.SnapshotService;
import io.micrometer.common.util.StringUtils;
import jakarta.servlet.http.HttpSession;

@Controller
public class FlightDataController {

   private static final Logger log = LoggerFactory.getLogger(FlightDataController.class);

   private final DataFetchService dataFetchService;
   private final DelayFlightService delayFlightService;
   private final SnapshotService snapshotService;

   public FlightDataController(DataFetchService dataFetchService, DelayFlightService delayFlightService,
         SnapshotService snapshotService) {
      this.dataFetchService = dataFetchService;
      this.delayFlightService = delayFlightService;
      this.snapshotService = snapshotService;
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
         // ... phần delayed giữ nguyên ...
         String sessionKey = "delayedFlights_" + delayType;
         List<DelayFlightResponse> delayedFlights = (List<DelayFlightResponse>) session.getAttribute(sessionKey);

         if ("fetch".equals(action) || delayedFlights == null) {
            delayedFlights = delayFlightService.fetchDelayedFlights(delayType);
            session.setAttribute(sessionKey, delayedFlights);
            log.info("Fetched delayed {} flights: {} items", delayType, delayedFlights.size());
         }

         model.addAttribute("delayedFlights", delayedFlights);
         model.addAttribute("delayType", delayType);
         model.addAttribute("totalDelayed", delayedFlights.size());
         model.addAttribute("tab", "delayed");

      } else { // live tab

         List<FlightData> fullFlights = (List<FlightData>) session.getAttribute("fullFlights");

         // Fetch full nếu cần (chỉ khi fetch hoặc chưa có)
         if ("fetch".equals(action) || fullFlights == null || fullFlights.isEmpty()) {
            fullFlights = dataFetchService.fetchLiveFlights("");
            session.setAttribute("fullFlights", fullFlights);
            effectiveAirline = ""; // reset search khi fetch mới
            log.info("Fetched full flights: {} total", fullFlights.size());
         }

         // Khởi tạo displayedFlights từ full
         List<FlightData> displayedFlights = fullFlights;

         if (!effectiveAirline.isEmpty() && !"save".equals(action)) { // chỉ lọc nếu không phải save (save đã lọc riêng)
            String search = effectiveAirline.toLowerCase();
            displayedFlights = fullFlights.stream()
                  .filter(f -> f.getName() != null && f.getName().toLowerCase().contains(search))
                  .collect(Collectors.toList());
         }

         // Nếu có search → lọc displayed
         if ("search".equals(action) && !effectiveAirline.isEmpty()) {
            String search = effectiveAirline.toLowerCase();
            displayedFlights = fullFlights.stream()
                  .filter(f -> f.getName() != null && f.getName().toLowerCase().contains(search))
                  .collect(Collectors.toList());
            log.info("Search '{}' → found {} displayed flights (total full: {})",
                  effectiveAirline, displayedFlights.size(), fullFlights.size());
         } else if ("clear".equals(action)) {
            effectiveAirline = "";
            displayedFlights = fullFlights;
            log.info("Cleared search → showing all {} flights", fullFlights.size());
         }

         // Gợi ý hãng từ full
         List<String> airlineNames = fullFlights.stream()
               .map(FlightData::getName)
               .filter(StringUtils::isNotBlank)
               .distinct()
               .sorted()
               .collect(Collectors.toList());

         // Thống kê status từ displayed
         Map<String, Long> statusCount = displayedFlights.stream()
               .filter(f -> f.getStatus() != null)
               .collect(Collectors.groupingBy(FlightData::getStatus, Collectors.counting()));

         // SAVE: LUÔN dùng displayedFlights (đã filtered nếu search)
         if ("save".equals(action) && StringUtils.isNotBlank(effectiveAirline)) {
            // Buộc lọc lại để chắc chắn
            String search = effectiveAirline.toLowerCase();
            List<FlightData> flightsToSave = fullFlights.stream()
                  .filter(f -> f.getName() != null && f.getName().toLowerCase().contains(search))
                  .collect(Collectors.toList());

            if (flightsToSave.isEmpty()) {
               model.addAttribute("saveMessage", "Không có chuyến bay nào để lưu cho hãng " + effectiveAirline);
            } else {
               snapshotService.saveOrUpdateSnapshot(effectiveAirline, flightsToSave);
               model.addAttribute("saveMessage", "Đã lưu/cập nhật " + flightsToSave.size() + " chuyến bay cho hãng "
                     + effectiveAirline + " hôm nay!");
               log.info("Saved snapshot for airline: {} ({} filtered flights)", effectiveAirline, flightsToSave.size());
            }

            // REDIRECT sau save để giữ trạng thái search
            return "redirect:/?airline=" + effectiveAirline + "&tab=live&action=search";
         }

         // Lấy lịch sử
         List<AirlineFlightSnapshot> snapshots = snapshotService.getSnapshotsForAirline(effectiveAirline);

         // Format updated
         displayedFlights.forEach(flight -> {
            if (flight.getUpdated() != null) {
               java.util.Date date = new java.util.Date(flight.getUpdated());
               java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM HH:mm");
               flight.setUpdatedFormatted(sdf.format(date));
            }
         });

         // Model: dùng displayedFlights (đã filtered hoặc full)
         model.addAttribute("flights", displayedFlights);
         model.addAttribute("historicalSnapshots", snapshots);
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