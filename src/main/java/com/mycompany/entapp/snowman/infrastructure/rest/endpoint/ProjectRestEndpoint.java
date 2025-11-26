/*
 * |-------------------------------------------------
 * | Copyright © 2018 Colin But. All rights reserved.
 * |-------------------------------------------------
 */
package com.mycompany.entapp.snowman.infrastructure.rest.endpoint;

import com.mycompany.entapp.snowman.domain.model.Project;
import com.mycompany.entapp.snowman.domain.service.ProjectService;
import com.mycompany.entapp.snowman.infrastructure.rest.mappers.ProjectResourceMapper;
import com.mycompany.entapp.snowman.infrastructure.rest.resources.ProjectResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/project")
public class ProjectRestEndpoint {


    private static final Logger LOGGER = LoggerFactory.getLogger(ProjectRestEndpoint.class);

    @Autowired
    private ProjectService projectService;

    @RequestMapping("/{projectId    private static String safe(String v) {
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
}")
    public ResponseEntity getProject(@PathVariable("projectId") String projectId) {
        if (isPlaceholder(projectId, "projectId")) {
            String body = "{"
                    + "\"error\":\"Invalid path placeholder used as literal\","
                    + "\"message\":\"Send a numeric projectId instead of {projectId}.\","
                    + "\"example\":\"/project/1\","
                    + "\"docs\":\"/openapi.json\""
                    + "}";
            return ResponseEntity.badRequest().body(body);
        }
        try {
            Integer id = Integer.valueOf(projectId);
            Project project = projectService.getProject(id);
            ProjectResource projectResource = ProjectResourceMapper.mapToProjectResource(project);
            return ResponseEntity.ok(projectResource);
        } catch (NumberFormatException ex) {
            String body = "{"
                    + "\"error\":\"Invalid projectId\","
                    + "\"message\":\"The provided projectId is not numeric: '" + safe(projectId) + "'.\","
                    + "\"example\":\"/project/1\""
                    + "}";
            return ResponseEntity.badRequest().body(body);
        }
    }

    @RequestMapping("/create")
    public ResponseEntity<?> createProject(@Valid ProjectResource projectResource) {
        Project project = ProjectResourceMapper.mapToProject(projectResource);
        projectService.createProject(project);
        return ResponseEntity.ok().build();
    }

    @RequestMapping("/{projectId}/delete")
    public ResponseEntity deleteProject(@PathVariable("projectId") String projectId) {
        if (isPlaceholder(projectId, "projectId")) {
            String body = "{"
                    + "\"error\":\"Invalid path placeholder used as literal\","
                    + "\"message\":\"Send a numeric projectId instead of {projectId}.\","
                    + "\"example\":\"/project/1/delete\""
                    + "}";
            return ResponseEntity.badRequest().body(body);
        }
        try {
            Integer id = Integer.valueOf(projectId);
            projectService.deleteProject(id);
            return ResponseEntity.ok().build();
        } catch (NumberFormatException ex) {
            String body = "{"
                    + "\"error\":\"Invalid projectId\","
                    + "\"message\":\"The provided projectId is not numeric: '" + safe(projectId) + "'.\","
                    + "\"example\":\"/project/1/delete\""
                    + "}";
            return ResponseEntity.badRequest().body(body);
        }
    }

    @RequestMapping("/update")
    public ResponseEntity<?> updateProject(ProjectResource projectResource) {
        Project project = ProjectResourceMapper.mapToProject(projectResource);
        projectService.updateProject(project);
        return ResponseEntity.ok().build();
    }
}
