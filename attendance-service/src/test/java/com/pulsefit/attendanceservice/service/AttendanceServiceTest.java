package com.pulsefit.attendanceservice.service;

import com.pulsefit.attendanceservice.client.MemberClient;
import com.pulsefit.attendanceservice.client.SubscriptionClient;
import com.pulsefit.attendanceservice.entity.Attendance;
import com.pulsefit.attendanceservice.repo.AttendanceRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AttendanceServiceTest {

    @Mock
    AttendanceRepository repo;

    @Mock
    MemberClient members;

    @Mock
    SubscriptionClient subscriptions;

    @InjectMocks
    AttendanceService service;

    @Test
    void checkInRequiresMemberAndActiveSubscription() {

        when(members.exists(1L))
                .thenReturn(Collections.singletonMap("exists", true));

        when(subscriptions.active(1L))
                .thenReturn(Collections.singletonMap("active", true));

        when(repo.save(any(Attendance.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        Attendance result =
                service.checkIn(1L, "Central");

        assertEquals(1L, result.memberId);
        assertEquals("Central", result.facility);
        assertEquals("CHECKED_IN", result.status);
        assertNotNull(result.checkInTime);

        verify(members).exists(1L);
        verify(subscriptions).active(1L);
        verify(repo).save(any(Attendance.class));
    }

    @Test
    void checkInRejectsMissingMember() {

        when(members.exists(1L))
                .thenReturn(Collections.singletonMap("exists", false));

        ResponseStatusException exception =
                assertThrows(
                        ResponseStatusException.class,
                        () -> service.checkIn(1L, "Central")
                );

        assertEquals(
                HttpStatus.NOT_FOUND,
                exception.getStatusCode()
        );

        verify(members).exists(1L);
        verifyNoInteractions(subscriptions);
        verify(repo, never()).save(any());
    }

    @Test
    void checkInRejectsInactiveSubscription() {

        when(members.exists(1L))
                .thenReturn(Collections.singletonMap("exists", true));

        when(subscriptions.active(1L))
                .thenReturn(Collections.singletonMap("active", false));

        ResponseStatusException exception =
                assertThrows(
                        ResponseStatusException.class,
                        () -> service.checkIn(1L, "Central")
                );

        assertEquals(
                HttpStatus.CONFLICT,
                exception.getStatusCode()
        );

        verify(members).exists(1L);
        verify(subscriptions).active(1L);
        verify(repo, never()).save(any());
    }

    @Test
    void checkInRejectsUnavailableMemberService() {

        when(members.exists(1L))
                .thenThrow(new RuntimeException(
                        "Connection refused"
                ));

        ResponseStatusException exception =
                assertThrows(
                        ResponseStatusException.class,
                        () -> service.checkIn(1L, "Central")
                );

        assertEquals(
                HttpStatus.SERVICE_UNAVAILABLE,
                exception.getStatusCode()
        );

        assertEquals(
                "Member service unavailable",
                exception.getReason()
        );

        verify(members).exists(1L);
        verifyNoInteractions(subscriptions);
        verify(repo, never()).save(any());
    }

    @Test
    void checkInRejectsUnavailableSubscriptionService() {

        when(members.exists(1L))
                .thenReturn(Collections.singletonMap("exists", true));

        when(subscriptions.active(1L))
                .thenThrow(new RuntimeException(
                        "Connection refused"
                ));

        ResponseStatusException exception =
                assertThrows(
                        ResponseStatusException.class,
                        () -> service.checkIn(1L, "Central")
                );

        assertEquals(
                HttpStatus.SERVICE_UNAVAILABLE,
                exception.getStatusCode()
        );

        assertEquals(
                "Subscription service unavailable",
                exception.getReason()
        );

        verify(members).exists(1L);
        verify(subscriptions).active(1L);
        verify(repo, never()).save(any());
    }

    @Test
    void checkOutMarksRecordCheckedOut() {

        Attendance attendance =
                new Attendance();

        attendance.id = 2L;
        attendance.status = "CHECKED_IN";
        attendance.checkInTime = LocalDateTime.now();

        when(repo.findById(2L))
                .thenReturn(Optional.of(attendance));

        when(repo.save(attendance))
                .thenReturn(attendance);

        Attendance result =
                service.checkOut(2L);

        assertEquals(
                "CHECKED_OUT",
                result.status
        );

        assertNotNull(
                result.checkOutTime
        );

        verify(repo).findById(2L);
        verify(repo).save(attendance);
    }

    @Test
    void checkOutTwiceIsConflict() {

        Attendance attendance =
                new Attendance();

        attendance.id = 2L;
        attendance.status = "CHECKED_OUT";
        attendance.checkOutTime =
                LocalDateTime.now();

        when(repo.findById(2L))
                .thenReturn(Optional.of(attendance));

        ResponseStatusException exception =
                assertThrows(
                        ResponseStatusException.class,
                        () -> service.checkOut(2L)
                );

        assertEquals(
                HttpStatus.CONFLICT,
                exception.getStatusCode()
        );

        verify(repo, never()).save(any());
    }

    @Test
    void allReturnsRepositoryResults() {

        when(repo.findAll())
                .thenReturn(Collections.singletonList(new Attendance()));

        assertEquals(
                1,
                service.all().size()
        );

        verify(repo).findAll();
    }

    @Test
    void memberReturnsRepositoryResults() {

        when(
                repo.findByMemberIdOrderByCheckInTimeDesc(1L)
        ).thenReturn(
                Collections.singletonList(new Attendance())
        );

        assertEquals(
                1,
                service.member(1L).size()
        );

        verify(
                repo
        ).findByMemberIdOrderByCheckInTimeDesc(1L);
    }

    @Test
    void facilityReturnsRepositoryResults() {

        when(
                repo.findByFacilityOrderByCheckInTimeDesc("Central")
        ).thenReturn(
                Collections.singletonList(new Attendance())
        );

        assertEquals(
                1,
                service.facility("Central").size()
        );

        verify(
                repo
        ).findByFacilityOrderByCheckInTimeDesc("Central");
    }

    @Test
    void getMissingThrows404() {

        when(repo.findById(99L))
                .thenReturn(Optional.empty());

        ResponseStatusException exception =
                assertThrows(
                        ResponseStatusException.class,
                        () -> service.get(99L)
                );

        assertEquals(
                HttpStatus.NOT_FOUND,
                exception.getStatusCode()
        );
    }
}