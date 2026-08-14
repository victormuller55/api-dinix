package br.net.convertix.dinix.service;

import br.net.convertix.dinix.dto.request.CreateLocationRequest;
import br.net.convertix.dinix.dto.response.LocationResponse;
import br.net.convertix.dinix.dto.response.PageResponse;
import br.net.convertix.dinix.entity.Category;
import br.net.convertix.dinix.entity.PurchaseLocation;
import br.net.convertix.dinix.entity.User;
import br.net.convertix.dinix.exception.ResourceNotFoundException;
import br.net.convertix.dinix.repository.PurchaseLocationRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class LocationService {

    private final PurchaseLocationRepository locationRepository;
    private final AuthService authService;
    private final CategoryService categoryService;

    public LocationService(
            PurchaseLocationRepository locationRepository,
            AuthService authService,
            CategoryService categoryService) {
        this.locationRepository = locationRepository;
        this.authService = authService;
        this.categoryService = categoryService;
    }

    @Transactional
    public LocationResponse create(UUID userId, CreateLocationRequest request) {
        User user = authService.getActive(userId);
        PurchaseLocation location = PurchaseLocation.builder()
                .user(user)
                .name(request.name())
                .description(request.description())
                .category(categoryService.getOwnedOrNull(userId, request.categoryId()))
                .address(request.address())
                .city(request.city())
                .state(request.state())
                .latitude(request.latitude())
                .longitude(request.longitude())
                .build();
        return toResponse(locationRepository.save(location));
    }

    @Transactional(readOnly = true)
    public PageResponse<LocationResponse> list(UUID userId, Pageable pageable) {
        return PageResponse.from(locationRepository.findByUserId(userId, pageable).map(this::toResponse));
    }

    @Transactional(readOnly = true)
    public LocationResponse get(UUID userId, UUID id) {
        return toResponse(getOwned(userId, id));
    }

    @Transactional
    public LocationResponse update(UUID userId, UUID id, CreateLocationRequest request) {
        PurchaseLocation location = getOwned(userId, id);
        location.setName(request.name());
        location.setDescription(request.description());
        location.setCategory(categoryService.getOwnedOrNull(userId, request.categoryId()));
        location.setAddress(request.address());
        location.setCity(request.city());
        location.setState(request.state());
        location.setLatitude(request.latitude());
        location.setLongitude(request.longitude());
        return toResponse(locationRepository.save(location));
    }

    @Transactional
    public void delete(UUID userId, UUID id) {
        locationRepository.delete(getOwned(userId, id));
    }

    public PurchaseLocation getOwned(UUID userId, UUID id) {
        return locationRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Local não encontrado"));
    }

    public PurchaseLocation getOwnedOrNull(UUID userId, UUID id) {
        if (id == null) {
            return null;
        }
        return getOwned(userId, id);
    }

    private LocationResponse toResponse(PurchaseLocation location) {
        Category category = location.getCategory();
        return new LocationResponse(
                location.getId(),
                location.getName(),
                location.getDescription(),
                category != null ? category.getId() : null,
                category != null ? category.getName() : null,
                location.getAddress(),
                location.getCity(),
                location.getState(),
                location.getLatitude(),
                location.getLongitude(),
                location.getCreatedAt(),
                location.getUpdatedAt());
    }
}
