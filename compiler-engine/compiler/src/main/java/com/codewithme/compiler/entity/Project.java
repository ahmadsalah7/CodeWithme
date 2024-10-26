    package com.codewithme.compiler.entity;
    
    import lombok.*;
    import org.springframework.data.annotation.*;
    import org.springframework.data.mongodb.core.mapping.Document;
    
    import java.util.ArrayList;
    import java.util.List;
    
    @Data
    @Document(collection = "projects")
    public class Project {
    
        @Id
        private String id;
        private String projectName;
        private String ownerEmail;
        @org.springframework.data.annotation.Version
        private Integer version;
        private List<Version> versions = new ArrayList<>();
        private List<Comment> comments = new ArrayList<>();
    
        public void addComment(Comment comment) {
            comments.add(comment);
        }
        @Override
        public String toString() {
            return "Project{" +
                    "projectName='" + projectName + '\'' +
                    ", ownerEmail='" + ownerEmail + '\'' +
                    '}';
        }
    }
