package flight.example.flightapi.entity;

import java.time.LocalDate;

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
@Table(name = "flight_day", uniqueConstraints = @UniqueConstraint(columnNames = { "airline_name", "snapshot_date",
            "hex" }))
@Getter
@Setter
public class AirlineFlightSnapshot {

      @Id
      @GeneratedValue(strategy = GenerationType.IDENTITY)
      private Long id;

      @Column(name = "airline_name", nullable = false)
      private String airlineName;

      @Column(name = "snapshot_date", nullable = false)
      private LocalDate snapshotDate;

      @Column(nullable = false)
      private String hex;

      // Các trường chính từ FlightData
      @Column
      private String regNumber;

      @Column
      private String flag;

      @Column
      private Double lat;

      @Column
      private Double lng;

      @Column
      private Integer alt;

      @Column
      private Integer speed;

      @Column
      private String flightIcao;

      @Column
      private String flightIata;

      @Column
      private String depIata;

      @Column
      private String arrIata;

      @Column
      private String status;

      @Column
      private Long fetchedAt;

      @Column
      private Long updatedAt;

      @Column
      private String aircraftIcao;

      @Column(columnDefinition = "TEXT")
      private String flightJson;
      // ... thêm các trường khác nếu cần
}