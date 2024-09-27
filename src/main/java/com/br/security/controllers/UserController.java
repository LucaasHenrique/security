package com.br.security.controllers;

import com.br.security.entity.Role;
import com.br.security.entity.User;
import com.br.security.entity.dto.CreateUserDto;
import com.br.security.repository.RoleRepository;
import jakarta.transaction.Transactional;
import org.apache.coyote.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.br.security.repository.UserRepository;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Set;

@RestController
public class UserController {
  
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserController (UserRepository userRepository, RoleRepository roleRepository, BCryptPasswordEncoder passwordEncoder) {
      this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/register")
    @Transactional
    public ResponseEntity<Void> newUser(@RequestBody CreateUserDto dto) {
        var basicRole = roleRepository.findByName(Role.Values.BASIC.name());

        var userFromDb = userRepository.findByUsername(dto.username());
        if (userFromDb.isPresent()) {
          throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY); 
        }

        var user = new User();
        user.setUsername(dto.username());
        user.setPasssword(passwordEncoder.encode(dto.password()));
        user.setRoles(Set.of(basicRole));

        userRepository.save(user);

        return ResponseEntity.ok().build();
    }

    @GetMapping("/users")
    @PreAuthorize("hasAuthority('SCOPE_ADMIN')")
    public ResponseEntity<List<User>> listUsers() {
        var users = userRepository.findAll();
        return ResponseEntity.ok(users);
    }


}
