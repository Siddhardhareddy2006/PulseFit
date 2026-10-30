# API Documentation

Gateway base URL: `http://localhost:8080`

- POST `/api/auth/login`
- GET/POST/PUT/DELETE `/api/members` and `/api/members/{id}`
- GET `/api/members/search?name=`
- GET/POST/PUT/DELETE `/api/plans` and `/api/plans/{id}`
- GET/POST/PUT/DELETE `/api/subscriptions` and `/api/subscriptions/{id}`
- POST `/api/subscriptions/{id}/renew`
- GET `/api/subscriptions/member/{memberId}`
- POST `/api/attendance/checkin`
- POST `/api/attendance/checkout/{id}`
- GET `/api/attendance/today`
- GET `/api/attendance/member/{memberId}`
- GET `/api/attendance/facility/{facility}`
