package flight.example.flightapi.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class FlightResponse {
   @JsonProperty("hex")
   private String hex;

   @JsonProperty("reg_number")
   private String regNumber;

   @JsonProperty("flag")
   private String flag;

   @JsonProperty("lat")
   private Double lat;

   @JsonProperty("lng")
   private Double lng;

   @JsonProperty("alt")
   private Integer alt;

   @JsonProperty("dir")
   private Double dir;

   @JsonProperty("speed")
   private Integer speed;

   @JsonProperty("v_speed")
   private Integer vSpeed;

   @JsonProperty("flight_number")
   private String flightNumber;

   @JsonProperty("flight_icao")
   private String flightIcao;

   @JsonProperty("flight_iata")
   private String flightIata;

   @JsonProperty("dep_icao")
   private String depIcao;

   @JsonProperty("dep_iata")
   private String depIata;

   @JsonProperty("arr_icao")
   private String arrIcao;

   @JsonProperty("arr_iata")
   private String arrIata;

   @JsonProperty("airline_icao")
   private String airlineIcao;

   @JsonProperty("airline_iata")
   private String airlineIata;

   @JsonProperty("aircraft_icao")
   private String aircraftIcao;

   @JsonProperty("updated")
   private Long updated;

   @JsonProperty("status")
   private String status;

   @JsonProperty("type")
   private String type;

   // Getters and Setters
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

}
