package flight.example.flightapi.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import flight.example.flightapi.dto.AirlineResponse;
import flight.example.flightapi.dto.FlightResponse;
import flight.example.flightapi.entity.FlightData;
import io.micrometer.common.util.StringUtils;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Service
public class DataFetchService {

   private static final Logger log = LoggerFactory.getLogger(DataFetchService.class);

   @Autowired
   private RestTemplate restTemplate;

   // @Autowired
   // private FlightDataRepository repository;

   @Autowired
   private FlightPersistenceService persistenceService;

   @Value("${api.flights.url}")
   private String flightsUrl;

   @Value("${api.airlines.url}")
   private String airlinesUrl;

   @Cacheable(value = "liveFlights", key = "#airlineName", condition = "#airlineName != null and !#airlineName.isEmpty()")
   public List<FlightData> fetchLiveFlights(String airlineName) {
      try {
         ObjectMapper mapper = new ObjectMapper();

         // Step1: Call airlines API ->map code to name
         log.info("Fetching airlines data from API: {}" + airlinesUrl);
         String airlinesJson = restTemplate.getForObject(airlinesUrl, String.class);
         JsonNode airlinesRoot = mapper.readTree(airlinesJson);
         List<AirlineResponse> airlines = mapper.convertValue(airlinesRoot.get("response"),
               mapper.getTypeFactory().constructCollectionType(List.class, AirlineResponse.class));

         // Step2: Create a map for quick lookup
         Map<String, String> airlineMap = new HashMap<>();
         for (AirlineResponse airline : airlines) {
            if (airline.getIcaoCode() != null) {
               airlineMap.put(airline.getIcaoCode().toUpperCase(), airline.getName());
            }
            if (airline.getIataCode() != null) {
               airlineMap.put(airline.getIataCode().toUpperCase(), airline.getName());
            }
         }

         // Step3: Call flights API
         log.info("Fetching flights data from API: {}" + flightsUrl);
         String flightsJson = restTemplate.getForObject(flightsUrl, String.class);
         JsonNode flightsRoot = mapper.readTree(flightsJson);
         List<FlightResponse> responses = mapper.convertValue(flightsRoot.get("response"),
               mapper.getTypeFactory().constructCollectionType(List.class, FlightResponse.class));

         // Step4: avoid duplicated in batch ,map name
         List<FlightData> flights = new ArrayList<>();
         Set<String> processedHex = new HashSet<>();

         for (FlightResponse resp : responses) {
            String hex = resp.getHex();
            if (StringUtils.isBlank(hex)) {
               continue;
            }
            hex = hex.trim().toUpperCase(); // normalize hex

            // check duplicate in the same batch and in DB
            if (!processedHex.add(hex)) {
               log.debug("skip duplicate hex in batch: {}", hex);
               continue;
            }

            String name = null;
            String icao = resp.getAirlineIcao();
            String iata = resp.getAirlineIata();
            if (icao != null) {
               name = airlineMap.get(icao.toUpperCase());
            }
            if (name == null && iata != null) {
               name = airlineMap.get(iata.toUpperCase());
            }

            if (name == null) {
               log.debug("Skip flight without airline name: {}", hex);
               continue;
            }
            FlightData data = new FlightData();
            data.setHex(hex);
            data.setRegNumber(resp.getRegNumber());
            data.setFlag(resp.getFlag());
            data.setLat(resp.getLat());
            data.setLng(resp.getLng());
            data.setAlt(resp.getAlt());
            data.setDir(resp.getDir());
            data.setSpeed(resp.getSpeed());
            data.setvSpeed(resp.getvSpeed());
            data.setFlightNumber(resp.getFlightNumber());
            data.setFlightIcao(resp.getFlightIcao());
            data.setFlightIata(resp.getFlightIata());
            data.setDepIcao(resp.getDepIcao());
            data.setDepIata(resp.getDepIata());
            data.setArrIcao(resp.getArrIcao());
            data.setArrIata(resp.getArrIata());
            data.setAirlineIcao(icao);
            data.setAirlineIata(iata);
            data.setAircraftIcao(resp.getAircraftIcao());
            data.setUpdated(resp.getUpdated());
            data.setStatus(resp.getStatus());
            data.setType(resp.getType());
            data.setName(name);

            flights.add(data);
         }

         // if search airlineName -> filter in-memory (ignore case, containing)
         List<FlightData> result = flights;
         if (StringUtils.isNotBlank(airlineName)) {
            String search = airlineName.trim().toLowerCase();
            flights = flights.stream()
                  .filter(f -> f.getName() != null && f.getName().toLowerCase().contains(search))
                  .collect(Collectors.toList());
         }

         persistenceService.saveFlightsAsync(flights);
         log.info("Fetched {} flights from API", flights.size());
         return result;
      } catch (IllegalArgumentException | RestClientException | JacksonException e) {
         log.error("Error fetching or processing flight data", e);
         return Collections.emptyList();
      }

   }

   // public List<String> getAllAirlineNames() {
   // try {
   // ObjectMapper mapper = new ObjectMapper();
   // String json = restTemplate.getForObject(airlinesUrl, String.class);
   // JsonNode root = mapper.readTree(json);
   // List<AirlineResponse> list = mapper.convertValue(root.path("response"),
   // mapper.getTypeFactory().constructCollectionType(List.class,
   // AirlineResponse.class));

   // return list.stream()
   // .map(AirlineResponse::getName)
   // .filter(n -> n != null && !n.isBlank())
   // .distinct()
   // .sorted()
   // .collect(Collectors.toList());
   // } catch (IllegalArgumentException | RestClientException | JacksonException e)
   // {
   // return List.of();
   // }
   // }
}
