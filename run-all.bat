@echo off
echo Starting Flight Booking Microservices (LOCAL profile)
echo.

set PROFILE=local

echo Starting Service Registry...
start cmd /k "cd service-registry && java -jar target\service-registry-0.0.1-SNAPSHOT.jar --spring.profiles.active=%PROFILE%"

timeout /t 15

echo Starting Config Server...
start cmd /k "cd server && java -jar target\server-0.0.1-SNAPSHOT.jar --spring.profiles.active=%PROFILE%"

timeout /t 15

echo Starting Auth Service...
start cmd /k "cd auth-service && java -jar target\auth-service-0.0.1-SNAPSHOT.jar --spring.profiles.active=%PROFILE%"

echo Starting Flight Service...
start cmd /k "cd flightservice && java -jar target\flightservice-0.0.1-SNAPSHOT.jar --spring.profiles.active=%PROFILE%"

echo Starting Booking Service...
start cmd /k "cd bookingservice && java -jar target\bookingservice-0.0.1-SNAPSHOT.jar --spring.profiles.active=%PROFILE%"

echo Starting API Gateway...
start cmd /k "cd api-gateway && java -jar target\api-gateway-0.0.1-SNAPSHOT.jar --spring.profiles.active=%PROFILE%"

echo.
echo All services triggered.
echo Check Eureka at http://localhost:8761
pause
