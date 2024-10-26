package com.codewithme.compiler.service;

import com.codewithme.compiler.dto.CommentRequest;
import com.codewithme.compiler.entity.Comment;
import com.codewithme.compiler.entity.Project;
import com.codewithme.compiler.entity.UserProject;
import com.codewithme.compiler.entity.Version;
import com.codewithme.compiler.repository.ProjectRepository;
import com.codewithme.compiler.repository.UserProjectRepository;
import com.codewithme.compiler.util.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

@Service
public class ProjectService {

    private ProjectRepository projectRepository;
    private UserProjectRepository userProjectRepository;
    private final Map<String, ReentrantLock> projectLocks = new ConcurrentHashMap<>();
    @Autowired
    public ProjectService(ProjectRepository projectRepository, UserProjectRepository userProjectRepository ) {
        this.projectRepository = projectRepository;
        this.userProjectRepository = userProjectRepository;
    }
    @Transactional
    public Project createProject(String projectName, String ownerEmail ,String content) {

        if (ownerEmail == null || ownerEmail.isEmpty()) {
            throw new IllegalArgumentException("Owner email must not be null or empty");
        }

        Optional<Project> existingProject = projectRepository.findByProjectNameAndOwnerEmail(projectName, ownerEmail);
        if (existingProject.isPresent()) {
            throw new IllegalArgumentException("A project with the same name already exists for this owner");
        }

        Version initialVersion = new Version(1.0 ,content , ownerEmail , new Date());

        Project newProject = new Project();
        newProject.setProjectName(projectName);
        newProject.setOwnerEmail(ownerEmail);
        newProject.getVersions().add(initialVersion);
        Project savedProject = projectRepository.save(newProject);

        UserProject userProject = new UserProject();
        userProject.setUserEmail(ownerEmail);
        userProject.setProjectId(savedProject.getId());
        userProject.setRole(Role.OWNER);
        userProjectRepository.save(userProject);

        return savedProject;
    }
    public boolean deleteProject(String projectId, String ownerEmail) {
        ReentrantLock lock = projectLocks.computeIfAbsent(projectId, k -> new ReentrantLock());
        lock.lock();
        try {
            if (ownerEmail == null || ownerEmail.isEmpty() || projectId == null || projectId.isEmpty()) {
                return false;
            }
            Optional<Project> existingProject = projectRepository.findById(projectId);
            if (existingProject.isEmpty()) {
                return false;
            }

            Optional<UserProject> userProjectOptional = userProjectRepository.findByUserEmailAndProjectId(ownerEmail, projectId);

            if (userProjectOptional.isPresent() && Role.OWNER.equals(userProjectOptional.get().getRole())) {
                projectRepository.delete(existingProject.get());
                userProjectRepository.delete(userProjectOptional.get());
                return true;
            }
            return false;
        } finally {
            lock.unlock();
            projectLocks.remove(projectId);
        }
    }

    public List<Project> getProjectsByUserEmail(String userEmail) {

        List<UserProject> userProjects = userProjectRepository.findByUserEmail(userEmail);
        return userProjects.stream()
                .map(userProject -> projectRepository.findById(userProject.getProjectId()).orElse(null))
                .filter(project -> project != null)
                .collect(Collectors.toList());
    }

    public void updateProjectContent(String projectId, String content ,String email) {
        if (email == null || email.isEmpty() || content == null) {
            throw new IllegalArgumentException("Invalid request data: email and content must not be null or empty");
        }

        Optional<UserProject> userProjectOptional = userProjectRepository.findByUserEmailAndProjectId(email, projectId);
        if (userProjectOptional.isPresent() && userProjectOptional.get().getRole()!= Role.VIEWER) {

            Optional<Project> projectOptional = projectRepository.findById(projectId);
            if (projectOptional.isPresent()) {

                Project project = projectOptional.get();

                double lastVersion = project.getVersions().get(project.getVersions().size() - 1).getVersionNumber();
                Version version = new Version(lastVersion+1 ,content ,email ,new Date());

                project.getVersions().add(version);
                projectRepository.save(project);

            } else {
                throw new IllegalArgumentException("Project not found");
            }
        }else{
            throw new IllegalArgumentException("User does not have permission to edit this project");
        }
    }

    @Async
    public void addCommentToProject(String projectId, CommentRequest commentRequest) {
        Optional<Project> projectOptional = projectRepository.findById(projectId);
        if(projectOptional.isPresent()) {
            Project project = projectOptional.get();

            Comment comment = new Comment();
            comment.setContent(commentRequest.getContent());
            comment.setUsername(commentRequest.getUsername());
            comment.setTimestamp(new Date());

            project.addComment(comment);
            projectRepository.save(project);
        }
        else{
            throw new IllegalArgumentException("Project not found");
        }
    }

    public List<Comment> getCommentsByProject(String projectId) {
        Optional<Project> projectOptional = projectRepository.findById(projectId);
        if(projectOptional.isPresent()) {
            Project project = projectOptional.get();
            return project.getComments();
        }
        else{
            throw new IllegalArgumentException("Project not found");
        }
    }


}
