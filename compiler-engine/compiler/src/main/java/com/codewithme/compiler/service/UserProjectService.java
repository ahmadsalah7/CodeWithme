package com.codewithme.compiler.service;

import com.codewithme.compiler.entity.User;
import com.codewithme.compiler.entity.UserProject;
import com.codewithme.compiler.repository.UserProjectRepository;
import com.codewithme.compiler.util.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class UserProjectService {

    UserProjectRepository userProjectRepository;
    private final Map<String, ReentrantLock> projectLocks = new ConcurrentHashMap<>();

    @Autowired
    public UserProjectService(UserProjectRepository userProjectRepository) {
        this.userProjectRepository = userProjectRepository;
    }
    public boolean isOwner (String projectId , String email){
        Optional<UserProject> userProjectOptional = userProjectRepository.findByUserEmailAndProjectIdAndRole(email, projectId, Role.OWNER);
        return userProjectOptional.isPresent();
    }
    public boolean isViewer (String projectId , String email){
        Optional<UserProject> userProjectOptional = userProjectRepository.findByUserEmailAndProjectIdAndRole(email, projectId, Role.VIEWER);
        return userProjectOptional.isPresent();
    }

    public boolean addCollaborator(String collaboratorEmail, String projectId, String role) {
        ReentrantLock lock = projectLocks.computeIfAbsent(projectId, k -> new ReentrantLock());
        lock.lock();
        try {
            Optional<UserProject> userProjectOptional = userProjectRepository.findByUserEmailAndProjectId(collaboratorEmail, projectId);

            Role roleEnum;
            try {
                roleEnum = Role.valueOf(role.toUpperCase());
            } catch (IllegalArgumentException e) {
                System.err.println("Invalid role provided: " + role);
                return false;
            }

            if (userProjectOptional.isPresent()) {
                UserProject existingProject = userProjectOptional.get();
                System.out.println("User already assigned to the project. Current role: " + existingProject.getRole());

                if (existingProject.getRole() == Role.OWNER) {
                    System.out.println("Cannot change role of an OWNER.");
                    return false;
                } else {
                    existingProject.setRole(roleEnum);
                    userProjectRepository.save(existingProject);
                    System.out.println("User role updated to: " + roleEnum);
                    return true;
                }
            } else {
                UserProject newUserProject = new UserProject();
                newUserProject.setUserEmail(collaboratorEmail);
                newUserProject.setProjectId(projectId);
                newUserProject.setRole(roleEnum);
                userProjectRepository.save(newUserProject);
                return true;
            }
        } catch (Exception e) {
            System.err.println("Error occurred while adding collaborator: " + e.getMessage());
            e.printStackTrace();
            return false;
        }finally {
            lock.unlock();
            projectLocks.remove(projectId);
        }
    }

    public boolean deleteCollaborator(String collaboratorEmail, String projectId) {
        ReentrantLock lock = projectLocks.computeIfAbsent(projectId, k -> new ReentrantLock());
        lock.lock();
        try {
            Optional<UserProject> userProjectOptional = userProjectRepository.findByUserEmailAndProjectId(collaboratorEmail, projectId);

            if (userProjectOptional.isPresent() && userProjectOptional.get().getRole() != Role.OWNER) {
                userProjectRepository.delete(userProjectOptional.get());
                System.out.println("Collaborator deleted: " + collaboratorEmail);
                return true;
            } else {
                System.out.println("Collaborator not found in the project");
                return false;
            }
        } catch (Exception e) {
            System.err.println("Error occurred while deleting collaborator: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            lock.unlock();
            projectLocks.remove(projectId);
        }
    }

}
