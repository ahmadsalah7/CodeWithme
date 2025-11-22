package com.codewithme.compiler.dto;

import lombok.*;

@Data
public class CodeExecutionResource {
    private String language;
    private String code;
    private String input;
}
