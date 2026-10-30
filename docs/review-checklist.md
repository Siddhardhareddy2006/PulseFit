# PulseFit Second Review Checklist

- [ ] All six Spring Boot modules build successfully with JDK 25.
- [ ] Eureka dashboard shows Auth, Member, Subscription, Attendance, and API Gateway.
- [ ] Login returns a signed JWT.
- [ ] Gateway returns 401 for protected API requests without a token and forwards authorized requests with a valid bearer token.
- [ ] Member CRUD works through `/api/members`.
- [ ] Plan CRUD works through `/api/plans`.
- [ ] Subscription create, list, renew, and cancel work through `/api/subscriptions`.
- [ ] New-member-in-subscription workflow creates the member first and then uses its returned ID to create the subscription.
- [ ] Attendance check-in and check-out work through `/api/attendance`.
- [ ] Start secondary Member and Subscription instances; verify both instances appear in Eureka and Gateway routes use `lb://` service IDs.
- [ ] Keep actual MOOC certificates/progress evidence separate from the software demonstration.
