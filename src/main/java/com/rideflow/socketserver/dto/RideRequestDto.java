package com.rideflow.socketserver.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RideRequestDto {

    @NotNull
    @Schema(description = "Passenger who requested the ride.", requiredMode = Schema.RequiredMode.REQUIRED, example = "101")
    private Long passengerId;

    @NotEmpty
    @Schema(description = "Only these drivers receive the ride request. Each driver subscribes to /topic/rideRequest/{driverId}.", requiredMode = Schema.RequiredMode.REQUIRED, example = "[201, 202]")
    private List<Long> driverIds;

    @NotNull
    @Schema(description = "Booking linked to the ride request.", requiredMode = Schema.RequiredMode.REQUIRED, example = "401")
    private Long bookingId;
}
