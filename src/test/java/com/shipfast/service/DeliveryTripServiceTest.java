package com.shipfast.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shipfast.domain.DeliveryTrip;
import com.shipfast.domain.TripStatus;
import com.shipfast.domain.Vehicle;
import com.shipfast.dto.CreateTripRequest;
import com.shipfast.exception.InsufficientCapacityException;
import com.shipfast.exception.TripAlreadyCompletedException;
import com.shipfast.repository.DeliveryTripRepository;
import com.shipfast.repository.OutboxEventRepository;
import com.shipfast.repository.VehicleRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

/**
 * Testes unitarios do DeliveryTripService.
 * Sem Spring context, sem banco, sem rede. Puro JUnit + Mockito.
 * Objetivo: cobrir todas as regras de negocio com feedback em < 500ms.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("DeliveryTripService - Testes Unitarios")
class DeliveryTripServiceTest {

    @Mock private DeliveryTripRepository tripRepository;
    @Mock private VehicleRepository vehicleRepository;
    @Mock private OutboxEventRepository outboxEventRepository;

    private DeliveryTripService service;
    private Vehicle vehicle;

    @BeforeEach
    void setUp() {
        service = new DeliveryTripService(tripRepository, vehicleRepository, outboxEventRepository, new ObjectMapper());
        vehicle = new Vehicle("ABC-1234", 1000.0, 10.0);
        vehicle.setId(1L);
    }

    @Nested
    @DisplayName("createTrip()")
    class CreateTripTests {

        @Test
        @DisplayName("deve criar viagem com status PENDING quando dados sao validos")
        void shouldCreateTripWithPendingStatus() {
            CreateTripRequest request = new CreateTripRequest(1L, "Sao Paulo", 500.0, 200.0);
            DeliveryTrip saved = buildPersistedTrip(10L, vehicle, TripStatus.PENDING);
            saved.setDestination("Sao Paulo");

            when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));
            when(tripRepository.save(any())).thenReturn(saved);
            when(outboxEventRepository.save(any())).thenReturn(null);

            DeliveryTrip result = service.createTrip(request);

            assertThat(result.getStatus()).isEqualTo(TripStatus.PENDING);
            assertThat(result.getDestination()).isEqualTo("Sao Paulo");
        }

        @Test
        @DisplayName("deve definir status PENDING na entidade passada ao repository")
        void shouldSetPendingStatusOnPersistedEntity() {
            CreateTripRequest request = new CreateTripRequest(1L, "Curitiba", 200.0, 400.0);
            when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));
            when(tripRepository.save(any())).thenAnswer(inv -> {
                DeliveryTrip t = inv.getArgument(0);
                t.setId(1L);
                return t;
            });
            when(outboxEventRepository.save(any())).thenReturn(null);

            ArgumentCaptor<DeliveryTrip> captor = ArgumentCaptor.forClass(DeliveryTrip.class);
            service.createTrip(request);
            verify(tripRepository).save(captor.capture());

            assertThat(captor.getValue().getStatus()).isEqualTo(TripStatus.PENDING);
        }

        @Test
        @DisplayName("deve salvar evento TRIP_CREATED no Outbox ao criar viagem")
        void shouldSaveOutboxEventOnCreate() {
            CreateTripRequest request = new CreateTripRequest(1L, "Campinas", 300.0, 100.0);
            DeliveryTrip saved = buildPersistedTrip(1L, vehicle, TripStatus.PENDING);

            when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));
            when(tripRepository.save(any())).thenReturn(saved);
            when(outboxEventRepository.save(any())).thenReturn(null);

            service.createTrip(request);

            verify(outboxEventRepository, times(1)).save(
                    argThat(e -> "TRIP_CREATED".equals(e.getEventType()))
            );
        }

        @Test
        @DisplayName("deve lancar InsufficientCapacityException quando carga excede capacidade")
        void shouldThrowWhenCargoExceedsCapacity() {
            CreateTripRequest request = new CreateTripRequest(1L, "Belo Horizonte", 1001.0, 300.0);
            when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));

            assertThatThrownBy(() -> service.createTrip(request))
                    .isInstanceOf(InsufficientCapacityException.class)
                    .hasMessageContaining("1001.0kg");

            verify(tripRepository, never()).save(any());
            verify(outboxEventRepository, never()).save(any());
        }

        @Test
        @DisplayName("deve permitir criacao quando carga e exatamente igual a capacidade maxima")
        void shouldAllowTripWhenCargoEqualsMaxCapacity() {
            CreateTripRequest request = new CreateTripRequest(1L, "Porto Alegre", 1000.0, 500.0);
            DeliveryTrip saved = buildPersistedTrip(2L, vehicle, TripStatus.PENDING);

            when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));
            when(tripRepository.save(any())).thenReturn(saved);
            when(outboxEventRepository.save(any())).thenReturn(null);

            DeliveryTrip result = service.createTrip(request);

            assertThat(result).isNotNull();
            verify(tripRepository, times(1)).save(any());
        }

        @Test
        @DisplayName("deve lancar EntityNotFoundException quando veiculo nao existe")
        void shouldThrowWhenVehicleNotFound() {
            CreateTripRequest request = new CreateTripRequest(99L, "Fortaleza", 100.0, 200.0);
            when(vehicleRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.createTrip(request))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("completeTrip()")
    class CompleteTripTests {

        @Test
        @DisplayName("deve calcular consumo corretamente: distancia / eficiencia")
        void shouldCalculateFuelConsumptionCorrectly() {
            DeliveryTrip trip = buildPendingTripWithDistance(400.0);
            when(tripRepository.findById(1L)).thenReturn(Optional.of(trip));
            when(tripRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(outboxEventRepository.save(any())).thenReturn(null);

            DeliveryTrip result = service.completeTrip(1L);

            // 400km / 10km/L = 40L
            assertThat(result.getFuelConsumptionLiters()).isEqualTo(40.0);
        }

        @Test
        @DisplayName("deve alterar status para COMPLETED ao concluir")
        void shouldSetStatusToCompleted() {
            DeliveryTrip trip = buildPendingTripWithDistance(200.0);
            when(tripRepository.findById(1L)).thenReturn(Optional.of(trip));
            when(tripRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(outboxEventRepository.save(any())).thenReturn(null);

            DeliveryTrip result = service.completeTrip(1L);

            assertThat(result.getStatus()).isEqualTo(TripStatus.COMPLETED);
        }

        @Test
        @DisplayName("deve salvar evento TRIP_COMPLETED no Outbox")
        void shouldSaveOutboxEventOnComplete() {
            DeliveryTrip trip = buildPendingTripWithDistance(100.0);
            when(tripRepository.findById(1L)).thenReturn(Optional.of(trip));
            when(tripRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(outboxEventRepository.save(any())).thenReturn(null);

            service.completeTrip(1L);

            verify(outboxEventRepository).save(
                    argThat(e -> "TRIP_COMPLETED".equals(e.getEventType()))
            );
        }

        @Test
        @DisplayName("deve lancar TripAlreadyCompletedException ao tentar completar viagem ja concluida")
        void shouldThrowWhenTripAlreadyCompleted() {
            DeliveryTrip trip = buildPendingTripWithDistance(100.0);
            trip.setStatus(TripStatus.COMPLETED);
            trip.setFuelConsumptionLiters(10.0);
            when(tripRepository.findById(1L)).thenReturn(Optional.of(trip));

            assertThatThrownBy(() -> service.completeTrip(1L))
                    .isInstanceOf(TripAlreadyCompletedException.class);

            verify(tripRepository, never()).save(any());
        }

        @Test
        @DisplayName("deve lancar EntityNotFoundException quando viagem nao existe")
        void shouldThrowWhenTripNotFound() {
            when(tripRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.completeTrip(99L))
                    .isInstanceOf(EntityNotFoundException.class);
        }

        @Test
        @DisplayName("deve calcular consumo proporcional para diferentes eficiencias de combustivel")
        void shouldCalculateFuelForDifferentEfficiencies() {
            // Veiculo com 5km/L -> 300km / 5 = 60L
            Vehicle lowEfficiencyVehicle = new Vehicle("XYZ-9999", 2000.0, 5.0);
            lowEfficiencyVehicle.setId(2L);

            DeliveryTrip trip = new DeliveryTrip();
            trip.setId(1L);
            trip.setVehicle(lowEfficiencyVehicle);
            trip.setDistanceKm(300.0);
            trip.setCargoWeightKg(500.0);
            trip.setStatus(TripStatus.PENDING);

            when(tripRepository.findById(1L)).thenReturn(Optional.of(trip));
            when(tripRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(outboxEventRepository.save(any())).thenReturn(null);

            DeliveryTrip result = service.completeTrip(1L);

            assertThat(result.getFuelConsumptionLiters()).isEqualTo(60.0);
        }
    }

    private DeliveryTrip buildPendingTripWithDistance(double distanceKm) {
        DeliveryTrip trip = new DeliveryTrip();
        trip.setId(1L);
        trip.setVehicle(vehicle);
        trip.setDestination("Destino Teste");
        trip.setDistanceKm(distanceKm);
        trip.setCargoWeightKg(300.0);
        trip.setStatus(TripStatus.PENDING);
        return trip;
    }

    private DeliveryTrip buildPersistedTrip(Long id, Vehicle v, TripStatus status) {
        DeliveryTrip trip = new DeliveryTrip();
        trip.setId(id);
        trip.setVehicle(v);
        trip.setDestination("Destino");
        trip.setDistanceKm(100.0);
        trip.setCargoWeightKg(100.0);
        trip.setStatus(status);
        return trip;
    }
}
