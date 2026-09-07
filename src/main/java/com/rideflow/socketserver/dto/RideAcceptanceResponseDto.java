package com.rideflow.socketserver.dto;

import com.rideflow.rideflowentityservice.models.BookingStatus;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RideAcceptanceResponseDto {

    private Long bookingId;
    private Long driverId;
    private BookingStatus status;
}
