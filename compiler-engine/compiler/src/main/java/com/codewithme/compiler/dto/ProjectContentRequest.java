package com.codewithme.compiler.dto;

import lombok.*;

@Data
public class ProjectContentRequest {
    private String projectId;
    private String content;
    private String email;
}
