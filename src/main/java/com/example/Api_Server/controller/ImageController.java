package com.example.Api_Server.controller;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("photo")
public class ImageController {

    @GetMapping("/image")
    public ResponseEntity<Resource> getImage(@RequestParam String imageName) throws IOException {
        File imageFile = new File("src/main/resources/static/images/" + imageName + ".jpg");
        if (!imageFile.exists()) {
            return ResponseEntity.notFound().build();
        }
        Resource resource = new FileSystemResource(imageFile);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, "image/jpeg")
                .body(resource);
    }
}