package com.codewithme.compiler.mappers;

import com.codewithme.compiler.dto.CodeExecutionResource;
import com.codewithme.compiler.models.CodeExecutionModel;
import org.mapstruct.Mapper;

@Mapper
public interface CodeExecutionMapper {
        CodeExecutionModel toModel(CodeExecutionResource resource);
}
