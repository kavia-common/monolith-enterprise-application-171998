# Detailed Code Changes: Placeholder Detection in REST Endpoints

Summary:
- Implement early detection of literal placeholders on path variables across Employee, Project, User, and Client endpoints.
- Return HTTP 400 with structured JSON guidance when placeholders are detected.
- Keep existing routes and service interactions intact; only modify path variable handling.

Changes by class:

1) EmployeeRestEndpoint.java
- Keep numeric GET route: GET /employee/{employeeId:[0-9]+}
- Add generic GET route that accepts String and:
  - Detects placeholder values like "{employeeId}", "%7BemployeeId%7D", and generic "{...}"
  - Returns 400 with error, message, example, hint, docs
  - If not placeholder: attempts to parse numeric and delegates to EmployeeService
- Update DELETE /employee/{employeeId}/delete to use String, detect placeholders, then parse numeric and delegate
- Add helpers: safe(String) and isPlaceholder(String, String)

2) ProjectRestEndpoint.java
- Change GET /project/{projectId} to accept String, detect placeholders, return 400; otherwise parse to Integer and delegate
- Change DELETE /project/{projectId}/delete similarly
- Fix minor typo in mapping path from "/update}" to "/update"
- Add helpers: safe, isPlaceholder

3) UserRestEndpoint.java
- Keep String for GET /user/{userId}; add placeholder detection with 400 guidance
- Change DELETE /user/{userId}/delete to String with detection, then parse numeric and delegate
- Add helpers: safe, isPlaceholder

4) ClientRestEndpoint.java
- Change GET /client/{clientId} to String with detection and parsing
- Change DELETE /client/{clientId} to String with detection and parsing
- Add helpers: safe, isPlaceholder

Response format:
- JSON bodies include "error", "message", "example", optionally "hint", and "docs" pointing to /openapi.json.

Backward compatibility:
- Numeric routes remain valid.
- Service calls untouched; only pre-parse logic added.

Testing:
- Existing unit tests should continue to pass; new behavior adds guard rails for invalid inputs.
