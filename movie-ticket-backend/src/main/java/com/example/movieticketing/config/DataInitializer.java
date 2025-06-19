package com.example.movieticketing.config;

import com.example.movieticketing.entity.*;
import com.example.movieticketing.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final MovieRepository movieRepository;
    private final TheaterRepository theaterRepository;
    private final ScreenRepository screenRepository; // Might not be needed if theater cascades screen saving
    private final ShowtimeRepository showtimeRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public DataInitializer(UserRepository userRepository, MovieRepository movieRepository,
                           TheaterRepository theaterRepository, ScreenRepository screenRepository,
                           ShowtimeRepository showtimeRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.movieRepository = movieRepository;
        this.theaterRepository = theaterRepository;
        this.screenRepository = screenRepository;
        this.showtimeRepository = showtimeRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        if (userRepository.count() > 0) { // Check if any user exists
            System.out.println("Data already exists. Skipping initialization.");
            return;
        }

        System.out.println("Initializing sample data...");

        // Create Users
        User user1 = new User();
        user1.setUsername("testuser");
        user1.setPassword(passwordEncoder.encode("password"));
        user1.setEmail("user@example.com");
        user1.setRoles("ROLE_USER");
        userRepository.save(user1);

        User adminUser = new User();
        adminUser.setUsername("adminuser");
        adminUser.setPassword(passwordEncoder.encode("adminpass"));
        adminUser.setEmail("admin@example.com");
        adminUser.setRoles("ROLE_ADMIN,ROLE_USER");
        userRepository.save(adminUser);
        System.out.println("Created Users: " + userRepository.count());

        // Create Movies
        Movie movie1 = new Movie(null, "Inception", "A mind-bending thriller about dream infiltration.", 148, LocalDate.parse("2010-07-16"), "Sci-Fi/Thriller", "posters/inception.jpg", new HashSet<>());
        Movie movie2 = new Movie(null, "The Dark Knight", "A masked vigilante battles the criminal underworld of Gotham City.", 152, LocalDate.parse("2008-07-18"), "Action/Crime", "posters/dark_knight.jpg", new HashSet<>());
        Movie movie3 = new Movie(null, "Interstellar", "A team of explorers travel through a wormhole in space in an attempt to ensure humanity's survival.", 169, LocalDate.parse("2014-11-07"), "Sci-Fi/Drama", "posters/interstellar.jpg", new HashSet<>());
        Movie movie4 = new Movie(null, "Parasite", "Greed and class discrimination threaten the newly formed symbiotic relationship between the wealthy Park family and the destitute Kim clan.", 132, LocalDate.parse("2019-05-30"), "Thriller/Drama", "posters/parasite.jpg", new HashSet<>());
        movieRepository.saveAll(List.of(movie1, movie2, movie3, movie4));
        System.out.println("Created Movies: " + movieRepository.count());

        // Create Theaters and Screens
        // Theater 1
        Theater theater1 = new Theater(null, "Grand Cinema City", "123 Main St, Downtown", new HashSet<>());
        Screen screen1A = new Screen(null, "Screen 1", 150, theater1, new HashSet<>());
        Screen screen1B = new Screen(null, "Screen 2 (VIP)", 80, theater1, new HashSet<>());
        Screen screen1C = new Screen(null, "Screen 3", 120, theater1, new HashSet<>());
        // Add screens to theater's set for cascading save
        theater1.getScreens().addAll(Set.of(screen1A, screen1B, screen1C));
        theaterRepository.save(theater1); // Saves theater and its screens due to CascadeType.ALL

        // Theater 2
        Theater theater2 = new Theater(null, "Uptown Movie Palace", "456 Oak Ave, Uptown", new HashSet<>());
        Screen screen2A = new Screen(null, "Hall A", 200, theater2, new HashSet<>());
        Screen screen2B = new Screen(null, "Hall B", 180, theater2, new HashSet<>());
        theater2.getScreens().addAll(Set.of(screen2A, screen2B));
        theaterRepository.save(theater2);
        System.out.println("Created Theaters: " + theaterRepository.count());
        System.out.println("Total Screens created: " + screenRepository.count());


        // Create Showtimes
        // Fetch saved entities to ensure they are managed and have IDs
        Movie inception = movieRepository.findByTitleContainingIgnoreCase("Inception").get(0);
        Movie darkKnight = movieRepository.findByTitleContainingIgnoreCase("Dark Knight").get(0);
        Movie interstellar = movieRepository.findByTitleContainingIgnoreCase("Interstellar").get(0);

        Theater grandCinema = theaterRepository.findByName("Grand Cinema City").get(0); // Assuming findByName exists or use findById
        Screen gcScreen1 = grandCinema.getScreens().stream().filter(s -> s.getScreenNumber().equals("Screen 1")).findFirst().orElse(null);
        Screen gcScreen2Vip = grandCinema.getScreens().stream().filter(s -> s.getScreenNumber().equals("Screen 2 (VIP)")).findFirst().orElse(null);

        Theater uptownPalace = theaterRepository.findByName("Uptown Movie Palace").get(0);
        Screen upHallA = uptownPalace.getScreens().stream().filter(s -> s.getScreenNumber().equals("Hall A")).findFirst().orElse(null);


        if (gcScreen1 != null) {
            Showtime show1 = new Showtime(null, inception, gcScreen1, LocalDateTime.now().plusDays(1).withHour(18).withMinute(0), LocalDateTime.now().plusDays(1).withHour(20).withMinute(28), new BigDecimal("12.50"), new HashSet<>());
            Showtime show2 = new Showtime(null, darkKnight, gcScreen1, LocalDateTime.now().plusDays(1).withHour(21).withMinute(0), LocalDateTime.now().plusDays(1).withHour(23).withMinute(32), new BigDecimal("13.00"), new HashSet<>());
            showtimeRepository.saveAll(List.of(show1, show2));
        }
        if (gcScreen2Vip != null) {
             Showtime show3 = new Showtime(null, interstellar, gcScreen2Vip, LocalDateTime.now().plusDays(2).withHour(19).withMinute(0), LocalDateTime.now().plusDays(2).withHour(21).withMinute(49), new BigDecimal("15.00"), new HashSet<>());
             showtimeRepository.save(show3);
        }
        if (upHallA != null) {
            Showtime show4 = new Showtime(null, inception, upHallA, LocalDateTime.now().plusDays(1).withHour(17).withMinute(30), LocalDateTime.now().plusDays(1).withHour(19).withMinute(58), new BigDecimal("11.00"), new HashSet<>());
            Showtime show5 = new Showtime(null, darkKnight, upHallA, LocalDateTime.now().plusDays(2).withHour(20).withMinute(0), LocalDateTime.now().plusDays(2).withHour(22).withMinute(32), new BigDecimal("12.00"), new HashSet<>());
            showtimeRepository.saveAll(List.of(show4, show5));
        }

        System.out.println("Created Showtimes: " + showtimeRepository.count());
        System.out.println("Sample data initialization complete.");
    }
}
