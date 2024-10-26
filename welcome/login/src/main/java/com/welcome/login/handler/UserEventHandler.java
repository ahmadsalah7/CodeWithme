package com.welcome.login.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.core.annotation.HandleBeforeCreate;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import com.welcome.login.entity.User;

@Component
@RepositoryEventHandler(User.class)
public class UserEventHandler {

    private PasswordEncoder passwordEncoder;

    @Autowired
    public void handle(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @HandleBeforeCreate
    public void handleUserCreate(User user) {
        String encodedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encodedPassword);
    }
}
