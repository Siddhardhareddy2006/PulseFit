package com.pulsefit.attendanceservice.controller;

import com.pulsefit.attendanceservice.entity.Attendance;
import com.pulsefit.attendanceservice.service.AttendanceService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/attendance")
public class AttendanceController {

    private final AttendanceService service;

    public AttendanceController(AttendanceService service) {
        this.service = service;
    }

    public static class CheckInRequest {
        @NotNull(message = "memberId is required")
        private Long memberId;

        @NotBlank(message = "facility is required")
        private String facility;

        public Long getMemberId() {
            return memberId;
        }

        public String getFacility() {
            return facility;
        }
    }

    @PostMapping("/checkin")
    public Attendance checkIn(
            @Valid @RequestBody CheckInRequest request) {

        return service.checkIn(
            request.getMemberId(),
            request.getFacility()
        );
    }

    @PostMapping("/checkout/{id}")
    public Attendance checkOut(@PathVariable Long id) {
        return service.checkOut(id);
    }

    @GetMapping
    public List<Attendance> all() {
        return service.all();
    }

    /*
     * Specific routes are kept before /{id}.
     */
    @GetMapping("/member/{memberId}")
    public List<Attendance> member(
            @PathVariable Long memberId) {

        return service.member(memberId);
    }

    @GetMapping("/today")
    public List<Attendance> today() {
        return service.today();
    }

    @GetMapping("/facility/{facility}")
    public List<Attendance> facility(
            @PathVariable String facility) {

        return service.facility(facility);
    }

    @GetMapping("/stats")
    public Map<String, Object> stats() {
        return service.stats();
    }

    @PostMapping("/subscription-events/{id}")
    public Map<String, Object> subscriptionEvent(
            @PathVariable Long id) {

        Map<String, Object> response = new HashMap<>();
        response.put("received", true);
        response.put("subscriptionId", id);
        return response;
    }

    @GetMapping("/{id}")
    public Attendance one(@PathVariable Long id) {
        return service.get(id);
    }
}