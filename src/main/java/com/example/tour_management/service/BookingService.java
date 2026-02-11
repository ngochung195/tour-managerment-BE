package com.example.tour_management.service;

import com.example.tour_management.dto.booking.BookingRequest;
import com.example.tour_management.dto.booking.BookingResponse;
import com.example.tour_management.entity.Booking;
import com.example.tour_management.entity.Tour;
import com.example.tour_management.entity.User;
import com.example.tour_management.enums.BookingStatus;
import com.example.tour_management.exception.NotFoundException;
import com.example.tour_management.repository.BookingRepository;
import com.example.tour_management.repository.TourRepository;
import com.example.tour_management.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class BookingService {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TourRepository tourRepository;

    public List<BookingResponse> getAll() {
        return bookingRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public BookingResponse getById(Integer id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Booking not found"));
        return toResponse(booking);
    }

    public BookingResponse create(BookingRequest req, String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found"));

        Tour tour = tourRepository.findById(req.getTourId())
                .orElseThrow(() -> new NotFoundException("Tour not found"));

        int quantity = req.getQuantity();

        if (tour.getQuantity() < quantity) {
            throw new RuntimeException("Not enough tour quantity");
        }

        tour.setQuantity(tour.getQuantity() - quantity);
        tourRepository.save(tour);

        Booking booking = new Booking();
        booking.setBookingCode(UUID.randomUUID().toString());
        booking.setUser(user);
        booking.setTour(tour);
        booking.setQuantity(quantity);
        booking.setTotal(
                tour.getPrice().multiply(
                        java.math.BigDecimal.valueOf(quantity)
                )
        );
        booking.setStatus(BookingStatus.PENDING);
        booking.setBookingDate(LocalDateTime.now());

        Booking saved = bookingRepository.save(booking);
        return toResponse(saved);
    }

    public BookingResponse updateStatus(Integer id, String status) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Booking not found"));

        try {
            booking.setStatus(BookingStatus.valueOf(status));
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid booking status: " + status);
        }

        return toResponse(bookingRepository.save(booking));
    }

    public void delete(Integer id) {
        if (!bookingRepository.existsById(id)) {
            throw new NotFoundException("Booking not found");
        }
        bookingRepository.deleteById(id);
    }

    private BookingResponse toResponse(Booking b) {
        BookingResponse res = new BookingResponse();
        res.setId(b.getId());
        res.setUserId(b.getUser().getId());
        res.setTourId(b.getTour().getId());
        res.setTotal(b.getTotal());
        res.setStatus(b.getStatus().name());
        res.setBookingDate(b.getBookingDate());
        return res;
    }
}
