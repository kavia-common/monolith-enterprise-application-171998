/*
 * |-------------------------------------------------
 * | Copyright © 2017 Colin But. All rights reserved.
 * |-------------------------------------------------
 */
package com.mycompany.entapp.snowman.infrastructure.rest.endpoint;

import com.mycompany.entapp.snowman.domain.model.User;
import com.mycompany.entapp.snowman.domain.service.UserService;
import com.mycompany.entapp.snowman.infrastructure.rest.mappers.UserResourceMapper;
import com.mycompany.entapp.snowman.infrastructure.rest.resources.UserResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/user")
public class UserRestEndpoint {

    @Autowired
    private UserService userService;

    @RequestMapping(value = "/{userId    private static String safe(String v) {
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
    public ResponseEntity getUser(@PathVariable("userId") String userId) {
        if (isPlaceholder(userId, "userId")) {
            String body = "{"
                    + "\"error\":\"Invalid path placeholder used as literal\","
                    + "\"message\":\"Send a concrete userId instead of {userId}.\","
                    + "\"example\":\"/user/1\""
                    + "}";
            return ResponseEntity.badRequest().body(body);
        }
        User user = userService.findUser(userId);
        UserResource userResource = UserResourceMapper.mapUserToUserResource(user);
        return ResponseEntity.ok(userResource);
    }

    @RequestMapping(value = "/create", method = RequestMethod.POST)
    public ResponseEntity createNewUser(@Valid UserResource userResource) {
        User user = UserResourceMapper.mapUserResourceToUser(userResource);
        userService.createUser(user);
        return ResponseEntity.ok().build();
    }

    @RequestMapping(value = "/update", method = RequestMethod.POST)
    public ResponseEntity updateExistingUser(@Valid UserResource userResource){
        User user = UserResourceMapper.mapUserResourceToUser(userResource);
        userService.updateUser(user);
        return ResponseEntity.ok().build();
    }

    @RequestMapping(value = "{userId}/delete", method = RequestMethod.DELETE)
    public ResponseEntity deleteUser(@PathVariable("userId") String userId) {
        if (isPlaceholder(userId, "userId")) {
            String body = "{"
                    + "\"error\":\"Invalid path placeholder used as literal\","
                    + "\"message\":\"Send a numeric userId instead of {userId}.\","
                    + "\"example\":\"/user/1/delete\""
                    + "}";
            return ResponseEntity.badRequest().body(body);
        }
        try {
            Integer id = Integer.valueOf(userId);
            userService.deleteUser(id);
            return ResponseEntity.ok().build();
        } catch (NumberFormatException ex) {
            String body = "{"
                    + "\"error\":\"Invalid userId\","
                    + "\"message\":\"The provided userId is not numeric: '" + safe(userId) + "'.\","
                    + "\"example\":\"/user/1/delete\""
                    + "}";
            return ResponseEntity.badRequest().body(body);
        }
    }
}
