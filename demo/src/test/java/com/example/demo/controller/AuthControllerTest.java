package com.example.demo.controller;

import com.example.demo.dto.LoginRequest;
import com.example.demo.dto.RegisterRequest;
import com.example.demo.entity.User;
import com.example.demo.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// @WebMvcTest se concentre sur la couche Web (contrôleurs) et désactive le scan complet des composants.
// Il faut fournir les mocks pour les dépendances des contrôleurs (comme AuthService).
// Par défaut, Spring Security est aussi actif. Si on veut le désactiver pour des tests simples de contrôleur :
// import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
// @AutoConfigureMockMvc(addFilters = false) // Désactive les filtres Spring Security
// Mais pour tester AuthController, il est bon de garder la sécurité active pour voir comment elle interagit.
// Cependant, JwtAuthenticationFilter ne sera pas complètement testé ici car il dépend de la clé secrète
// qui est chargée via @Value. Pour des tests d'intégration complets de la sécurité, @SpringBootTest est mieux.
// Pour l'instant, nous allons tester les endpoints /api/auth/** qui sont permis par SecurityConfig.
@WebMvcTest(AuthController.class)
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean // Crée un mock de AuthService et l'injecte dans le contexte d'application pour ce test
    private AuthService authService;

    @MockBean // JwtAuthenticationFilter a besoin de UserRepository, qui n'est pas chargé par @WebMvcTest
    private com.example.demo.repository.UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper; // Pour convertir les objets en JSON

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest();
        registerRequest.setFirstName("Test");
        registerRequest.setLastName("User");
        registerRequest.setEmail("test@example.com");
        registerRequest.setPassword("ValidPassword123"); // Doit être valide selon les DTO contraintes
        registerRequest.setCompany("TestCorp");

        loginRequest = new LoginRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("ValidPassword123");
    }

    @Test
    void register_whenValidRequest_shouldReturnSuccessMessage() throws Exception {
        when(authService.register(any(RegisterRequest.class))).thenReturn("✅ Code envoyé. Vérifiez votre email.");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk())
                .andExpect(content().string("✅ Code envoyé. Vérifiez votre email."));
    }

    @Test
    void register_whenInvalidRequest_shouldReturnBadRequest() throws Exception {
        RegisterRequest invalidRequest = new RegisterRequest(); // Champs manquants
        invalidRequest.setEmail("invalidemail"); // Format email incorrect

        // La validation des DTO est gérée par Spring avant d'appeler la méthode du contrôleur.
        // Donc, authService.register ne sera même pas appelé.
        // @WebMvcTest active la validation par défaut.

        ResultActions result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest()); // Attend un statut 400

        // On peut vérifier les messages d'erreur spécifiques si GlobalExceptionHandler est actif
        // et que @WebMvcTest le charge (il devrait s'il est dans le même package ou un sous-package scanné).
        // Si GlobalExceptionHandler est bien pris en compte:
        result.andExpect(jsonPath("$.statusCode").value(400))
              .andExpect(jsonPath("$.message").value("Erreur de validation des données d'entrée."))
              .andExpect(jsonPath("$.validationErrors.firstName").exists()) // ou le message exact
              .andExpect(jsonPath("$.validationErrors.lastName").exists())
              .andExpect(jsonPath("$.validationErrors.email").value("Le format de l'email est invalide."))
              .andExpect(jsonPath("$.validationErrors.password").exists());
    }


    @Test
    void login_whenValidCredentials_shouldReturnToken() throws Exception {
        Map<String, String> successResponse = Map.of(
                "message", "✅ Connexion réussie",
                "token", "mocked.jwt.token"
        );
        when(authService.login(anyString(), anyString())).thenReturn(successResponse);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("✅ Connexion réussie"))
                .andExpect(jsonPath("$.token").value("mocked.jwt.token"));
    }

    @Test
    void login_whenInvalidCredentials_shouldReturnUnauthorized() throws Exception {
        Map<String, String> errorResponse = Map.of("error", "❌ Mot de passe incorrect.");
        when(authService.login(anyString(), anyString())).thenReturn(errorResponse);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized()) // AuthService retourne 401 dans le controller
                .andExpect(jsonPath("$.error").value("❌ Mot de passe incorrect."));
    }

    @Test
    void login_whenInvalidRequestFormat_shouldReturnBadRequest() throws Exception {
        LoginRequest invalidRequest = new LoginRequest(); // Champs email/password manquants

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value(400))
                .andExpect(jsonPath("$.validationErrors.email").exists())
                .andExpect(jsonPath("$.validationErrors.password").exists());
    }

    // TODO: Ajouter des tests pour /verify-otp, /forgot-password, /reset-password
    // Ces tests suivront un schéma similaire, en moquant la réponse de AuthService
    // et en vérifiant le statut et le corps de la réponse HTTP.
}
