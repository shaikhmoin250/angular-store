package com.example.movieticketing.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingDto {
    private Long id;
    private Long userId;
    private Long showtimeId;
    private int numberOfTickets;
    private String seatsBooked;
    private LocalDateTime bookingTime;
    private BigDecimal totalPrice;
    private String status;
}
