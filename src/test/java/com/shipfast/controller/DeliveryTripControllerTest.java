package com.shipfast.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shipfast.domain.DeliveryTrip;
import com.shipfast.domain.TripStatus;
import com.shipfast.domain.Vehicle;
import com.shipfast.dto.CreateTripRequest;
import com.shipfast.exception.GlobalExceptionHandler;
import com.shipfast.exception.InsufficientCapacityException;
import com.shipfast.exception.TripAlreadyCompletedException;
import com.shipfast.service.DeliveryTripService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Slice de teste para a camada Web (Controller + ExceptionHandler).
 * Carrega apenas o contexto web do Spring: sem JPA, sem RabbitMQ, sem OTLP.
 * Boot esperado: ~2-3s. O servico e mockado via @MockBean.
 */
@WebMvcTest(controllers = {DeliveryTripController.class, GlobalExceptionHandler.class})
@DisplayName("DeliveryTripController - Slice @WebMvcTest")
class DeliveryTripControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private DeliveryTripService tripService;

    private Vehicle vehicle;
    private DeliveryTrip pendingTrip;

    @BeforeEach
    void setUp() {
        vehicle = new Vehicle("ABC-1234", 1000.0, 10.0);
        vehicle.setId(1L);

        pendingTrip = new DeliveryTrip();
        pendingTrip.setId(1L);
        pendingTrip.setVehicle(vehicle);
        pendingTrip.setDestination("Sao Paulo");
        pendingTrip.setCargoWeightKg(500.0);
        pendingTrip.setDistanceKm(200.0);
        pendingTrip.setStatus(TripStatus.PENDING);
    }

    @Nested
    @DisplayName("POST /api/trips - Criar Viagem")
    class CreateTripEndpointTests {

        @Test
        @DisplayName("deve retornar 201 Created com corpo da viagem ao criar com sucesso")
        void shouldReturn201WhenTripIsCreated() throws Exception {
            CreateTripRequest request = new CreateTripRequest(1L, "Sao Paulo", 500.0, 200.0);
            when(tripService.createTrip(any())).thenReturn(pendingTrip);

            mockMvc.perform(post("/api/trips")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(1L))
                    .andExpect(jsonPath("$.status").value("PENDING"))
                    .andExpect(jsonPath("$.destination").value("Sao Paulo"));
        }

        @Test
        @DisplayName("deve retornar 400 Bad Request quando carga excede capacidade do veiculo")
        void shouldReturn400WhenCargoExceedsCapacity() throws Exception {
            CreateTripRequest request = new CreateTripRequest(1L, "Rio", 9999.0, 100.0);
            when(tripService.createTrip(any()))
                    .thenThrow(new InsufficientCapacityException("Cargo weight 9999.0kg exceeds max capacity of 1000.0kg"));

            mockMvc.perform(post("/api/trips")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400));
        }

        @Test
        @DisplayName("deve retornar 404 Not Found quando veiculo nao existe")
        void shouldReturn404WhenVehicleNotFound() throws Exception {
            CreateTripRequest request = new CreateTripRequest(99L, "Rio", 100.0, 100.0);
            when(tripService.createTrip(any()))
                    .thenThrow(new EntityNotFoundException("Vehicle not found with id: 99"));

            mockMvc.perform(post("/api/trips")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404));
        }

        @Test
        @DisplayName("deve retornar 400 Bad Request com erros de validacao quando payload invalido")
        void shouldReturn400WithValidationErrorsWhenPayloadIsInvalid() throws Exception {
            // destination em branco e cargoWeightKg null violam as constraints do record
            String invalidJson = """
                    {"vehicleId": 1, "destination": "", "cargoWeightKg": null, "distanceKm": 100.0}
                    """;

            mockMvc.perform(post("/api/trips")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(invalidJson))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors").exists());
        }

        @Test
        @DisplayName("deve retornar 400 Bad Request quando vehicleId e nulo")
        void shouldReturn400WhenVehicleIdIsNull() throws Exception {
            String invalidJson = """
                    {"vehicleId": null, "destination": "Rio", "cargoWeightKg": 100.0, "distanceKm": 100.0}
                    """;

            mockMvc.perform(post("/api/trips")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(invalidJson))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("POST /api/trips/{id}/complete - Completar Viagem")
    class CompleteTripEndpointTests {

        @Test
        @DisplayName("deve retornar 200 OK com viagem completada e consumo calculado")
        void shouldReturn200WithCompletedTrip() throws Exception {
            DeliveryTrip completed = new DeliveryTrip();
            completed.setId(1L);
            completed.setVehicle(vehicle);
            completed.setDestination("Sao Paulo");
            completed.setDistanceKm(200.0);
            completed.setCargoWeightKg(500.0);
            completed.setStatus(TripStatus.COMPLETED);
            completed.setFuelConsumptionLiters(20.0);

            when(tripService.completeTrip(eq(1L))).thenReturn(completed);

            mockMvc.perform(post("/api/trips/1/complete"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("COMPLETED"))
                    .andExpect(jsonPath("$.fuelConsumptionLiters").value(20.0));
        }

        @Test
        @DisplayName("deve retornar 404 Not Found quando viagem nao existe")
        void shouldReturn404WhenTripNotFound() throws Exception {
            when(tripService.completeTrip(eq(99L)))
                    .thenThrow(new EntityNotFoundException("Trip not found with id: 99"));

            mockMvc.perform(post("/api/trips/99/complete"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("Trip not found with id: 99"));
        }

        @Test
        @DisplayName("deve retornar 409 Conflict quando viagem ja esta concluida")
        void shouldReturn409WhenTripAlreadyCompleted() throws Exception {
            when(tripService.completeTrip(eq(1L)))
                    .thenThrow(new TripAlreadyCompletedException("Trip is already completed with id: 1"));

            mockMvc.perform(post("/api/trips/1/complete"))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.status").value(409));
        }
    }

    @Nested
    @DisplayName("GET /api/trips - Listar Viagens")
    class ListTripsEndpointTests {

        @Test
        @DisplayName("deve retornar 200 OK com lista de viagens")
        void shouldReturn200WithListOfTrips() throws Exception {
            when(tripService.getAllTrips()).thenReturn(List.of(pendingTrip));

            mockMvc.perform(get("/api/trips"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1))
                    .andExpect(jsonPath("$[0].id").value(1L));
        }

        @Test
        @DisplayName("deve retornar 200 OK com lista vazia quando nao ha viagens")
        void shouldReturn200WithEmptyListWhenNoTrips() throws Exception {
            when(tripService.getAllTrips()).thenReturn(List.of());

            mockMvc.perform(get("/api/trips"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(0));
        }
    }
}
