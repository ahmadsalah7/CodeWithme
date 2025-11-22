package com.codewithme.compiler.models;

import lombok.Data;

@Data
public class CodeExecutionModel {
    private String language;
    private String code;
    private String input;
}
