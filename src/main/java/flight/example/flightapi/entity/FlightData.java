package flight.example.flightapi.entity;

import org.springframework.data.annotation.Transient;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "flight_data_info")
public class FlightData {
   @Id
   @Column(unique = true, nullable = false, length = 10)
   private String hex;

   @Column
   private Long fetchedAt;

   @PrePersist
   public void onCreate() {
      normalizeHex();
      if (this.fetchedAt == null) {
         this.fetchedAt = System.currentTimeMillis();
      }
   }

   @Transient
   private String updatedFormatted;

   public String getUpdatedFormatted() {
      return updatedFormatted;
   }

   public void setUpdatedFormatted(String updatedFormatted) {
      this.updatedFormatted = updatedFormatted;
   }

   @Transient
   private String snapshotDateFormatted;

   public String getSnapshotDateFormatted() {
      return snapshotDateFormatted;
   }

   public void setSnapshotDateFormatted(String snapshotDateFormatted) {
      this.snapshotDateFormatted = snapshotDateFormatted;
   }

   @PreUpdate
   public void onUpdate() {
      normalizeHex();
      this.fetchedAt = System.currentTimeMillis(); // update timestamp every time entity is modified
   }

   private void normalizeHex() {
      if (this.hex != null) {
         this.hex = this.hex.trim().toUpperCase();
      }
   }

   // public Instant getUpdatedInstant() {
   // return (this.updated != null) ? Instant.ofEpochMilli(this.updated) : null;
   // }

   private String regNumber;
   private String flag;
   private Double lat;
   private Double lng;
   private Integer alt;
   private Double dir;
   private Integer speed;
   private Integer vSpeed;
   private String flightNumber;
   private String flightIcao;
   private String flightIata;
   private String depIcao;
   private String depIata;
   private String arrIcao;
   private String arrIata;
   private String airlineIcao;
   private String airlineIata;
   private String aircraftIcao;
   private Long updated;
   private String status;
   private String type;
   private String name;

   // Getters and Setters
   public Long getFetchedAt() {
      return fetchedAt;
   }

   public void setFetchedAt(Long fetchedAt) {
      this.fetchedAt = fetchedAt;
   }

   public String getHex() {
      return hex;
   }

   public void setHex(String hex) {
      this.hex = hex;
   }

   public String getRegNumber() {
      return regNumber;
   }

   public void setRegNumber(String regNumber) {
      this.regNumber = regNumber;
   }

   public String getFlag() {
      return flag;
   }

   public void setFlag(String flag) {
      this.flag = flag;
   }

   public Double getLat() {
      return lat;
   }

   public void setLat(Double lat) {
      this.lat = lat;
   }

   public Double getLng() {
      return lng;
   }

   public void setLng(Double lng) {
      this.lng = lng;
   }

   public Integer getAlt() {
      return alt;
   }

   public void setAlt(Integer alt) {
      this.alt = alt;
   }

   public Double getDir() {
      return dir;
   }

   public void setDir(Double dir) {
      this.dir = dir;
   }

   public Integer getSpeed() {
      return speed;
   }

   public void setSpeed(Integer speed) {
      this.speed = speed;
   }

   public Integer getvSpeed() {
      return vSpeed;
   }

   public void setvSpeed(Integer vSpeed) {
      this.vSpeed = vSpeed;
   }

   public String getFlightNumber() {
      return flightNumber;
   }

   public void setFlightNumber(String flightNumber) {
      this.flightNumber = flightNumber;
   }

   public String getFlightIcao() {
      return flightIcao;
   }

   public void setFlightIcao(String flightIcao) {
      this.flightIcao = flightIcao;
   }

   public String getFlightIata() {
      return flightIata;
   }

   public void setFlightIata(String flightIata) {
      this.flightIata = flightIata;
   }

   public String getDepIcao() {
      return depIcao;
   }

   public void setDepIcao(String depIcao) {
      this.depIcao = depIcao;
   }

   public String getDepIata() {
      return depIata;
   }

   public void setDepIata(String depIata) {
      this.depIata = depIata;
   }

   public String getArrIcao() {
      return arrIcao;
   }

   public void setArrIcao(String arrIcao) {
      this.arrIcao = arrIcao;
   }

   public String getArrIata() {
      return arrIata;
   }

   public void setArrIata(String arrIata) {
      this.arrIata = arrIata;
   }

   public String getAirlineIcao() {
      return airlineIcao;
   }

   public void setAirlineIcao(String airlineIcao) {
      this.airlineIcao = airlineIcao;
   }

   public String getAirlineIata() {
      return airlineIata;
   }

   public void setAirlineIata(String airlineIata) {
      this.airlineIata = airlineIata;
   }

   public String getAircraftIcao() {
      return aircraftIcao;
   }

   public void setAircraftIcao(String aircraftIcao) {
      this.aircraftIcao = aircraftIcao;
   }

   public Long getUpdated() {
      return updated;
   }

   public void setUpdated(Long updated) {
      this.updated = updated;
   }

   public String getStatus() {
      return status;
   }

   public void setStatus(String status) {
      this.status = status;
   }

   public String getType() {
      return type;
   }

   public void setType(String type) {
      this.type = type;
   }

   public String getName() {
      return name;
   }

   public void setName(String name) {
      this.name = name;
   }

}
