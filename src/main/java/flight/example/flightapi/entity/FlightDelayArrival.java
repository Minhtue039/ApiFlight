package flight.example.flightapi.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "flight_delay_arrivals", uniqueConstraints = @UniqueConstraint(name = "uk_arr_flight_time", columnNames = {
      "flight_icao", "dep_time_utc" }))
@Getter
@Setter
public class FlightDelayArrival {

   @Id
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   private Long id;

   @Column(nullable = false, length = 10)
   private String flightIcao;

   @Column(length = 20)
   private String depTimeUtc;

   // Giống hệt các trường ở trên
   private String airlineIata;
   private String airlineIcao;
   private String flightIata;
   private String flightNumber;
   private String depIata;
   private String depIcao;
   private String depTerminal;
   private String depGate;
   private String depTime;
   private String depEstimated;
   private String depActual;
   private String arrIata;
   private String arrIcao;
   private String arrTerminal;
   private String arrGate;
   private String arrTime;
   private String arrEstimated;
   private String arrActual;
   private String status;
   private Integer duration;
   private Integer delayed;
   private Integer arrDelayed;
   private String aircraftIcao;

   @Column
   private Long fetchedAt = System.currentTimeMillis();
}
