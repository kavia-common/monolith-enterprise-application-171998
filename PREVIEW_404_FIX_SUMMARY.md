# Placeholder Detection for REST Endpoints

This change improves developer experience by detecting when literal path placeholders are sent to the API (e.g., `{employeeId}` or `%7BemployeeId%7D`) and returning HTTP 400 with actionable guidance instead of a confusing 404.

Updated endpoints:
- GET /employee/{employeeId}, DELETE /employee/{employeeId}/delete
- GET /project/{projectId}, DELETE /project/{projectId}/delete
- GET /user/{userId}, DELETE /user/{userId}/delete
- GET /client/{clientId}, DELETE /client/{clientId}

Behavior:
- If a placeholder pattern is detected, the endpoint returns:
  {
    "error": "Invalid path placeholder used as literal",
    "message": "Send a numeric ID instead of {…}.",
    "example": "/resource/1",
    "hint": "If you see %7B…%7D, your client is URL-encoding a template; remove the braces.",
    "docs": "/openapi.json"
  }

Notes:
- Existing URLs, HTTP methods, and service calls remain unchanged.
- Numeric IDs are parsed only after verifying the input is not a placeholder.
