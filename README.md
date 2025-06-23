# 🎫 Smart-Ticket

**Smart-Ticket** est un outil intelligent de gestion des signalements et requêtes utilisateurs, conçu pour offrir un traitement centralisé, collaboratif et automatisé à l’aide de l’IA.

---

## 🎯 Objectif

Mettre en place une plateforme intelligente permettant de :

- Collecter et catégoriser les signalements et demandes.
- Prioriser, historiser et traiter rapidement les tickets.
- Exploiter l’intelligence artificielle pour :
  - Regrouper les tickets similaires.
  - Détecter les urgences.
  - Suggérer des réponses pertinentes.
- Favoriser la collaboration entre équipes (modération, support, développement).
- Offrir une expérience fluide, aussi bien pour les utilisateurs que les agents.

---

## 🛠️ Stack Technique

### 🔧 Backend - Java / Spring Boot

- **Architecture 3 couches** : API REST / Service / DAO
- **Java 21**, **Maven**
- **Spring Boot 3.5**, avec les starters :
  - `spring-boot-starter-data-jpa`
  - `spring-boot-starter-web`
  - `spring-boot-starter-security`
  - `spring-boot-starter-actuator`
  - `spring-boot-starter-oauth2-resource-server`
- **Sécurité** : Spring Security + OAuth2 (Keycloak)
- **Documentation API** : OpenAPI avec `springdoc-openapi`
- **Cache distribué** : Redis avec Spring Data Redis
- **Clients HTTP** : Spring Cloud OpenFeign
- **Configuration centralisée** : Spring Config Client
- **Résilience** : Resilience4j (circuit breaker, retry, bulkhead)
- **Mapping DTO ↔ Entity** : MapStruct
- **Requêtes dynamiques** : QueryDSL
- **Code simplifié** : Lombok (`@Data`, `@SuperBuilder`)

---

## 🧠 IA & Traitement Asynchrone

- **Spring AI** : pour interagir avec différents LLMs (OpenAI, Ollama, Hugging Face, etc.)
- **Embeddings** : pgvector
- **Architecture événementielle** :
  - Apache Kafka
  - Utilisé pour le traitement IA asynchrone, calculs d’embeddings, audit, traçabilité, notifications, etc.
  - Producteurs/consommateurs avec `spring-kafka`

---

## 🧪 Tests

- **Tests unitaires** : JUnit 5, Mockito, AssertJ
- **Base mémoire** : H2
- **Tests d’intégration** :
  - Testcontainers (PostgreSQL, Kafka, Redis)
  - WireMock (pour simuler les appels externes, ex. OpenAI)

---

## 🗄️ Base de Données

- **PostgreSQL**
- **pgvector** : gestion des vecteurs pour l’IA
- **PostGIS** : support des données géospatiales
- **Liquibase** :
  - Utilisation d’un fichier principal YAML avec `includeAll` par version (`1.x.x`, `2.x.x`, etc.)

---

## 📌 À venir

- UI Web (dashboard utilisateurs et agents)
- Intégration d'un moteur de recherche sémantique
- Notification en temps réel (WebSockets)

---

## 🚀 Contribuer

Les contributions sont les bienvenues ! Merci de créer une issue ou une pull request.
