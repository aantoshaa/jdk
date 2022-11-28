package com.example.demo.controller;

import com.example.demo.dto.RatingForUpdate;
import com.example.demo.dto.TrackDto;
import com.example.demo.dto.TrackFromRedactor;
import com.example.demo.entity.Author;
import com.example.demo.entity.Genre;
import com.example.demo.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;

@RestController
@RequestMapping("/tracks/")
@CrossOrigin("*")
@Slf4j
public class TrackController {

    private final TrackServiceImpl trackService;
    private final UserServiceImpl userService;
    private final GenreServiceImpl genreService;
    private final AuthorServiceImpl authorService;
    private final EmailServiceImpl emailService;

    @Value("${upload.path}")
    private String uploadPath;

    @Autowired
    public TrackController(TrackServiceImpl trackService, UserServiceImpl userService, GenreServiceImpl genreService, AuthorServiceImpl authorService, EmailServiceImpl emailService) {
        this.trackService = trackService;
        this.userService = userService;
        this.genreService = genreService;
        this.authorService = authorService;
        this.emailService = emailService;
    }

    @PostMapping(value = "AddTrack", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TrackDto> addTrack(@RequestBody TrackFromRedactor trackFromRedactor) {
        Author author = authorService.getAuthorById(trackFromRedactor.getAuthorId());
        Genre genre = genreService.getGenreById(trackFromRedactor.getGenreId());
        String name = trackFromRedactor.getName();
        String path = trackFromRedactor.getPath();

        TrackDto trackDto = trackService.createTrack(name, path, author, genre);

        new Thread(() -> {
            emailService.sendEmailToAllUsers("New composition was added!", "New track " + name + " added to the library. Let's listen to!");
        }).start();

        return new ResponseEntity<>(trackDto, HttpStatus.OK);
    }

    @PostMapping(value = "CopyTrackInAudio", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity uploadFile(@RequestBody MultipartFile file) {
        try {
            InputStream inputStream = file.getInputStream();
            String path = file.getOriginalFilename();
            Path destination = new File("D:\\EDUCATION\\Third course\\1semestr\\JAVA\\spring-project-ui\\public\\Audio" + path).toPath();

            Files.copy(inputStream, destination);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return ResponseEntity.ok().build();
    }


    @GetMapping(value = "TracksForUser/{login}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Collection<TrackDto>> getUserRatedTracks(@PathVariable String login) {
        Long id = userService.getUserIdByLogin(login);

        if (id == null) return ResponseEntity.badRequest().build();

        Collection<TrackDto> tracks = trackService.getAllTracksForCurrentUser(id);

        return new ResponseEntity<>(tracks, HttpStatus.OK);
    }

    @PostMapping(value = "AddOrUpdateRating",produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TrackDto> addOrUpdateTrackUserRating(@RequestBody RatingForUpdate ratingForUpdate) {
        String login = ratingForUpdate.getLogin();
        Long trackId = ratingForUpdate.getTrackId();
        int mark = ratingForUpdate.getMark();

        Long id = userService.getUserIdByLogin(login);
        if (id == null) return ResponseEntity.badRequest().build();

        TrackDto track = trackService.addOrUpdateTrackUserRating(trackId, mark, id);
        return new ResponseEntity<>(track, HttpStatus.OK);
    }
}


