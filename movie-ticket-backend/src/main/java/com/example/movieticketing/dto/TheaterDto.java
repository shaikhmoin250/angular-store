package com.example.movieticketing.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
// import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TheaterDto {
    private Long id;
    private String name;
    private String location;
    // Set<ScreenDto> screens; // Omitted for now
}
