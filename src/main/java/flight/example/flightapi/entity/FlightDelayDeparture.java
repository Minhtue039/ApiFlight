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
@Table(name = "flight_delay_departures", uniqueConstraints = @UniqueConstraint(name = "uk_dep_flight_time", columnNames = {
            "flight_icao", "dep_time_utc" }))
@Getter
@Setter
public class FlightDelayDeparture {

      @Id
      @GeneratedValue(strategy = GenerationType.IDENTITY)
      private Long id;

      @Column(nullable = false, length = 10)
      private String flightIcao; // Ensure this field is correctly defined

      @Column(length = 20)
      private String depTimeUtc; // dùng để unique + so sánh thời gian

      // Các trường chính - copy từ DelayFlightResponse
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
      private Integer depDelayed;
      private String aircraftIcao;

      // thêm trường timestamp lưu thời điểm fetch (giúp biết dữ liệu cũ bao lâu)
      @Column
      private Long fetchedAt = System.currentTimeMillis();
}
