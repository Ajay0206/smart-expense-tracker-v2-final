package com.expensepro.controller;

import com.expensepro.model.User;
import com.expensepro.repository.UserRepository;
import com.expensepro.security.JwtUtil;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired AuthenticationManager authManager;
    @Autowired UserRepository        users;
    @Autowired PasswordEncoder       encoder;
    @Autowired JwtUtil               jwtUtil;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterReq req) {
        if (users.existsByUsername(req.username))
            return ResponseEntity.badRequest().body(Map.of("error", "Username already taken"));
        if (users.existsByEmail(req.email))
            return ResponseEntity.badRequest().body(Map.of("error", "Email already registered"));

        var user = User.builder()
                .username(req.username)
                .email(req.email)
                .passwordHash(encoder.encode(req.password))
                .fullName(req.fullName)
                .currency(req.currency != null ? req.currency : "INR")
                .avatarColor(randomColor())
                .monthlyLimit(BigDecimal.ZERO)
                .build();
        users.save(user);
        return ResponseEntity.ok(Map.of("message", "Registered successfully! Please login."));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginReq req) {
        Authentication auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.username, req.password));
        SecurityContextHolder.getContext().setAuthentication(auth);

        var user = users.findByUsername(req.username).orElseThrow();
        String token = jwtUtil.generate(auth);

        return ResponseEntity.ok(Map.of(
                "token",        token,
                "tokenType",    "Bearer",
                "userId",       user.getId(),
                "username",     user.getUsername(),
                "email",        user.getEmail(),
                "fullName",     user.getFullName(),
                "currency",     user.getCurrency(),
                "avatarColor",  user.getAvatarColor(),
                "monthlyLimit", user.getMonthlyLimit()
        ));
    }

    private String randomColor() {
        String[] colors = {"#6366f1","#10b981","#f59e0b","#ef4444","#3b82f6","#ec4899"};
        return colors[(int)(Math.random() * colors.length)];
    }

    @Data static class RegisterReq {
        @NotBlank @Size(min=3,max=50) public String username;
        @NotBlank @Email               public String email;
        @NotBlank @Size(min=6)         public String password;
        @NotBlank                      public String fullName;
        public String currency;
    }

    @Data static class LoginReq {
        @NotBlank public String username;
        @NotBlank public String password;
    }
}
