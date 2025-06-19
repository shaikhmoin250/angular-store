package com.example.movieticketing.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
// Note: Set import is not strictly needed if not used directly in this class for initialization
// import java.util.Set; // Not needed if only using field types

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "bookings")
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "showtime_id", nullable = false)
    private Showtime showtime;

    @Column(nullable = false)
    private int numberOfTickets;

    // Could be a separate entity if seats are complex objects, but string for simplicity
    private String seatsBooked; // e.g., "A1,A2,A3"

    @CreationTimestamp // Automatically set to current time on creation
    @Column(nullable = false, updatable = false)
    private LocalDateTime bookingTime;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalPrice;

    @Column(nullable = false)
    private String status; // e.g., "CONFIRMED", "PENDING", "CANCELLED"
}
