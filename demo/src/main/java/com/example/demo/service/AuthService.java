package com.example.demo.service;

import com.example.demo.dto.RegisterRequest;
import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.annotation.PostConstruct; // Import pour @PostConstruct
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value; // Import pour @Value
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final JavaMailSender mailSender;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final ConcurrentHashMap<String, TempUser> pendingUsers = new ConcurrentHashMap<>();

    @Value("${jwt.secret.key}") // Injection de la clé depuis application.properties
    private String jwtSecretString;

    private SecretKey jwtSecretKey; // La SecretKey sera initialisée dans init()

    @PostConstruct // Exécuté après l'injection des dépendances
    public void init() {
        this.jwtSecretKey = new SecretKeySpec(
                jwtSecretString.getBytes(StandardCharsets.UTF_8),
                SignatureAlgorithm.HS256.getJcaName()
        );
    }

    public String register(RegisterRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            return "❌ Cet email est déjà utilisé.";
        }

        String otpCode = generateOtpCode();
        TempUser tempUser = new TempUser(
                request.getFirstName(),
                request.getLastName(),
                request.getEmail(),
                passwordEncoder.encode(request.getPassword()),
                request.getCompany(),
                otpCode,
                LocalDateTime.now()
        );

        pendingUsers.put(request.getEmail(), tempUser);

        if (sendEmail(request.getEmail(), "Code de vérification - Smart Ticket",
                "Voici votre code de vérification : " + otpCode + "\nValide 10 minutes.")) {
            return "✅ Code envoyé. Vérifiez votre email.";
        } else {
            pendingUsers.remove(request.getEmail());
            return "⚠️ Échec de l'envoi de l'email.";
        }
    }

    public String verifyOtp(String email, String code) {
        TempUser tempUser = pendingUsers.get(email);
        if (tempUser == null) return "❌ Aucun utilisateur en attente.";
        if (!tempUser.otpCode().equals(code)) return "❌ Code incorrect.";
        if (tempUser.otpGeneratedAt().plusMinutes(10).isBefore(LocalDateTime.now()))
            return "⏰ Code expiré.";

        User user = User.builder()
                .firstName(tempUser.firstName())
                .lastName(tempUser.lastName())
                .email(tempUser.email())
                .password(tempUser.password())
                .company(tempUser.company())
                .isVerified(true)
                .build();

        userRepository.save(user);
        pendingUsers.remove(email);

        return "✅ Compte vérifié avec succès.";
    }

    public Map<String, String> login(String email, String rawPassword) {
        System.out.println("🔐 Tentative de login pour : " + email);

        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            System.out.println("❌ Utilisateur non trouvé : " + email);
            return Map.of("error", "❌ Utilisateur non trouvé.");
        }

        User user = userOpt.get();

        if (!user.isVerified()) {
            System.out.println("⚠️ Compte non vérifié");
            return Map.of("error", "⚠️ Compte non vérifié.");
        }

        System.out.println("🔑 Mot de passe tapé : " + rawPassword);
        System.out.println("🔐 Mot de passe en base : " + user.getPassword());
        System.out.println("✅ passwordEncoder: " + passwordEncoder.getClass().getName());
        System.out.println("✅ Match result: " + passwordEncoder.matches(rawPassword, user.getPassword()));

        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            System.out.println("❌ Mot de passe incorrect");
            return Map.of("error", "❌ Mot de passe incorrect.");
        }

        String token = Jwts.builder()
                .setSubject(user.getEmail())
                .claim("role", "USER") // TODO: Gérer les rôles dynamiquement si nécessaire
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 86400000)) // 24 heures de validité
                .signWith(this.jwtSecretKey, SignatureAlgorithm.HS256) // Utilisation de la clé initialisée
                .compact();

        System.out.println("✅ Connexion réussie - Token généré");
        return Map.of("message", "✅ Connexion réussie", "token", token);
    }

    public String requestPasswordReset(String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) return "❌ Email introuvable.";

        String resetToken = UUID.randomUUID().toString();
        String resetLink = "http://localhost:3000/reset-password?token=" + resetToken;

        if (sendEmail(email, "Réinitialisation de mot de passe",
                "Cliquez ici pour réinitialiser votre mot de passe :\n" + resetLink)) {
            return "📧 Lien de réinitialisation envoyé.";
        } else {
            return "⚠️ Échec de l'envoi de l'email.";
        }
    }

    public String resetPassword(String email, String newPassword) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) return "❌ Utilisateur introuvable.";

        User user = userOpt.get();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        return "✅ Mot de passe mis à jour.";
    }

    private boolean sendEmail(String to, String subject, String text) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("smartlearn907@gmail.com");
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            mailSender.send(message);
            return true;
        } catch (Exception e) {
            System.err.println("❌ Erreur email : " + e.getMessage());
            return false;
        }
    }

    private String generateOtpCode() {
        return String.valueOf(new Random().nextInt(900_000) + 100_000);
    }

    private record TempUser(
            String firstName,
            String lastName,
            String email,
            String password,
            String company,
            String otpCode,
            LocalDateTime otpGeneratedAt
    ) {}
}
