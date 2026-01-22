package flight.example.flightapi.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

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

   @Column
   private String flightIcao;

   @Column
   private String flightIata;

   @Column
   private String status;

   @Column(columnDefinition = "TEXT")
   private String flightJson;

   @Column
   private Long fetchedAt;

   @Column
   private Long updatedAt; // Để track thời điểm update cuối cùng
}