package com.codewithme.compiler.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Version {
    private double versionNumber;
    private String content;
    private String editedBy;
    private Date timestamp;

}
