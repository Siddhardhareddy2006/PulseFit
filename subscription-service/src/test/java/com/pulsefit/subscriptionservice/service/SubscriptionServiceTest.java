package com.pulsefit.subscriptionservice.service;

import com.pulsefit.subscriptionservice.client.AttendanceClient;
import com.pulsefit.subscriptionservice.client.MemberClient;
import com.pulsefit.subscriptionservice.entity.Plan;
import com.pulsefit.subscriptionservice.entity.Subscription;
import com.pulsefit.subscriptionservice.repo.PlanRepository;
import com.pulsefit.subscriptionservice.repo.SubscriptionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubscriptionServiceTest {

    @Mock
    PlanRepository plans;

    @Mock
    SubscriptionRepository subs;

    @Mock
    MemberClient members;

    @Mock
    AttendanceClient attendance;

    @InjectMocks
    SubscriptionService service;

    private Plan activePlan() {

        Plan plan = new Plan();

        plan.id = 2L;
        plan.durationMonths = 3;
        plan.active = true;

        return plan;
    }

    @Test
    void createBuildsActiveSubscription() {

        Plan plan = activePlan();

        when(members.exists(1L))
                .thenReturn(Collections.singletonMap("exists", true));

        when(plans.findById(2L))
                .thenReturn(Optional.of(plan));

        when(subs.save(any(Subscription.class)))
                .thenAnswer(invocation -> {

                    Subscription subscription =
                            invocation.getArgument(0);

                    subscription.id = 10L;

                    return subscription;
                });

        when(attendance.subscriptionEvent(10L))
                .thenReturn(Collections.singletonMap("ok", true));

        Subscription result =
                service.create(1L, 2L, true);

        assertEquals(1L, result.memberId);
        assertEquals(2L, result.planId);
        assertEquals("ACTIVE", result.status);
        assertTrue(result.autoRenew);

        assertEquals(
                LocalDate.now().plusMonths(3),
                result.endDate
        );

        verify(members).exists(1L);
        verify(plans).findById(2L);
        verify(subs).save(any(Subscription.class));
        verify(attendance).subscriptionEvent(10L);
    }

    @Test
    void createRejectsMissingMember() {

        when(members.exists(1L))
                .thenReturn(Collections.singletonMap("exists", false));

        ResponseStatusException exception =
                assertThrows(
                        ResponseStatusException.class,
                        () -> service.create(1L, 2L, false)
                );

        assertEquals(
                HttpStatus.NOT_FOUND,
                exception.getStatusCode()
        );

        verify(members).exists(1L);
        verifyNoInteractions(plans);
    }

    @Test
    void createRejectsInactivePlan() {

        Plan plan = activePlan();
        plan.active = false;

        when(members.exists(1L))
                .thenReturn(Collections.singletonMap("exists", true));

        when(plans.findById(2L))
                .thenReturn(Optional.of(plan));

        ResponseStatusException exception =
                assertThrows(
                        ResponseStatusException.class,
                        () -> service.create(1L, 2L, false)
                );

        assertEquals(
                HttpStatus.CONFLICT,
                exception.getStatusCode()
        );

        verify(members).exists(1L);
        verify(plans).findById(2L);
        verify(subs, never()).save(any());
    }

    @Test
    void renewExtendsSubscription() {

        Subscription subscription = new Subscription();

        subscription.id = 5L;
        subscription.planId = 2L;
        subscription.status = "EXPIRED";

        when(subs.findById(5L))
                .thenReturn(Optional.of(subscription));

        when(plans.findById(2L))
                .thenReturn(Optional.of(activePlan()));

        when(subs.save(subscription))
                .thenReturn(subscription);

        Subscription result =
                service.renew(5L);

        assertEquals(
                "ACTIVE",
                result.status
        );

        assertEquals(
                LocalDate.now().plusMonths(3),
                result.endDate
        );

        verify(subs).findById(5L);
        verify(plans).findById(2L);
        verify(subs).save(subscription);
    }

    @Test
    void cancelMarksSubscriptionCancelled() {

        Subscription subscription =
                new Subscription();

        subscription.id = 5L;
        subscription.status = "ACTIVE";

        when(subs.findById(5L))
                .thenReturn(Optional.of(subscription));

        when(subs.save(subscription))
                .thenReturn(subscription);

        Subscription result =
                service.cancel(5L);

        assertEquals(
                "CANCELLED",
                result.status
        );

        verify(subs).findById(5L);
        verify(subs).save(subscription);
    }

    @Test
    void refreshExpiresPastActiveSubscription() {

        Subscription subscription =
                new Subscription();

        subscription.endDate =
                LocalDate.now().minusDays(1);

        subscription.status = "ACTIVE";

        Subscription result =
                service.refresh(subscription);

        assertEquals(
                "EXPIRED",
                result.status
        );
    }

    @Test
    void refreshDoesNotExpireFutureSubscription() {

        Subscription subscription =
                new Subscription();

        subscription.endDate =
                LocalDate.now().plusDays(10);

        subscription.status = "ACTIVE";

        Subscription result =
                service.refresh(subscription);

        assertEquals(
                "ACTIVE",
                result.status
        );
    }

    @Test
    void getMissingThrows404() {

        when(subs.findById(99L))
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