package flight.example.flightapi.service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import flight.example.flightapi.dto.DelayFlightResponse;
import flight.example.flightapi.entity.FlightDelayArrival;
import flight.example.flightapi.entity.FlightDelayDeparture;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.exc.JsonNodeException;

@Service
public class DelayFlightService {

   @Autowired
   private FlightNatsPublisherService natsPublisher;

   private static final Logger log = LoggerFactory.getLogger(DelayFlightService.class);

   private final RestTemplate restTemplate;
   private final ObjectMapper objectMapper;
   private final DelayPersistenceService delayPersistenceService;

   @Value("${api.delays.url}")
   private String delaysUrlTemplate;

   public DelayFlightService(
         RestTemplate restTemplate,
         ObjectMapper objectMapper,
         DelayPersistenceService delayPersistenceService) {
      this.restTemplate = restTemplate;
      this.objectMapper = objectMapper;
      this.delayPersistenceService = delayPersistenceService;
   }

   /**
    * Fetch delayed flights từ Airlabs API cho loại departures hoặc arrivals.
    * Sau khi fetch thành công, sẽ map sang entity và lưu async vào database.
    *
    * @param type "departures" hoặc "arrivals"
    * @return danh sách DelayFlightResponse (dùng để hiển thị ngay lập tức)
    */

   @Cacheable(value = "delayedFlights", key = "#type")
   public List<DelayFlightResponse> fetchDelayedFlights(String type) {
      if (!"departures".equals(type) && !"arrivals".equals(type)) {
         log.warn("Invalid delay type: {}. Must be 'departures' or 'arrivals'", type);
         return Collections.emptyList();
      }

      String url = delaysUrlTemplate.replace("{type}", type);
      log.info("Fetching delayed flights (type={}) from URL: {}", type, url);

      try {
         // Gọi API
         String jsonResponse = restTemplate.getForObject(url, String.class);

         // Parse JSON
         JsonNode root = objectMapper.readTree(jsonResponse);
         JsonNode responseNode = root.path("response");

         if (!responseNode.isArray()) {
            log.warn("Unexpected response format from Airlabs delays API (type={})", type);
            return Collections.emptyList();
         }

         // Chuyển thành List<DelayFlightResponse>
         List<DelayFlightResponse> delayedFlights = objectMapper.convertValue(
               responseNode,
               objectMapper.getTypeFactory().constructCollectionType(List.class, DelayFlightResponse.class));

         log.info("Successfully fetched {} delayed {} flights from Airlabs API",
               delayedFlights.size(), type);

         // Map sang entity và lưu async
         if ("departures".equals(type)) {
            List<FlightDelayDeparture> entities = delayedFlights.stream()
                  .map(this::mapToDepartureEntity)
                  .filter(e -> e.getFlightIcao() != null && e.getDepTimeUtc() != null) // lọc record thiếu key
                  .collect(Collectors.toList());

            for (FlightDelayDeparture entity : entities) {
               natsPublisher.publishDelayDeparture(entity);
            }
            if (!entities.isEmpty()) {
               delayPersistenceService.saveDeparturesAsync(entities);
            }
         } else { // arrivals
            List<FlightDelayArrival> entities = delayedFlights.stream()
                  .map(this::mapToArrivalEntity)
                  .filter(e -> e.getFlightIcao() != null && e.getDepTimeUtc() != null)
                  .collect(Collectors.toList());
            for (FlightDelayArrival entity : entities) {
               natsPublisher.publishDelayArrival(entity);
            }
            if (!entities.isEmpty()) {
               delayPersistenceService.saveArrivalsAsync(entities);
            }
         }

         return delayedFlights;

      } catch (RestClientException e) {
         log.error("HTTP error when calling Airlabs delays API (type={}): {}", type, e.getMessage(), e);
         return Collections.emptyList();
      } catch (JsonNodeException e) {
         log.error("JSON parsing error from Airlabs delays response (type={}): {}", type, e.getMessage(), e);
         return Collections.emptyList();
      } catch (IllegalArgumentException | JacksonException e) {
         log.error("Unexpected error while fetching/processing delayed {} flights", type, e);
         return Collections.emptyList();
      }
   }

   // -------------------------------------------------------------------------
   // Mapping DTO → Entity Departure
   // -------------------------------------------------------------------------
   private FlightDelayDeparture mapToDepartureEntity(DelayFlightResponse dto) {
      FlightDelayDeparture entity = new FlightDelayDeparture();

      entity.setFlightIcao(dto.getFlightIcao());
      entity.setDepTimeUtc(dto.getDepTimeUtc());

      entity.setAirlineIata(dto.getAirlineIata());
      entity.setAirlineIcao(dto.getAirlineIcao());
      entity.setFlightIata(dto.getFlightIata());
      entity.setFlightNumber(dto.getFlightNumber());
      entity.setDepIata(dto.getDepIata());
      entity.setDepIcao(dto.getDepIcao());
      entity.setDepTerminal(dto.getDepTerminal());
      entity.setDepGate(dto.getDepGate());
      entity.setDepTime(dto.getDepTime());
      entity.setDepEstimated(dto.getDepEstimated());
      entity.setDepActual(dto.getDepActual());

      entity.setArrIata(dto.getArrIata());
      entity.setArrIcao(dto.getArrIcao());
      entity.setArrTerminal(dto.getArrTerminal());
      entity.setArrGate(dto.getArrGate());
      entity.setArrTime(dto.getArrTime());
      entity.setArrEstimated(dto.getArrEstimated());
      entity.setArrActual(dto.getArrActual());

      entity.setStatus(dto.getStatus());
      entity.setDuration(dto.getDuration());
      entity.setDelayed(dto.getDelayed());
      entity.setDepDelayed(dto.getDepDelayed());
      entity.setAircraftIcao(dto.getAircraftIcao());

      // fetchedAt sẽ được set trong persistence layer khi save/update
      return entity;
   }

   // -------------------------------------------------------------------------
   // Mapping DTO → Entity Arrival
   // -------------------------------------------------------------------------
   private FlightDelayArrival mapToArrivalEntity(DelayFlightResponse dto) {
      FlightDelayArrival entity = new FlightDelayArrival();

      entity.setFlightIcao(dto.getFlightIcao());
      entity.setDepTimeUtc(dto.getDepTimeUtc());

      entity.setAirlineIata(dto.getAirlineIata());
      entity.setAirlineIcao(dto.getAirlineIcao());
      entity.setFlightIata(dto.getFlightIata());
      entity.setFlightNumber(dto.getFlightNumber());
      entity.setDepIata(dto.getDepIata());
      entity.setDepIcao(dto.getDepIcao());
      entity.setDepTerminal(dto.getDepTerminal());
      entity.setDepGate(dto.getDepGate());
      entity.setDepTime(dto.getDepTime());
      entity.setDepEstimated(dto.getDepEstimated());
      entity.setDepActual(dto.getDepActual());

      entity.setArrIata(dto.getArrIata());
      entity.setArrIcao(dto.getArrIcao());
      entity.setArrTerminal(dto.getArrTerminal());
      entity.setArrGate(dto.getArrGate());
      entity.setArrTime(dto.getArrTime());
      entity.setArrEstimated(dto.getArrEstimated());
      entity.setArrActual(dto.getArrActual());

      entity.setStatus(dto.getStatus());
      entity.setDuration(dto.getDuration());
      entity.setDelayed(dto.getDelayed());
      entity.setArrDelayed(dto.getArrDelayed());
      entity.setAircraftIcao(dto.getAircraftIcao());

      return entity;
   }
}