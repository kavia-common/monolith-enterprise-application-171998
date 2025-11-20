package com.mycompany.entapp.snowman.infrastructure.rest.endpoint;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * PUBLIC_INTERFACE
 * A minimal health endpoint to verify that Spring MVC routing is active and the application is responding.
 * 
 * GET /health
 * - Summary: Health check
 * - Description: Returns a simple "OK" response to indicate the service is up.
 * - Response: 200 OK with a small JSON body: {"status":"OK"}
 */
@RestController
public class HealthEndpoint {

    // PUBLIC_INTERFACE
    @GetMapping(path = "/health", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> health() {
        /**
         * This method returns a simple status message for health verification.
         * It avoids heavy dependencies and sticks to core Spring MVC.
         */
        return ResponseEntity.ok("{\"status\":\"OK\"}");
    }
}
