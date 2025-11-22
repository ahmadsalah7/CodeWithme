package com.codewithme.compiler.controller;

import com.codewithme.compiler.dto.CodeExecutionResource;
import com.codewithme.compiler.mappers.CodeExecutionMapper;
import com.codewithme.compiler.service.CodeExecutionService;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/code")
public class CodeExecutionController {

    private final CodeExecutionService codeExecutionService;
    private final CodeExecutionMapper mapper;
    @Autowired
    public CodeExecutionController(CodeExecutionService codeExecutionService, CodeExecutionMapper mapper) {
        this.codeExecutionService = codeExecutionService;
        this.mapper = mapper;
    }

    @PostMapping("/execute")
    public CompletableFuture<String> executeCode(@RequestBody CodeExecutionResource resource) {
        return codeExecutionService.execute(mapper.toModel(resource))
                .exceptionally(e -> "Error during execution: " + e.getMessage());
    }
}
