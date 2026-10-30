package com.pulsefit.subscriptionservice.service;

import com.pulsefit.subscriptionservice.client.AttendanceClient;
import com.pulsefit.subscriptionservice.client.MemberClient;
import com.pulsefit.subscriptionservice.entity.Plan;
import com.pulsefit.subscriptionservice.entity.Subscription;
import com.pulsefit.subscriptionservice.repo.PlanRepository;
import com.pulsefit.subscriptionservice.repo.SubscriptionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.Map;

@Service
public class SubscriptionService {

    private final PlanRepository plans;
    private final SubscriptionRepository subs;
    private final MemberClient members;
    private final AttendanceClient attendance;

    public SubscriptionService(
            PlanRepository plans,
            SubscriptionRepository subs,
            MemberClient members,
            AttendanceClient attendance) {

        this.plans = plans;
        this.subs = subs;
        this.members = members;
        this.attendance = attendance;
    }

    public Subscription create(
            Long memberId,
            Long planId,
            boolean autoRenew) {

        /*
         * Verify that the member exists.
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
         * Find the requested membership plan.
         */
        Plan plan = plans.findById(planId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Plan not found"
                        )
                );

        /*
         * Do not allow subscriptions to inactive plans.
         */
        if (!plan.active) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Plan is inactive"
            );
        }

        /*
         * Create subscription.
         */
        Subscription subscription =
                new Subscription();

        subscription.memberId = memberId;
        subscription.planId = planId;
        subscription.startDate = LocalDate.now();
        subscription.endDate =
                subscription.startDate
                        .plusMonths(plan.durationMonths);
        subscription.status = "ACTIVE";
        subscription.autoRenew = autoRenew;

        Subscription saved =
                subs.save(subscription);

        /*
         * Notify Attendance Service.
         *
         * Attendance notification should not prevent
         * the subscription from being created.
         */
        try {
            attendance.subscriptionEvent(saved.id);
        } catch (Exception e) {
            // Subscription has already been saved.
            // Attendance Service can recover independently.
        }

        return saved;
    }

    public Subscription renew(Long id) {

        Subscription subscription = get(id);

        Plan plan = plans.findById(subscription.planId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Plan not found"
                        )
                );

        if (!plan.active) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Plan is inactive"
            );
        }

        subscription.endDate =
                LocalDate.now()
                        .plusMonths(plan.durationMonths);

        subscription.status = "ACTIVE";

        return subs.save(subscription);
    }

    public Subscription cancel(Long id) {

        Subscription subscription = get(id);

        subscription.status = "CANCELLED";

        return subs.save(subscription);
    }

    public Subscription refresh(Subscription subscription) {

        if (subscription.endDate != null
                && subscription.endDate.isBefore(LocalDate.now())
                && "ACTIVE".equals(subscription.status)) {

            subscription.status = "EXPIRED";
        }

        return subscription;
    }

    public Subscription get(Long id) {

        return subs.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Subscription not found"
                        )
                );
    }
}