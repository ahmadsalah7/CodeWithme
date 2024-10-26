package com.welcome.login.controller;


import com.nimbusds.jwt.JWT;
import com.welcome.login.entity.User;
import com.welcome.login.repository.UserRepository;
import com.welcome.login.util.Jwt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
public class LoginController {

    private final UserRepository userRepository;
    private PasswordEncoder passwordEncoder;

    public LoginController(UserRepository userRepository , PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/oauth/github")
    public RedirectView redirectToGithub() {
        return new RedirectView("/oauth2/authorization/github");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User loginUser) {

        Optional<User> user = userRepository.findByEmail(loginUser.getEmail());

        if (user.isPresent() && passwordEncoder.matches(loginUser.getPassword(), user.get().getPassword())) {
            System.out.println(user.get().getEmail());
            String token = Jwt.generateToken(user.get().getEmail());
            Map<String,String> response = new HashMap<>();
            response.put("token", token);
            response.put("email" , user.get().getEmail());

            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(401).body("Login failed");
        }

    }
}
