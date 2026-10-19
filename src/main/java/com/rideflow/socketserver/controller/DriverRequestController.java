package com.rideflow.socketserver.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;



import com.rideflow.socketserver.Producers.KafkaProducerService;
import com.rideflow.socketserver.dto.RideAcceptanceResponseDto;
import com.rideflow.socketserver.dto.RideRequestDto;
import com.rideflow.socketserver.dto.RideResponseDto;
import com.rideflow.socketserver.dto.UpdateBookingRequestDto;
import com.rideflow.socketserver.dto.UpdateBookingResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import com.netflix.discovery.EurekaClient;
import org.springframework.web.client.RestTemplate;


@Tag(name = "Socket REST bridge")
@RestController
@RequestMapping("/api/socket")
public class DriverRequestController {

    private final SimpMessagingTemplate simpMessagingTemplate;
    private final RestTemplate restTemplate;

    private final EurekaClient eurekaClient;
    private final KafkaProducerService kafkaProducerService;

    public DriverRequestController(
            SimpMessagingTemplate simpMessagingTemplate,
            EurekaClient eurekaClient,
            KafkaProducerService kafkaProducerService) {
        this.simpMessagingTemplate = simpMessagingTemplate;
        this.restTemplate = new RestTemplate();
        this.eurekaClient = eurekaClient;
        this.kafkaProducerService = kafkaProducerService;
    }

    @Operation(operationId = "SocketServer_help", summary = "Publish the Kafka smoke-test message",
            description = "Checks the explicit Kafka producer/consumer demo before combining it with booking messages. DriverRequestController.help -> KafkaProducerService.publishMessage -> KafkaTemplate.send(\"sample-topic\", \"Hello\"). KafkaConsumerService and KafkaConsumerService1 log messages using sample-group and sample-group-2. This GET has a side effect: every call publishes a message. It is not a health endpoint and does not query a database.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Publish the Kafka smoke-test message completed on the controller success branch.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Boolean.class), examples = @ExampleObject(value = "true"))),
            @ApiResponse(responseCode = "500", description = "Synchronous producer failure or unhandled error; asynchronous failure can occur after 200.",
                    content = @Content)
    })
    @GetMapping
    public Boolean help() {
        return true;
    }

    @Operation(operationId = "SocketServer_raiseRideRequest", summary = "Broadcast a ride request to subscribers",
            description = "Tests the REST-to-STOMP bridge that the distributed booking service invokes. DriverRequestController.raiseRideRequest -> sendDriversNewRideRequest -> SimpMessagingTemplate.convertAndSend(\"/topic/rideRequest\", requestDto). Returns Boolean.TRUE immediately. driverIds is not used for targeting. This call broadcasts to all subscribers and performs no booking/passenger/driver lookup; it does not create a booking or publish Kafka itself.")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true,
            description = "JSON body is required by Spring MVC. Field descriptions distinguish service requirements from active validation.",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = com.rideflow.socketserver.dto.RideRequestDto.class),
                    examples = @ExampleObject(value = "{\n  \"passengerId\": 101,\n  \"driverIds\": [\n    201\n  ],\n  \"bookingId\": 401\n}")))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Broadcast a ride request to subscribers completed on the controller success branch.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Boolean.class), examples = @ExampleObject(value = "true"))),
            @ApiResponse(responseCode = "400", description = "Unreadable request JSON before invocation.",
                    content = @Content),
            @ApiResponse(responseCode = "500", description = "Unhandled messaging error; no custom error DTO.",
                    content = @Content)
    })
    @PostMapping ("/newride")
    public ResponseEntity<Boolean> raiseRideRequest(@Valid @RequestBody RideRequestDto requestDto) {
        sendDriversNewRideRequest(requestDto);
        return ResponseEntity.ok(Boolean.TRUE);
    }

    public void sendDriversNewRideRequest(RideRequestDto requestDto) {
        if (requestDto.getDriverIds() == null || requestDto.getDriverIds().isEmpty()) {
            throw new IllegalArgumentException("At least one driver is required");
        }

        // Each driver listens to a topic containing its own ID. A ride request
        // therefore reaches only the nearby drivers selected by Location-Service.
        requestDto.getDriverIds().forEach(driverId ->
                simpMessagingTemplate.convertAndSend(
                        "/topic/rideRequest/" + driverId,
                        requestDto));
    }

    @MessageMapping("/rideResponse/{userId}")
    @SendToUser(value = "/queue/rideResponse", broadcast = false)
    public synchronized RideAcceptanceResponseDto rideResponseHandler(@DestinationVariable String userId, RideResponseDto rideResponseDto) {

        System.out.println(rideResponseDto.getResponse() + " " + userId);
        UpdateBookingRequestDto requestDto = UpdateBookingRequestDto.builder()
                .driverId(Long.parseLong(userId))
                .status("SCHEDULED")
                .build();
        ResponseEntity<UpdateBookingResponseDto> result = this.restTemplate.postForEntity(
                getBookingServiceUrl() + "/api/v1/booking/" + rideResponseDto.bookingId,
                requestDto,
                UpdateBookingResponseDto.class);
        System.out.println(result.getStatusCode());

        UpdateBookingResponseDto bookingResponse = result.getBody();
        if (bookingResponse == null) {
            throw new IllegalStateException("BookingService returned an empty response");
        }

        RideAcceptanceResponseDto reply = RideAcceptanceResponseDto.builder()
                .bookingId(bookingResponse.getBookingId())
                .driverId(bookingResponse.getDriver() == null
                        ? null
                        : bookingResponse.getDriver().getId())
                .status(bookingResponse.getStatus())
                .build();

        System.out.println(
                "RIDE_REPLY: DTO built for booking "
                        + bookingResponse.getBookingId()
        );

        return reply;
    }

    private String getBookingServiceUrl() {
        return eurekaClient.getNextServerFromEureka("BOOKINGSERVICE", false).getHomePageUrl();
    }
}
