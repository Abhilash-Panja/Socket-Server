package com.rideflow.socketserver.configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(info = @Info(
        title = "RideFlow Socket REST API",
        version = "0.0.1-SNAPSHOT",
        description = "REST bridge for ride broadcasts and a Kafka smoke test. STOMP destination /app/rideResponse/{userId} and topic /topic/rideRequest are messaging contracts, not REST paths; see POSTMAN_API_GUIDE.md."
))
public class OpenApiConfig {
}
