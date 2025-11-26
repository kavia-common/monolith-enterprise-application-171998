/*
 * |-------------------------------------------------
 * | Copyright © 2018 Colin But. All rights reserved.
 * |-------------------------------------------------
 */
package com.mycompany.entapp.snowman.infrastructure.rest.endpoint;

import com.mycompany.entapp.snowman.domain.exception.SnowmanException;
import com.mycompany.entapp.snowman.infrastructure.rest.mappers.ClientResourceMapper;
import com.mycompany.entapp.snowman.infrastructure.rest.resources.ClientResource;
import com.mycompany.entapp.snowman.domain.model.Client;
import com.mycompany.entapp.snowman.domain.service.ClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/client")
public class ClientRestEndpoint {

    @Autowired
    private ClientService clientService;

    @RequestMapping(value = "/{clientId    private static String safe(String v) {
        if (v == null) return "";
        return v.replace("\\", "\\\\").replace("\"", "\\\"");
    }
    private static boolean isPlaceholder(String value, String name) {
        if (value == null) return false;
        String v = value.trim();
        if (v.equalsIgnoreCase("%7B" + name + "%7D")) return true;
        if (v.equalsIgnoreCase("{" + name + "}")) return true;
        return (v.startsWith("{") && v.endsWith("}"));
    }
}", method = RequestMethod.GET)
    public ResponseEntity getClientInfo(@PathVariable("clientId") String clientId) {
        if (isPlaceholder(clientId, "clientId")) {
            String body = "{"
                    + "\"error\":\"Invalid path placeholder used as literal\","
                    + "\"message\":\"Send a numeric clientId instead of {clientId}.\","
                    + "\"example\":\"/client/1\""
                    + "}";
            return ResponseEntity.badRequest().body(body);
        }
        try {
            Integer id = Integer.valueOf(clientId);
            Client client = clientService.getClient(id);
            ClientResource clientResource = ClientResourceMapper.mapToClientResource(client);
            return ResponseEntity.ok(clientResource);
        } catch (NumberFormatException ex) {
            String body = "{"
                    + "\"error\":\"Invalid clientId\","
                    + "\"message\":\"The provided clientId is not numeric: '" + safe(clientId) + "'.\","
                    + "\"example\":\"/client/1\""
                    + "}";
            return ResponseEntity.badRequest().body(body);
        }
    }

    @RequestMapping(value = "/new", method = RequestMethod.POST)
    public void createClientInfo(@RequestBody ClientResource clientResource) {
        Client client = ClientResourceMapper.mapToClient(clientResource);
        try {
            clientService.createClient(client);
        } catch (SnowmanException e) {
            throw new RuntimeException(e);
        }
    }

    @RequestMapping(value = "/update", method = RequestMethod.POST)
    public void updateClientInfo(@RequestBody ClientResource clientResource) {
        Client client = ClientResourceMapper.mapToClient(clientResource);
        try {
            clientService.updateClient(client);
        } catch (SnowmanException e) {
            throw new RuntimeException(e);
        }
    }

    @RequestMapping(value = "/{clientId}", method = RequestMethod.DELETE)
    public ResponseEntity deleteClientInfo(@PathVariable("clientId") String clientId) {
        if (isPlaceholder(clientId, "clientId")) {
            String body = "{"
                    + "\"error\":\"Invalid path placeholder used as literal\","
                    + "\"message\":\"Send a numeric clientId instead of {clientId}.\","
                    + "\"example\":\"/client/1\""
                    + "}";
            return ResponseEntity.badRequest().body(body);
        }
        try {
            Integer id = Integer.valueOf(clientId);
            try {
                clientService.deleteClient(id);
                return ResponseEntity.ok().build();
            } catch (SnowmanException e) {
                throw new RuntimeException(e);
            }
        } catch (NumberFormatException ex) {
            String body = "{"
                    + "\"error\":\"Invalid clientId\","
                    + "\"message\":\"The provided clientId is not numeric: '" + safe(clientId) + "'.\","
                    + "\"example\":\"/client/1\""
                    + "}";
            return ResponseEntity.badRequest().body(body);
        }
    }
}
