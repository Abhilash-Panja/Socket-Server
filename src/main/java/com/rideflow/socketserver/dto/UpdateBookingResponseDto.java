package com.rideflow.socketserver.dto;

import com.rideflow.rideflowentityservice.models.BookingStatus;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateBookingResponseDto {
    private Long bookingId;
    private BookingStatus status;
    private DriverSummaryDto driver;
}
