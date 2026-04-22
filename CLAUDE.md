# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commandes essentielles

```bash
# Compiler et lancer l'application
./mvnw spring-boot:run

# Compiler sans lancer
./mvnw compile

# Lancer les tests
./mvnw test

# Lancer un test spécifique
./mvnw test -Dtest=NomDeLaClasse#nomDeLaMethode

# Packager
./mvnw package
```

L'application tourne sur **http://localhost:8083**.

## Base de données

Oracle Database locale — connexion configurée dans `application.properties` :
- URL : `jdbc:oracle:thin:@localhost:1521/FREEPDB1`
- Schéma : `sprinvoice` / `sprinvoice`
- `ddl-auto=update` : Hibernate synchronise le schéma automatiquement au démarrage
- Les séquences Oracle (`USER_SEQ`, `INVOICE_SEQ`, `CUSTOMER_SEQ`) gèrent les IDs

## Architecture

### Structure des packages (`fr.manu.sprinvoice`)

```
models/       — entités JPA
repositories/ — interfaces Spring Data JPA
services/     — logique métier
controllers/  — controllers Spring MVC (Thymeleaf)
dto/          — objets de transfert pour les formulaires
config/       — configuration Spring Security
```

### Modèle de données

- **`User`** implémente `UserDetails` de Spring Security. Chaque `User` a exactement un `Role` (EAGER) et optionnellement un `Customer` lié.
- **`Role`** : les noms de rôles doivent correspondre exactement (`ROLE_ADMIN`, `CLIENT`) car Spring Security utilise `getAuthorities()` directement depuis `User.getRole().getName()`.
- **`Customer`** est l'entité "client" de facturation. La création d'un `Customer` crée aussi un `User` associé via `CustomerService.createCustomerWithAccount()` dans une transaction unique.
- **`Invoice`** appartient à un `Customer` et contient des `InvoiceRow`. La méthode `Invoice.total()` calcule le montant total en sommant les lignes.

### Sécurité

`SecurityConfig` définit :
- `/auth/**` → accès public (page de login)
- `/admin/**` → accès restreint à `ROLE_ADMIN`
- Tout le reste → authentification requise

`CustomUserDetailsService` charge le `User` depuis la BDD par username. L'entité `User` est directement le `UserDetails` retourné.

### Conventions de routage

- `/auth/*` — authentification
- `/invoices/*` — CRUD factures (utilisateur connecté)
- `/admin/customers/*` — gestion clients (admin uniquement)
