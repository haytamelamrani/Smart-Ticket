package com.example.demo.exception;

import com.example.demo.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // Gérer les erreurs de validation des DTOs (@Valid)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST) // Définit le statut HTTP de la réponse
    public ResponseEntity<ErrorResponse> handleValidationExceptions(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Erreur de validation des données d'entrée.",
                request.getRequestURI(),
                errors
        );
        logger.warn("Erreur de validation: {} sur le chemin: {}. Détails: {}", errorResponse.getMessage(), request.getRequestURI(), errors);
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    // Exemple pour une exception personnalisée (à créer si nécessaire)
    // @ExceptionHandler(UserNotFoundException.class) // Remplacez par votre exception
    // @ResponseStatus(HttpStatus.NOT_FOUND)
    // public ResponseEntity<ErrorResponse> handleUserNotFoundException(
    //         UserNotFoundException ex, HttpServletRequest request) {
    //     ErrorResponse errorResponse = new ErrorResponse(
    //             HttpStatus.NOT_FOUND.value(),
    //             ex.getMessage(), // Le message de votre exception personnalisée
    //             request.getRequestURI()
    //     );
    //     logger.warn("Ressource non trouvée: {} sur le chemin: {}", ex.getMessage(), request.getRequestURI());
    //     return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    // }

    // Exemple pour une autre exception personnalisée
    // @ExceptionHandler(EmailAlreadyExistsException.class) // Remplacez par votre exception
    // @ResponseStatus(HttpStatus.CONFLICT)
    // public ResponseEntity<ErrorResponse> handleEmailAlreadyExistsException(
    //         EmailAlreadyExistsException ex, HttpServletRequest request) {
    //     ErrorResponse errorResponse = new ErrorResponse(
    //             HttpStatus.CONFLICT.value(),
    //             ex.getMessage(),
    //             request.getRequestURI()
    //     );
    //     logger.warn("Conflit de données: {} sur le chemin: {}", ex.getMessage(), request.getRequestURI());
    //     return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    // }


    // Gérer les erreurs d'authentification/autorisation de Spring Security (plus spécifique)
    // Ces erreurs sont souvent déjà gérées par les entry points et access denied handlers de Spring Security,
    // mais on peut les personnaliser ici si on veut un format de réponse JSON cohérent.
    // @ExceptionHandler(org.springframework.security.core.AuthenticationException.class)
    // @ResponseStatus(HttpStatus.UNAUTHORIZED)
    // public ResponseEntity<ErrorResponse> handleAuthenticationException(
    //         org.springframework.security.core.AuthenticationException ex, HttpServletRequest request) {
    //     ErrorResponse errorResponse = new ErrorResponse(
    //             HttpStatus.UNAUTHORIZED.value(),
    //             "Erreur d'authentification: " + ex.getMessage(),
    //             request.getRequestURI()
    //     );
    //     logger.warn("Erreur d'authentification: {} sur le chemin: {}", ex.getMessage(), request.getRequestURI());
    //     return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    // }

    // @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    // @ResponseStatus(HttpStatus.FORBIDDEN)
    // public ResponseEntity<ErrorResponse> handleAccessDeniedException(
    //         org.springframework.security.access.AccessDeniedException ex, HttpServletRequest request) {
    //     ErrorResponse errorResponse = new ErrorResponse(
    //             HttpStatus.FORBIDDEN.value(),
    //             "Accès refusé: " + ex.getMessage(),
    //             request.getRequestURI()
    //     );
    //     logger.warn("Accès refusé: {} sur le chemin: {}", ex.getMessage(), request.getRequestURI());
    //     return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
    // }


    // Handler générique pour toutes les autres exceptions non interceptées
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR) // Définit le statut HTTP de la réponse
    public ResponseEntity<ErrorResponse> handleAllUncaughtException(
            Exception ex, HttpServletRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Une erreur interne inattendue est survenue. Veuillez réessayer plus tard.",
                // "Message: " + ex.getMessage(), // En développement, on peut vouloir le message, mais pas en production.
                request.getRequestURI()
        );
        // Logguer l'erreur complète pour le débogage
        logger.error("Erreur interne inattendue sur le chemin: {}: {}", request.getRequestURI(), ex.getMessage(), ex);
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
