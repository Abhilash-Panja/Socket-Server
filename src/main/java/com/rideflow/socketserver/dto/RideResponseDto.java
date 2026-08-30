package com.rideflow.socketserver.dto;

import lombok.*;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RideResponseDto {

    public Boolean response;
    public Long bookingId;

}
