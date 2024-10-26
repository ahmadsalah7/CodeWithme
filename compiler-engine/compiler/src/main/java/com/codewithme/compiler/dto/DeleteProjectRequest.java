package com.codewithme.compiler.dto;

import lombok.Data;

@Data
public class DeleteProjectRequest {
    String projectId ;
    String ownerEmail;
}
