package flight.example.flightapi.service;

import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import flight.example.flightapi.entity.FlightData;
import flight.example.flightapi.entity.FlightDelayArrival;
import flight.example.flightapi.entity.FlightDelayDeparture;
import io.nats.client.JetStream;
import io.nats.client.JetStreamApiException;
import io.nats.client.api.PublishAck;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Service
public class FlightNatsPublisherService {

   private static final Logger log = LoggerFactory.getLogger(FlightNatsPublisherService.class);

   @Autowired
   private JetStream jetStream;

   private final ObjectMapper objectMapper = new ObjectMapper();

   private static final String SUBJECT_FLIGHT_UPDATE = "flight.data.update";
   private static final String SUBJECT_DELAY_DEPARTURE = "flight.delay.departure";
   private static final String SUBJECT_DELAY_ARRIVAL = "flight.delay.arrival";

   @Async
   public void publishFlightUpdate(FlightData flight) {
      try {
         String json = objectMapper.writeValueAsString(flight);
         PublishAck ack = jetStream.publish(SUBJECT_FLIGHT_UPDATE, json.getBytes());
         log.debug("Published flight update hex={} seq={}", flight.getHex(), ack.getSeqno());
      } catch (JetStreamApiException | IOException | JacksonException e) {
         log.error("Failed to publish flight update hex={}", flight.getHex(), e);
      }
   }

   @Async
   public void publishDelayDeparture(FlightDelayDeparture dep) {
      try {
         String json = objectMapper.writeValueAsString(dep);
         jetStream.publish(SUBJECT_DELAY_DEPARTURE, json.getBytes());
         log.debug("Published delay departure flightIcao={}", dep.getFlightIcao());
      } catch (JetStreamApiException | IOException | JacksonException e) {
         log.error("Failed to publish delay departure", e);
      }
   }

   @Async
   public void publishDelayArrival(FlightDelayArrival arr) {
      try {
         String json = objectMapper.writeValueAsString(arr);
         jetStream.publish(SUBJECT_DELAY_ARRIVAL, json.getBytes());
         log.debug("Published delay arrival flightIcao={}", arr.getFlightIcao());
      } catch (JetStreamApiException | IOException | JacksonException e) {
         log.error("Failed to publish delay arrival", e);
      }
   }

   // Nếu muốn publish batch (list) để tối ưu
   @Async
   public void publishFlightBatch(List<FlightData> flights) {
      for (FlightData f : flights) {
         publishFlightUpdate(f); // Hoặc dùng publishAsync nếu high volume
      }
   }
}