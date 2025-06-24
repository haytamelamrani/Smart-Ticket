package com.example.demo.service;

import com.example.demo.dto.RegisterRequest;
import com.example.demo.entity.PasswordResetToken;
import com.example.demo.entity.User;
import com.example.demo.repository.PasswordResetTokenRepository;
import com.example.demo.repository.UserRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final PasswordResetTokenRepository resetTokenRepository;
    private final JavaMailSender mailSender;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final ConcurrentHashMap<String, TempUser> pendingUsers = new ConcurrentHashMap<>();

    // ✅ Clé sécurisée : au moins 32 caractères
    private static final String JWT_SECRET_STRING = "myVerySecureSecretKeyThatIsLongEnough!";
    private static final SecretKey JWT_SECRET = new SecretKeySpec(
            JWT_SECRET_STRING.getBytes(StandardCharsets.UTF_8),
            SignatureAlgorithm.HS256.getJcaName()
    );

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
                .claim("role", "USER")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 86400000)) // 24h
                .signWith(JWT_SECRET, SignatureAlgorithm.HS256)
                .compact();

        System.out.println("✅ Connexion réussie - Token généré");
        return Map.of("message", "✅ Connexion réussie", "token", token);
    }

    public String requestPasswordReset(String email) {
    Optional<User> userOpt = userRepository.findByEmail(email);
    if (userOpt.isEmpty()) return "❌ Email introuvable.";

    String resetToken = UUID.randomUUID().toString();
    String resetLink = "http://localhost:3000/forgetpassword/changepassword?token=" + resetToken;


    resetTokenRepository.save(
        PasswordResetToken.builder()
            .email(email)
            .token(resetToken)
            .expiration(LocalDateTime.now().plusMinutes(30))
            .build()
    );

    if (sendEmail(email, "Réinitialisation de mot de passe",
            "Cliquez ici pour réinitialiser votre mot de passe :\n" + resetLink)) {
        return "📧 Lien de réinitialisation envoyé.";
    } else {
        return "⚠️ Échec de l'envoi de l'email.";
    }
    }

    @Transactional
    public String resetPasswordByToken(String token, String newPassword) {
        System.out.println("🔑 Vérification du token reçu : " + token);
    
        Optional<PasswordResetToken> tokenOpt = resetTokenRepository.findByToken(token);
        if (tokenOpt.isEmpty()) {
            System.out.println("❌ Token introuvable");
            return "❌ Token invalide.";
        }
    
        PasswordResetToken resetToken = tokenOpt.get();
        System.out.println("📧 Email lié au token : " + resetToken.getEmail());
    
        if (resetToken.getExpiration().isBefore(LocalDateTime.now())) {
            System.out.println("⏰ Token expiré");
            return "⏰ Token expiré.";
        }
    
        Optional<User> userOpt = userRepository.findByEmail(resetToken.getEmail());
        if (userOpt.isEmpty()) {
            System.out.println("❌ Utilisateur introuvable avec cet email");
            return "❌ Utilisateur introuvable.";
        }
    
        User user = userOpt.get();
        String hashed = passwordEncoder.encode(newPassword);
        System.out.println("🔐 Nouveau mot de passe hashé : " + hashed);
    
        user.setPassword(hashed);
        userRepository.save(user);
        System.out.println("💾 Mot de passe mis à jour avec succès");
    
        resetTokenRepository.delete(resetToken);
        System.out.println("🧹 Token supprimé de la base");
    
        return "✅ Mot de passe réinitialisé avec succès.";
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
