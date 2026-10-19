package com.rideflow.socketserver.dto;

import com.rideflow.rideflowentityservice.models.BookingStatus;
import lombok.*;

import java.util.Optional;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateBookingResponseDto {

    private Long bookingId;
    private BookingStatus status;
    private Optional<DriverSummaryDto> driver;
}
