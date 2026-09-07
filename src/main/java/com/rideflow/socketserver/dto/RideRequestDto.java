package com.rideflow.socketserver.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RideRequestDto {

    @Schema(description = "Broadcast unchanged; not validated and not looked up in a database.", requiredMode = Schema.RequiredMode.NOT_REQUIRED, example = "101")
    private Long passengerId;

//    private ExactLocation startLocation;
//
//    private ExactLocation endLocation;

    @Schema(description = "Accepted but not used to select recipients. Broadcast goes to every /topic/rideRequest subscriber.", requiredMode = Schema.RequiredMode.NOT_REQUIRED, example = "[201]")
    private List<Long> driverIds;

    @Schema(description = "Broadcast unchanged. Clients need a real booking ID for a later ride response, but this REST operation does not check it.", requiredMode = Schema.RequiredMode.NOT_REQUIRED, example = "401")
    private Long bookingId;
}


