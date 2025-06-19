package com.example.movieticketing.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
// import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScreenDto {
    private Long id;
    private String screenNumber;
    private int capacity;
    private Long theaterId; // Representing the Theater relationship with an ID
    // Set<ShowtimeDto> showtimes; // Omitted for now
}
