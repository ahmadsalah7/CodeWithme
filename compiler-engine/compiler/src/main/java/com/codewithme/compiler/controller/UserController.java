package com.codewithme.compiler.controller;

import com.codewithme.compiler.entity.User;
import com.codewithme.compiler.repository.UserRepository;
import org.springframework.http.ResponseEntity;
//import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
//import org.springframework.security.oauth2.core.user.OAuth2User;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
public class UserController {


    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @PostMapping("/main")
    public ResponseEntity<String>  receiveUserInfo(@RequestBody Map<String,Object> userInfo  ) throws IOException {
        String email = (String) userInfo.get("email");

        if (email != null) {
            return ResponseEntity.ok("Success");
        } else {
            return ResponseEntity.badRequest().body("Invalid email");
        }
    }

    @PostMapping("/getuserdata")
    public ResponseEntity<Map<String, String>> getUserInfo(@RequestBody Map<String, Object> userInfo) throws IOException {
        String email = (String) userInfo.get("email");

        if (email != null) {
            Optional<User> user = userRepository.findByEmail(email);
            if (user.isPresent()) {
                String username = user.get().getUsername();
                Map<String, String> response = new HashMap<>();
                System.out.println("username: " + username + " email"+user.get().getEmail());
                response.put("username", username);
                response.put("email", email);
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(404).body(null);
            }
        } else {
            return ResponseEntity.badRequest().body(null);
        }
    }

}
