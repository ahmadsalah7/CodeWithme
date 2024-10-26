package com.codewithme.compiler.entity;

import com.codewithme.compiler.util.Role;
import jakarta.persistence.*;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "userprojects")
public class UserProject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_email", nullable = false)
    private String userEmail;

    @Column(name = "project_id", nullable = false)
    private String projectId;

    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    private Role role;

    @Override
    public String toString() {
        return "UserProject{" +
                "userEmail='" + userEmail + '\'' +
                ", projectId='" + projectId + '\'' +
                ", role=" + role +
                '}';
    }

}
