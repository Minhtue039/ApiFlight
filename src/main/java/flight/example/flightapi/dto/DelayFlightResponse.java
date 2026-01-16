package flight.example.flightapi.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class DelayFlightResponse {
   @JsonProperty("airline_iata")
   private String airlineIata;

   @JsonProperty("airline_icao")
   private String airlineIcao;

   @JsonProperty("flight_iata")
   private String flightIata;

   @JsonProperty("flight_icao")
   private String flightIcao;

   @JsonProperty("flight_number")
   private String flightNumber;

   @JsonProperty("dep_iata")
   private String depIata;

   @JsonProperty("dep_icao")
   private String depIcao;

   @JsonProperty("dep_terminal")
   private String depTerminal;

   @JsonProperty("dep_gate")
   private String depGate;

   @JsonProperty("dep_time")
   private String depTime;

   @JsonProperty("dep_time_utc")
   private String depTimeUtc;

   @JsonProperty("dep_estimated")
   private String depEstimated;

   @JsonProperty("dep_estimated_utc")
   private String depEstimatedUtc;

   @JsonProperty("dep_actual")
   private String depActual;

   @JsonProperty("dep_actual_utc")
   private String depActualUtc;

   @JsonProperty("arr_iata")
   private String arrIata;

   @JsonProperty("arr_icao")
   private String arrIcao;

   @JsonProperty("arr_terminal")
   private String arrTerminal;

   @JsonProperty("arr_gate")
   private String arrGate;

   @JsonProperty("arr_baggage")
   private String arrBaggage;

   @JsonProperty("arr_time")
   private String arrTime;

   @JsonProperty("arr_time_utc")
   private String arrTimeUtc;

   @JsonProperty("arr_estimated")
   private String arrEstimated;

   @JsonProperty("arr_estimated_utc")
   private String arrEstimatedUtc;

   @JsonProperty("arr_actual")
   private String arrActual;

   @JsonProperty("arr_actual_utc")
   private String arrActualUtc;

   @JsonProperty("cs_airline_iata")
   private String csAirlineIata;

   @JsonProperty("cs_flight_number")
   private String csFlightNumber;

   @JsonProperty("cs_flight_iata")
   private String csFlightIata;

   @JsonProperty("status")
   private String status;

   @JsonProperty("duration")
   private Integer duration;

   @JsonProperty("delayed")
   private Integer delayed;

   @JsonProperty("dep_delayed")
   private Integer depDelayed;

   @JsonProperty("arr_delayed")
   private Integer arrDelayed;

   @JsonProperty("aircraft_icao")
   private String aircraftIcao;

}
