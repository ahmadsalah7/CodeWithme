package com.codewithme.compiler.dto;

import lombok.*;

@Data
public class CollaboratorDeletetionRequest {
    private String ownerEmail;
    private String collaboratorEmail;
    private String projectId;
}
