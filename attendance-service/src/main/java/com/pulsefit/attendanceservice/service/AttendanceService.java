package com.pulsefit.attendanceservice.service;

import com.pulsefit.attendanceservice.client.MemberClient;
import com.pulsefit.attendanceservice.client.SubscriptionClient;
import com.pulsefit.attendanceservice.entity.Attendance;
import com.pulsefit.attendanceservice.repo.AttendanceRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AttendanceService {

    private final AttendanceRepository repo;
    private final MemberClient members;
    private final SubscriptionClient subscriptions;

    public AttendanceService(
            AttendanceRepository repo,
            MemberClient members,
            SubscriptionClient subscriptions) {

        this.repo = repo;
        this.members = members;
        this.subscriptions = subscriptions;
    }

    public Attendance checkIn(
            Long memberId,
            String facility) {

        /*
         * 1. Verify that the member exists.
         */
        try {
            Map<String, Object> response =
                    members.exists(memberId);

            boolean exists =
                    Boolean.TRUE.equals(response.get("exists"));

            if (!exists) {
                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Member not found"
                );
            }

        } catch (ResponseStatusException e) {
            throw e;

        } catch (Exception e) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Member service unavailable",
                    e
            );
        }

        /*
         * 2. Verify that the member has an active subscription.
         */
        try {
            Map<String, Object> response =
                    subscriptions.active(memberId);

            boolean active =
                    Boolean.TRUE.equals(response.get("active"));

            if (!active) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Member has no active subscription"
                );
            }

        } catch (ResponseStatusException e) {
            throw e;

        } catch (Exception e) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Subscription service unavailable",
                    e
            );
        }

        /*
         * 3. Create attendance record.
         */
        Attendance attendance =
                new Attendance();

        attendance.memberId = memberId;
        attendance.facility = facility;
        attendance.checkInTime = LocalDateTime.now();
        attendance.status = "CHECKED_IN";

        return repo.save(attendance);
    }

    public Attendance checkOut(Long id) {

        Attendance attendance =
                repo.findById(id)
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Attendance record not found"
                                )
                        );

        if (attendance.checkOutTime != null) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Already checked out"
            );
        }

        attendance.checkOutTime =
                LocalDateTime.now();

        attendance.status = "CHECKED_OUT";

        return repo.save(attendance);
    }

    public List<Attendance> all() {
        return repo.findAll();
    }

    public Attendance get(Long id) {

        return repo.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Attendance record not found"
                        )
                );
    }

    public List<Attendance> member(Long memberId) {

        return repo.findByMemberIdOrderByCheckInTimeDesc(
                memberId
        );
    }

    public List<Attendance> today() {

        LocalDate today = LocalDate.now();

        return repo.findByCheckInTimeBetweenOrderByCheckInTimeDesc(
                today.atStartOfDay(),
                today.plusDays(1).atStartOfDay()
        );
    }

    public List<Attendance> facility(String facility) {

        return repo.findByFacilityOrderByCheckInTimeDesc(
                facility
        );
    }

    public Map<String, Object> stats() {

        List<Attendance> today = today();

        long checkedIn = today.stream()
                .filter(a -> "CHECKED_IN".equals(a.status))
                .count();

        Map<String, Object> result = new HashMap<>();
        result.put("today", today.size());
        result.put("checkedIn", checkedIn);
        return result;
    }
}