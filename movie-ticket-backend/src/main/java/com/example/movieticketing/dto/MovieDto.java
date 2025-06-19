package com.example.movieticketing.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDate;
// import java.util.Set; // Avoid Set<ShowtimeDto> for now to prevent circular dependencies if ShowtimeDto also has MovieDto

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MovieDto {
    private Long id;
    private String title;
    private String description;
    private int durationMinutes;
    private LocalDate releaseDate;
    private String genre;
    private String posterUrl;
    // We might exclude or simplify the 'showtimes' collection in DTOs
    // to avoid circular dependencies or overly large responses.
    // For now, let's omit it from the DTO, or it could be Set<Long> showtimeIds.
}
