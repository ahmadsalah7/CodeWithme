package com.codewithme.compiler.service;

import com.codewithme.compiler.entity.User;
import com.codewithme.compiler.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {
    private UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public boolean exists(String email){
        Optional<User> user = userRepository.findByEmail(email);
        return user.isPresent();
    }
}
