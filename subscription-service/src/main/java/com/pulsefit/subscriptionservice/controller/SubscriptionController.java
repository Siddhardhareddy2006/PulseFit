package com.pulsefit.subscriptionservice.controller;

import com.pulsefit.subscriptionservice.entity.Subscription;
import com.pulsefit.subscriptionservice.repo.SubscriptionRepository;
import com.pulsefit.subscriptionservice.service.SubscriptionService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/subscriptions")
public class SubscriptionController {

    private final SubscriptionRepository repo;
    private final SubscriptionService service;

    public SubscriptionController(
            SubscriptionRepository repo,
            SubscriptionService service) {
        this.repo = repo;
        this.service = service;
    }

    public record CreateRequest(
            @NotNull(message = "memberId is required") Long memberId,
            @NotNull(message = "planId is required") Long planId,
            boolean autoRenew
    ) {}

    @GetMapping
    public List<Subscription> all() {

        List<Subscription> subscriptions = repo.findAll();

        subscriptions.forEach(service::refresh);

        return repo.saveAll(subscriptions);
    }

    @GetMapping("/member/{memberId}")
    public List<Subscription> member(
            @PathVariable Long memberId) {

        List<Subscription> subscriptions =
                repo.findByMemberId(memberId);

        subscriptions.forEach(service::refresh);

        return repo.saveAll(subscriptions);
    }

    @GetMapping("/member/{memberId}/active")
    public Map<String, Object> active(
            @PathVariable Long memberId) {

        boolean active = repo.findByMemberId(memberId)
                .stream()
                .map(service::refresh)
                .anyMatch(subscription ->
                        "ACTIVE".equals(subscription.status));

        Map<String, Object> response = new HashMap<>();
        response.put("memberId", memberId);
        response.put("active", active);

        return response;
    }

    @GetMapping("/{id}")
    public Subscription one(@PathVariable Long id) {
        return service.refresh(service.get(id));
    }

    @PostMapping
    public Subscription create(
            @Valid @RequestBody CreateRequest request) {

        return service.create(
                request.memberId(),
                request.planId(),
                request.autoRenew()
        );
    }

    @PutMapping("/{id}")
    public Subscription update(
            @PathVariable Long id,
            @Valid @RequestBody CreateRequest request) {

        Subscription subscription = service.get(id);

        subscription.memberId = request.memberId();
        subscription.planId = request.planId();
        subscription.autoRenew = request.autoRenew();

        return repo.save(subscription);
    }

    @DeleteMapping("/{id}")
    public Subscription cancel(@PathVariable Long id) {
        return service.cancel(id);
    }

    @PostMapping("/{id}/renew")
    public Subscription renew(@PathVariable Long id) {
        return service.renew(id);
    }
}