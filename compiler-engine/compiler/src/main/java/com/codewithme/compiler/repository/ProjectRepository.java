package com.codewithme.compiler.repository;

import com.codewithme.compiler.entity.Project;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProjectRepository extends MongoRepository<Project, String> {
    Optional<Project> findByProjectNameAndOwnerEmail(String projectName, String ownerEmail);
}