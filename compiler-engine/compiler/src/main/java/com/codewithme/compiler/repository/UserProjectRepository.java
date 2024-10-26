package com.codewithme.compiler.repository;

import com.codewithme.compiler.entity.UserProject;
import com.codewithme.compiler.util.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public interface UserProjectRepository extends JpaRepository<UserProject, Long> {
    List<UserProject> findByUserEmail(String userEmail);
    Optional<UserProject> findByUserEmailAndProjectIdAndRole(String userEmail, String projectId, Role role);
    Optional<UserProject> findByUserEmailAndProjectId(String userEmail, String projectId);
}
