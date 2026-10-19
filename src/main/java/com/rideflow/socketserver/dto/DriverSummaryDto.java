package com.rideflow.socketserver.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DriverSummaryDto {
    private Long id;
    private String driverName;
}
