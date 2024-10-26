package com.codewithme.compiler.controller;

import com.codewithme.compiler.dto.CodeExecutionRequest;
import com.codewithme.compiler.service.CodeExecutionService;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/code")
public class CodeExecutionController {

    private CodeExecutionService codeExecutionService;
    @Autowired
    public CodeExecutionController(CodeExecutionService codeExecutionService) {
        this.codeExecutionService = codeExecutionService;
    }

    @PostMapping("/execute")
    public CompletableFuture<String> executeCode(@RequestBody CodeExecutionRequest request) {
        return codeExecutionService.execute(request.getLanguage(), request.getCode(), request.getInput())
                .exceptionally(e -> "Error during execution: " + e.getMessage());
    }
}
