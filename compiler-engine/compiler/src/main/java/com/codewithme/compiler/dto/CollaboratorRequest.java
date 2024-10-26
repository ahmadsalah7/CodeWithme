package com.codewithme.compiler.dto;


import com.codewithme.compiler.util.Role;
import lombok.*;

@Data
public class CollaboratorRequest {
    private String ownerEmail;
    private String collaboratorEmail;
    private String projectId;
    private String role;
}
