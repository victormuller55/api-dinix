package br.net.convertix.dinix.controller;

import br.net.convertix.dinix.dto.request.CreateLocationRequest;
import br.net.convertix.dinix.dto.response.LocationResponse;
import br.net.convertix.dinix.dto.response.PageResponse;
import br.net.convertix.dinix.security.SecurityUtils;
import br.net.convertix.dinix.service.LocationService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import br.net.convertix.dinix.web.Paginacao;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/locais")
@Tag(name = "Locais")
public class LocationController {

    private final LocationService locationService;

    public LocationController(LocationService locationService) {
        this.locationService = locationService;
    }

    @GetMapping
    public PageResponse<LocationResponse> list(Paginacao paginacao) {
        return locationService.list(SecurityUtils.currentUserId(), paginacao.toPageable());
    }

    @GetMapping("/{id}")
    public LocationResponse get(@PathVariable UUID id) {
        return locationService.get(SecurityUtils.currentUserId(), id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public LocationResponse create(@Valid @RequestBody CreateLocationRequest request) {
        return locationService.create(SecurityUtils.currentUserId(), request);
    }

    @PutMapping("/{id}")
    public LocationResponse update(@PathVariable UUID id, @Valid @RequestBody CreateLocationRequest request) {
        return locationService.update(SecurityUtils.currentUserId(), id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        locationService.delete(SecurityUtils.currentUserId(), id);
    }
}
