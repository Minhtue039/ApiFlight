package flight.example.flightapi.controller;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
         Model model,
         RedirectAttributes redirectAttributes) {

      String effectiveAirline = airlineName.trim();

      if ("delayed".equals(tab)) {
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

         if ("fetch".equals(action) || fullFlights == null || fullFlights.isEmpty()) {
            fullFlights = dataFetchService.fetchLiveFlights("");
            session.setAttribute("fullFlights", fullFlights);
            log.info("Fetched full flights: {} total", fullFlights.size());
         }

         List<FlightData> displayedFlights = fullFlights;
         String searchAirline = effectiveAirline;

         // ƯU TIÊN: Nếu action = clear → luôn hiển thị full, xóa airline
         if ("clear".equals(action)) {
            searchAirline = "";
            effectiveAirline = "";
            displayedFlights = fullFlights;
            log.info("Return/Clear clicked → showing all {} flights", fullFlights.size());
            log.info("Cleared airline filter, showing all flights: {} total", fullFlights.size());
         }
         // Chỉ lọc nếu có airline VÀ KHÔNG PHẢI clear
         else if (!effectiveAirline.isEmpty()) {
            String search = effectiveAirline.toLowerCase();
            displayedFlights = fullFlights.stream()
                  .filter(f -> f.getName() != null && f.getName().toLowerCase().contains(search))
                  .collect(Collectors.toList());
            log.info("Filtered for '{}': {} flights (full: {})", effectiveAirline, displayedFlights.size(),
                  fullFlights.size());
         }

         // Gợi ý hãng từ full
         List<String> airlineNames = fullFlights.stream()
               .map(FlightData::getName)
               .filter(StringUtils::isNotBlank)
               .distinct()
               .sorted()
               .collect(Collectors.toList());

         // Lấy lịch sử
         List<AirlineFlightSnapshot> snapshots = snapshotService.getSnapshotsForAirline(effectiveAirline);

         // Tạo map đã group theo ngày
         Map<LocalDate, List<AirlineFlightSnapshot>> groupedSnapshots = snapshots.stream()
               .collect(Collectors.groupingBy(AirlineFlightSnapshot::getSnapshotDate));

         // Sắp xếp theo ngày giảm dần (mới nhất trước)
         Map<LocalDate, List<AirlineFlightSnapshot>> sortedGrouped = groupedSnapshots.entrySet().stream()
               .sorted(Map.Entry.<LocalDate, List<AirlineFlightSnapshot>>comparingByKey().reversed())
               .collect(Collectors.toMap(
                     Map.Entry::getKey,
                     Map.Entry::getValue,
                     (e1, e2) -> e1, LinkedHashMap::new));

         // Merge nếu có airline (sau clear thì không merge)
         if (!effectiveAirline.isEmpty()) {
            displayedFlights = snapshotService.mergeWithHistorical(displayedFlights, snapshots, effectiveAirline);
         }

         // Thống kê
         Map<String, Long> statusCount = displayedFlights.stream()
               .filter(f -> f.getStatus() != null)
               .collect(Collectors.groupingBy(FlightData::getStatus, Collectors.counting()));

         // SAVE
         if ("save".equals(action) && StringUtils.isNotBlank(effectiveAirline)) {
            snapshotService.saveOrUpdateSnapshot(effectiveAirline, displayedFlights);
            redirectAttributes.addFlashAttribute("saveMessage",
                  "Đã lưu/cập nhật " + displayedFlights.size() + " chuyến bay cho hãng " + effectiveAirline
                        + " hôm nay!");
            log.info("Saved snapshot for airline: {} ({} flights)", effectiveAirline, displayedFlights.size());

            return "redirect:/?airline=" + effectiveAirline + "&tab=live";
         }

         // Format
         displayedFlights.forEach(flight -> {
            if (flight.getUpdated() != null) {
               java.util.Date date = new java.util.Date(flight.getUpdated());
               java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM HH:mm");
               flight.setUpdatedFormatted(sdf.format(date));
            } else {
               flight.setUpdatedFormatted("-");
            }
            if (flight.getSnapshotDateFormatted() == null) {
               flight.setSnapshotDateFormatted("Today");
            }
         });

         model.addAttribute("flights", displayedFlights);
         model.addAttribute("groupedHistoricalSnapshots", sortedGrouped);
         model.addAttribute("historicalSnapshots", snapshots);
         model.addAttribute("searchAirline", searchAirline); // dùng searchAirline để hiển thị input đúng
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