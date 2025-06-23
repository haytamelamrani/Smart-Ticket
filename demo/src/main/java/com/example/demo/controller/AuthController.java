package com.example.demo.controller;

import com.example.demo.dto.LoginRequest;
import com.example.demo.dto.RegisterRequest;
import com.example.demo.dto.ResetPasswordRequest;
import com.example.demo.service.AuthService;
import jakarta.validation.Valid; // Import pour @Valid
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) { // Ajout de @Valid
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<String> verifyOtp(@RequestBody Map<String, String> payload) {
        // Pour verify-otp et forgot-password, la validation est plus simple (juste email/otp).
        // On pourrait créer des DTOs spécifiques si on voulait une validation plus poussée via annotations.
        // Pour l'instant, on garde la Map, mais on pourrait ajouter des vérifications manuelles si nécessaire.
        String email = payload.get("email");
        String code = payload.get("otp");
        if (email == null || email.trim().isEmpty() || code == null || code.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("L'email et le code OTP sont requis.");
        }
        return ResponseEntity.ok(authService.verifyOtp(email, code));
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@Valid @RequestBody LoginRequest loginRequest) { // Ajout de @Valid
        Map<String, String> result = authService.login(loginRequest.getEmail(), loginRequest.getPassword());

        if (result.containsKey("error")) {
            return ResponseEntity.status(401).body(result);
        }

        return ResponseEntity.ok(result);
    }


    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestBody Map<String, String> payload) {
        String email = payload.get("email");
        if (email == null || email.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("L'email est requis.");
        }
        // Idéalement, valider aussi le format de l'email ici manuellement ou via un DTO simple.
        return ResponseEntity.ok(authService.requestPasswordReset(email));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@Valid @RequestBody ResetPasswordRequest request) { // Ajout de @Valid
        return ResponseEntity.ok(authService.resetPassword(request.getEmail(), request.getNewPassword()));
    }
}
