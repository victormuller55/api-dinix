package br.net.convertix.dinix.controller;

import br.net.convertix.dinix.dto.request.CreateGoalRequest;
import br.net.convertix.dinix.dto.request.UpdateGoalRequest;
import br.net.convertix.dinix.dto.response.GoalResponse;
import br.net.convertix.dinix.dto.response.PageResponse;
import br.net.convertix.dinix.security.SecurityUtils;
import br.net.convertix.dinix.service.GoalService;
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
@RequestMapping("/api/v1/metas")
@Tag(name = "Metas")
public class GoalController {

    private final GoalService goalService;

    public GoalController(GoalService goalService) {
        this.goalService = goalService;
    }

    @GetMapping
    public PageResponse<GoalResponse> list(Paginacao paginacao) {
        return goalService.list(SecurityUtils.currentUserId(), paginacao.toPageable());
    }

    @GetMapping("/{id}")
    public GoalResponse get(@PathVariable UUID id) {
        return goalService.get(SecurityUtils.currentUserId(), id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public GoalResponse create(@Valid @RequestBody CreateGoalRequest request) {
        return goalService.create(SecurityUtils.currentUserId(), request);
    }

    @PutMapping("/{id}")
    public GoalResponse update(@PathVariable UUID id, @Valid @RequestBody UpdateGoalRequest request) {
        return goalService.update(SecurityUtils.currentUserId(), id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        goalService.delete(SecurityUtils.currentUserId(), id);
    }
}
