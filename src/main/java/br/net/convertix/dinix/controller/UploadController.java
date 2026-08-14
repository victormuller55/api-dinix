package br.net.convertix.dinix.controller;

import br.net.convertix.dinix.service.ProfilePhotoStorage;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

@RestController
public class UploadController {

    private final ProfilePhotoStorage profilePhotoStorage;

    public UploadController(ProfilePhotoStorage profilePhotoStorage) {
        this.profilePhotoStorage = profilePhotoStorage;
    }

    @GetMapping("/uploads/avatars/{filename}")
    public ResponseEntity<Resource> avatar(@PathVariable String filename) {
        Resource resource = profilePhotoStorage.load(filename);
        if (resource == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok()
                .contentType(profilePhotoStorage.mediaType(filename))
                .cacheControl(CacheControl.maxAge(30, TimeUnit.DAYS).cachePublic())
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                .body(resource);
    }
}
