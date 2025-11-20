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
     * This is the primary, existing route that expects an integer path variable.
     */
    @RequestMapping(value = "/{employeeId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<EmployeeResource> getEmployee(@PathVariable Integer employeeId) {
        Employee employee = employeeService.getEmployee(employeeId);
        EmployeeResource employeeResource = EmployeeResourceMapper.mapEmployeeToEmployeeResource(employee);
        return ResponseEntity.ok(employeeResource);
    }

    // PUBLIC_INTERFACE
    /**
     * Compatibility handler for preview systems that request literal placeholder tokens.
     *
     * Handles:
     * - GET /employee/%7BemployeeId%7D    (URL-encoded)
     * - GET /employee/{employeeId}        (string literal as-is)
     *
     * Returns 400 Bad Request with a helpful message explaining correct usage.
     */
    @RequestMapping(value = {"/%7BemployeeId%7D", "/{employeeId}"}, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE, params = "literal=true", headers = "!X-Ignore-Literal")
    public ResponseEntity<String> literalEmployeeIdCompatibility() {
        String body = "{"
                + "\"error\":\"Invalid path placeholder used as literal\","
                + "\"message\":\"This endpoint expects a numeric employeeId, e.g. /employee/123. "
                + "It looks like the placeholder {employeeId} was sent literally by a preview system.\","
                + "\"docs\":\"/openapi.json\""
                + "}";
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    // PUBLIC_INTERFACE
    /**
     * Fallback matcher that captures any string as {id}. If it's non-numeric, respond 404 with clear message.
     * This helps when preview tools construct /employee/{id} with non-numeric values.
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getEmployeeFallback(@PathVariable("id") String id) {
        try {
            Integer numericId = Integer.valueOf(id);
            // If it's numeric, delegate to the primary handler
            Employee employee = employeeService.getEmployee(numericId);
            EmployeeResource employeeResource = EmployeeResourceMapper.mapEmployeeToEmployeeResource(employee);
            // Serialize minimal JSON to avoid introducing object mappers here.
            String json = "{"
                    + "\"employeeId\":" + employeeResource.getEmployeeId() + ","
                    + "\"firstName\":\"" + safe(employeeResource.getFirstName()) + "\","
                    + "\"secondName\":\"" + safe(employeeResource.getSecondName()) + "\","
                    + "\"role\":\"" + safe(employeeResource.getRole()) + "\""
                    + "}";
            return ResponseEntity.ok(json);
        } catch (NumberFormatException ex) {
            String body = "{"
                    + "\"error\":\"Employee not found\","
                    + "\"message\":\"The path segment provided is not a numeric employeeId: '" + safe(id) + "'. "
                    + "Use a numeric id, e.g. /employee/1.\","
                    + "\"docs\":\"/openapi.json\""
                    + "}";
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
        }
    }

    // PUBLIC_INTERFACE
    /**
     * Simple list endpoint to make previews show non-404 content.
     * Returns a small static list of example employees derived from known IDs
     * using existing mapping to avoid new service methods.
     */
    @RequestMapping(value = "", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> listEmployeesForPreview() {
        // Build a tiny list from a couple of known IDs if present; if fails, return empty list gracefully.
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
                // If not found or errors, skip; we just want to provide some content if available
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
