package com.codewithme.compiler.dto;
import lombok.*;

@Data
public  class CreateProjectRequest {
    private String projectName;
    private String ownerEmail;
    private String content ;
    // create intial project content then edit the create from js set the content empty and check if the endpoint called any other place
    // then in the service if the content set the content after creating the project as first version content

}