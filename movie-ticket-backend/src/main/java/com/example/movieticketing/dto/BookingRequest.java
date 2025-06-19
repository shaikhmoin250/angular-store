package com.example.movieticketing.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingRequest {
    private Long userId;
    private Long showtimeId;
    private int numberOfTickets;
    private String seatsBooked; // e.g., "A1,A2"
    // Consider adding validation annotations
}
