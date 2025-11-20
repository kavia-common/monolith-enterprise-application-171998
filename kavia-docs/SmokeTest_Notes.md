# Snowman Backend REST Endpoint Smoke Test Notes

This repository config uses embedded Jetty with a Spring MVC DispatcherServlet (web.xml mapped to "/").
Controllers are annotated with @RestController in package:
- com.mycompany.entapp.snowman.infrastructure.rest.endpoint

Component scanning is enabled via META-INF/application-context.xml.

Endpoints (examples):
- GET /employee/{employeeId}
- POST /employee/create (form-urlencoded binding to EmployeeResource)
- POST /employee/update (form-urlencoded)
- DELETE /employee/{employeeId}/delete

Basic smoke sequence:
1) GET http://localhost:3001/employee/1
2) POST http://localhost:3001/employee/create
   Content-Type: application/x-www-form-urlencoded
   Body: firstName=John&secondName=Doe&role=ENGINEER
3) GET http://localhost:3001/employee/{newId}   (note: create returns 200 without body; IDs may need DB seed or to query list endpoints if available)
4) POST http://localhost:3001/employee/update
   Content-Type: application/x-www-form-urlencoded
   Body: employeeId={newId}&firstName=Johnny&secondName=Doe&role=ENGINEER
5) DELETE http://localhost:3001/employee/{newId}/delete
6) GET to confirm deletion returns 404 or domain-specific not found handling.

If 404 from Jetty default page appears, ensure Spring controllers are scanned:
- META-INF/application-context.xml should have component-scan that includes controllers, e.g.:
  <context:component-scan base-package="com.mycompany.entapp.snowman" />
