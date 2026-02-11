package com.example.tour_management.controller;

import com.example.tour_management.dto.booking.BookingRequest;
import com.example.tour_management.dto.booking.BookingResponse;
import com.example.tour_management.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    @Autowired
    private BookingService bookingService;

    @PostMapping
    public ResponseEntity<BookingResponse> create(
            @Valid @RequestBody BookingRequest req,
            Authentication authentication
    ) {
        String email = authentication.getName();
        return ResponseEntity.ok(bookingService.create(req, email));
    }
}
