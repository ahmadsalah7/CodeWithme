package com.codewithme.compiler.entity;

import jakarta.persistence.*;
import lombok.*;

@Data
@Entity
@Table(name="user")
public class User {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name="id")
    private int id;

    @Setter
    @Column(name="username")
    private String username;

    @Column(name="email")
    private String email;

    @Column(name="password")
    private String password;

    @Column(name="provider_type")
    private String providerType;

    @Column(name="provider_id")
    private String providerId;

    @Override
    public String toString() {
        return "User{" +
                "email='" + email + '\'' +
                ", username='" + username + '\'' +
                ", id=" + id +
                '}';
    }
}
