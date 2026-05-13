package com.shipfast;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nstech.recruiting.assessment_grader.api.*;
import com.shipfast.domain.DeliveryTrip;
import com.shipfast.domain.TripStatus;
import com.shipfast.domain.Vehicle;
import com.shipfast.dto.CreateTripRequest;
import com.shipfast.repository.DeliveryTripRepository;
import com.shipfast.repository.VehicleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class DeliveryTripIntegrationTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private VehicleRepository vehicleRepository;

  @Autowired private DeliveryTripRepository tripRepository;

  @Autowired private ObjectMapper objectMapper;

  private Vehicle testVehicle;

  @BeforeEach
  void setUp() {
    tripRepository.deleteAll();
    vehicleRepository.deleteAll();

    testVehicle = new Vehicle("ABC-1234", 1000.0, 10.0); // 1000kg capacity, 10km/L
    testVehicle = vehicleRepository.save(testVehicle);
  }

  @Test
  void shouldCreateValidTrip() throws Exception {
    CreateTripRequest request =
        new CreateTripRequest(testVehicle.getId(), "São Paulo", 500.0, 100.0);

    mockMvc
        .perform(
            post("/api/trips")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated());

    assertThat(tripRepository.findAll()).hasSize(1);
    DeliveryTrip trip = tripRepository.findAll().get(0);
    assertThat(trip.getDestination()).isEqualTo("São Paulo");
    assertThat(trip.getStatus()).isEqualTo(TripStatus.PENDING);
  }

  @Test
  void shouldRejectTripExceedingCapacity() throws Exception {
    CreateTripRequest request =
        new CreateTripRequest(
            testVehicle.getId(),
            "Rio de Janeiro",
            1500.0, // Exceeds 1000.0
            200.0);

    mockMvc
        .perform(
            post("/api/trips")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());

    assertThat(tripRepository.findAll()).isEmpty();
  }

  @Test
  void shouldCompleteTripWithCorrectFuelCalculation() throws Exception {
    // First, manually save a pending trip
    DeliveryTrip trip = new DeliveryTrip();
    trip.setVehicle(testVehicle);
    trip.setDestination("Curitiba");
    trip.setCargoWeightKg(300.0);
    trip.setDistanceKm(400.0);
    trip.setStatus(TripStatus.PENDING);
    trip = tripRepository.save(trip);

    mockMvc.perform(post("/api/trips/" + trip.getId() + "/complete")).andExpect(status().isOk());

    DeliveryTrip completedTrip = tripRepository.findById(trip.getId()).orElseThrow();
    assertThat(completedTrip.getStatus()).isEqualTo(TripStatus.COMPLETED);

    // Calculation: 400km / 10km/L = 40.0L
    assertThat(completedTrip.getFuelConsumptionLiters()).isEqualTo(40.0);
  }

  @Test
  void exerciseGrader() {
    // Grade assessment
    // Don't remove this test
    new ExerciseGrader().gradeExercise();
  }
}
