package com.codewithme.compiler.dto;

import lombok.*;

@Data
public class CodeExecutionRequest {
    private String language;
    private String code;
    private String input;
}
