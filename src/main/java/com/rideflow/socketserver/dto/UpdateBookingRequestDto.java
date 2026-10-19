package com.rideflow.socketserver.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateBookingRequestDto {
    private String status;
    private Long driverId;
}
