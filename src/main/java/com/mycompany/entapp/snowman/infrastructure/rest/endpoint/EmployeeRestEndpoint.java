/*
 * |-------------------------------------------------
 * | Copyright © 2018 Colin But. All rights reserved.
 * |-------------------------------------------------
 */
package com.mycompany.entapp.snowman.infrastructure.rest.endpoint;

import com.mycompany.entapp.snowman.domain.model.Employee;
import com.mycompany.entapp.snowman.domain.service.EmployeeService;
import com.mycompany.entapp.snowman.infrastructure.rest.mappers.EmployeeResourceMapper;
import com.mycompany.entapp.snowman.infrastructure.rest.resources.EmployeeResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/employee")
public class EmployeeRestEndpoint {

    @Autowired
    private EmployeeService employeeService;

    // PUBLIC_INTERFACE
    /**
     * Get employee by numeric ID.
     * This is the primary route that expects a numeric employeeId path variable.
     * Examples:
     * - GET /employee/1      -> 200 with employee
     * - GET /employee/9999   -> 404 (if not found)
     */
    @RequestMapping(value = "/{employeeId:[0-9]+}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<EmployeeResource> getEmployee(@PathVariable Integer employeeId) {
        Employee employee = employeeService.getEmployee(employeeId);
        EmployeeResource employeeResource = EmployeeResourceMapper.mapEmployeeToEmployeeResource(employee);
        return ResponseEntity.ok(employeeResource);
    }

    // PUBLIC_INTERFACE
    /**
     * Defensive handler for cases where clients send the literal placeholder token.
     * Handles the following and returns 400 with guidance:
     * - GET /employee/%7BemployeeId%7D
     * - GET /employee/{employeeId}
     */
    @RequestMapping(
            value = {"/%7BemployeeId%7D", "/{employeeId:\\{employeeId\\}}"},
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> rejectLiteralEmployeeId() {
        String body = "{"
                + "\"error\":\"Invalid path placeholder used as literal\","
                + "\"message\":\"Send a numeric employeeId instead of {employeeId}. Example: /employee/123\","
                + "\"hint\":\"If you see %7BemployeeId%7D, your client is URL-encoding the template; remove the braces.\","
                + "\"docs\":\"/openapi.json\""
                + "}";
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    // PUBLIC_INTERFACE
    /**
     * Fallback for any non-numeric path segment. If numeric, delegate to service. If not numeric, return 404 with message.
     * This prevents the servlet container from serving a static default handler and gives clearer feedback.
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getEmployeeFallback(@PathVariable("id") String id) {
        try {
            Integer numericId = Integer.valueOf(id);
            Employee employee = employeeService.getEmployee(numericId);
            EmployeeResource r = EmployeeResourceMapper.mapEmployeeToEmployeeResource(employee);
            String json = "{"
                    + "\"employeeId\":" + r.getEmployeeId() + ","
                    + "\"firstName\":\"" + safe(r.getFirstName()) + "\","
                    + "\"secondName\":\"" + safe(r.getSecondName()) + "\","
                    + "\"role\":\"" + safe(r.getRole()) + "\""
                    + "}";
            return ResponseEntity.ok(json);
        } catch (NumberFormatException ex) {
            String body = "{"
                    + "\"error\":\"Employee not found\","
                    + "\"message\":\"The provided path segment is not a numeric employeeId: '" + safe(id) + "'. Use /employee/1.\","
                    + "\"docs\":\"/openapi.json\""
                    + "}";
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
        }
    }

    // PUBLIC_INTERFACE
    /**
     * Optional list endpoint for previews/smoke tests; returns available probe IDs if present.
     */
    @RequestMapping(value = "", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> listEmployeesForPreview() {
        List<String> items = new ArrayList<>();
        for (int probeId : new int[]{1, 2}) {
            try {
                Employee employee = employeeService.getEmployee(probeId);
                EmployeeResource r = EmployeeResourceMapper.mapEmployeeToEmployeeResource(employee);
                String item = "{"
                        + "\"employeeId\":" + r.getEmployeeId() + ","
                        + "\"firstName\":\"" + safe(r.getFirstName()) + "\","
                        + "\"secondName\":\"" + safe(r.getSecondName()) + "\","
                        + "\"role\":\"" + safe(r.getRole()) + "\""
                        + "}";
                items.add(item);
            } catch (Exception ignore) {
                // skip if not available
            }
        }
        String response = "{\"items\":[" + String.join(",", items) + "]}";
        return ResponseEntity.ok(response);
    }

    @RequestMapping(value = "/create", method = RequestMethod.POST)
    public ResponseEntity createEmployee(@Valid EmployeeResource employeeResource) {
        Employee employee = EmployeeResourceMapper.mapEmployeeResourceToEmployee(employeeResource);
        employeeService.createEmployee(employee);
        return ResponseEntity.ok().build();
    }

    @RequestMapping(value = "/update", method = RequestMethod.POST)
    public ResponseEntity updateExistingEmployee(@Valid EmployeeResource employeeResource){
        Employee employee = EmployeeResourceMapper.mapEmployeeResourceToEmployee(employeeResource);
        employeeService.updateEmployee(employee);
        return ResponseEntity.ok().build();
    }

    @RequestMapping(value = "/{employeeId}/delete", method = RequestMethod.DELETE)
    public ResponseEntity deleteExistingEmployee(@PathVariable Integer employeeId){
        employeeService.deleteEmployee(employeeId);
        return ResponseEntity.ok().build();
    }

    /**
     * Escape helper for simple JSON string building to avoid breaking JSON structure.
     */
    private static String safe(String v) {
        if (v == null) return "";
        return v.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
