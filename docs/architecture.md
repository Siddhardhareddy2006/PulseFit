# PulseFit Architecture

Client -> Frontend -> API Gateway (:8080) -> Eureka-discovered microservices.

Services: Eureka 8761, Gateway 8080, Auth 8081, Member 8082, Subscription 8083, Attendance 8084. Each business service uses its own in-memory H2 database. JWT is issued by Auth and verified at the Gateway. Subscription and Attendance use OpenFeign with Eureka service names for internal communication.
