package flight.example.flightapi.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AirlineResponse {
   @JsonProperty("name")
   private String name;

   @JsonProperty("iata_code")
   private String iataCode;

   @JsonProperty("icao_code")
   private String icaoCode;

   // Getters and Setters
   public String getName() {
      return name;
   }

   public void setName(String name) {
      this.name = name;
   }

   public String getIataCode() {
      return iataCode;
   }

   public void setIataCode(String iataCode) {
      this.iataCode = iataCode;
   }

   public String getIcaoCode() {
      return icaoCode;
   }

   public void setIcaoCode(String icaoCode) {
      this.icaoCode = icaoCode;
   }

}