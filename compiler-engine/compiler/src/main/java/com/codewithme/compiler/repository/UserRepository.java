package com.codewithme.compiler.repository;

import com.codewithme.compiler.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import java.util.Optional;

@RepositoryRestResource(path="users")
public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByEmail(@Param("email")String email);
}
