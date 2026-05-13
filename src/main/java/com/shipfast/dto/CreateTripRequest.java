package com.shipfast.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreateTripRequest(
    @NotNull Long vehicleId,
    @NotBlank String destination,
    @NotNull @Positive Double cargoWeightKg,
    @NotNull @Positive Double distanceKm
) {}
