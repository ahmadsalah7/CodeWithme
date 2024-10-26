package com.codewithme.compiler.controller;
import com.codewithme.compiler.dto.*;
import com.codewithme.compiler.entity.Comment;
import com.codewithme.compiler.entity.Project;
import com.codewithme.compiler.service.ProjectService;

import com.codewithme.compiler.service.UserProjectService;
import com.codewithme.compiler.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

@RestController
@RequestMapping("/projects")
public class ProjectController {

    private ProjectService projectService;
    private UserProjectService userProjectService;
    private UserService userService;

    @Autowired
    public ProjectController(ProjectService projectService , UserProjectService userProjectService , UserService userService ) {
        this.projectService = projectService;
        this.userProjectService = userProjectService;
        this.userService = userService;
    }

    @PostMapping("/create")
    public Project createProject( @RequestBody CreateProjectRequest request) {
        return projectService.createProject(request.getProjectName(), request.getOwnerEmail() ,request.getContent());
    }
    @PostMapping("/delete")
    public  ResponseEntity deleteProject( @RequestBody DeleteProjectRequest request) {
        boolean success = projectService.deleteProject(request.getProjectId() , request.getOwnerEmail());
        if(success){
            return ResponseEntity.ok("Project successfully deleted.");
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Project deletion failed.");
    }

    @GetMapping("/getprojects/{email}")
    public List<Project> getProjectsByUserEmail(@PathVariable String email) {
        return projectService.getProjectsByUserEmail(email);
    }

    @PostMapping("/saveproject")
    public ResponseEntity<String> saveProjectContent( @RequestBody ProjectContentRequest request) {
        try {
            projectService.updateProjectContent(request.getProjectId(), request.getContent(), request.getEmail());
            return ResponseEntity.ok("Project content updated successfully");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to update project content");
        }
    }

    @PostMapping("/addcomment/{projectId}")
    public ResponseEntity<String> addCommentToProject(@PathVariable String projectId,@RequestBody CommentRequest commentRequest) {
        try{
            projectService.addCommentToProject(projectId , commentRequest );
            return ResponseEntity.ok("{\"message\": \"Comment saved successfully\"}");
        }
        catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("{\"message\": \"Failed to save comment\"}");
        }
    }
    @GetMapping("/getcomments/{projectId}")
    public ResponseEntity<List<Comment>> getCommentsFromProject(@PathVariable String projectId) {
        try{
            System.out.println("rpoject id is" + projectId);
            List<Comment> comments = projectService.getCommentsByProject(projectId);
            return ResponseEntity.ok(comments);
        }
        catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PostMapping("/addcollaborator")
    public ResponseEntity<String> addCollaborator(@RequestBody CollaboratorRequest request) {
        System.out.println("Received request to add collaborator: " + request);

        if (!userService.exists(request.getCollaboratorEmail())) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Collaborator does not use this application");
        }

        try {
            if (userProjectService.isOwner(request.getProjectId(), request.getOwnerEmail())) {
                boolean success = userProjectService.addCollaborator(request.getCollaboratorEmail(), request.getProjectId(), request.getRole());

                if (success) {
                    return ResponseEntity.ok("Collaborator added successfully");
                } else {
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to add collaborator");
                }
            } else {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("User is not authorized to add collaborators for this project");
            }
        } catch (Exception e) {
            System.err.println("An error occurred while adding collaborator: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while adding collaborator");
        }
    }

    @PostMapping("/deletecollaborator")
    public ResponseEntity<String> deleteCollaborator(@RequestBody CollaboratorDeletetionRequest request) {
        System.out.println("Received request to delete collaborator: " + request);

        try {
            if (userProjectService.isOwner(request.getProjectId(), request.getOwnerEmail())) {
                boolean success = userProjectService.deleteCollaborator(request.getCollaboratorEmail(), request.getProjectId());

                if (success) {
                    return ResponseEntity.ok("Collaborator deleted successfully");
                } else {
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to delete collaborator");
                }
            } else {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("User is not authorized to delete collaborators for this project");
            }
        } catch (Exception e) {
            System.err.println("An error occurred while deleting collaborator: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while deleting collaborator");
        }
    }

    @GetMapping("/isViewer/{projectId}/{email}")
    public ResponseEntity<String> isViewer(@PathVariable String projectId, @PathVariable String email) {
        if(userProjectService.isViewer(projectId , email)) {
            return ResponseEntity.ok("he is Viewer");
        } else{
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("he is not Viewer");
        }
    }
    
}
