package fr.manu.sprinvoice.controllers;

import fr.manu.sprinvoice.models.Customer;
import fr.manu.sprinvoice.models.Role;
import fr.manu.sprinvoice.models.User;
import fr.manu.sprinvoice.repositories.UserRepository;
import fr.manu.sprinvoice.services.CustomerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * TYPE : Test d'intégration web (SpringBootTest + MockMvc + H2)
 *
 * Charge le contexte Spring complet avec une base H2 en mémoire (profil "test").
 * Les services et repositories sont remplacés par des mocks Mockito.
 *
 * Scénarios couverts :
 * - Contrôle d'accès (admin/client) sur les routes /admin/customers/**
 * - Validation du formulaire de création : mot de passe faible → erreur
 * - Username déjà pris → erreur
 * - Données valides → redirection vers la liste
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ActiveProfiles("test")
class AdminCustomerControllerTest {

    @Autowired
    private WebApplicationContext context;

    @MockitoBean private CustomerService customerService;
    @MockitoBean private UserRepository userRepository;

    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
    }

    // ── Utilitaires ────────────────────────────────────────────────

    private User buildAdmin() {
        Role role = new Role();
        role.setName("ROLE_ADMIN");
        User u = new User();
        u.setId(1);
        u.setUsername("admin");
        u.setPassword("pass");
        u.setRole(role);
        return u;
    }

    private User buildClient() {
        Role role = new Role();
        role.setName("ROLE_CLIENT");
        Customer c = new Customer();
        c.setId(5);
        User u = new User();
        u.setId(2);
        u.setUsername("client");
        u.setPassword("pass");
        u.setRole(role);
        u.setCustomer(c);
        return u;
    }

    // ── Tests contrôle d'accès ─────────────────────────────────────

    /**
     * Un admin peut afficher la liste des clients (HTTP 200).
     */
    @Test
    void list_asAdmin_returns200() throws Exception {
        when(customerService.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/admin/customers").with(user(buildAdmin())))
                .andExpect(status().isOk());
    }

    /**
     * Un client ne peut PAS accéder à la liste admin → HTTP 403.
     */
    @Test
    void list_asClient_returns403() throws Exception {
        mockMvc.perform(get("/admin/customers").with(user(buildClient())))
                .andExpect(status().isForbidden());
    }

    /**
     * Un admin peut afficher le formulaire de création de client (HTTP 200).
     */
    @Test
    void createForm_asAdmin_returns200() throws Exception {
        mockMvc.perform(get("/admin/customers/create").with(user(buildAdmin())))
                .andExpect(status().isOk());
    }

    /**
     * Un client ne peut PAS afficher le formulaire de création → HTTP 403.
     */
    @Test
    void createForm_asClient_returns403() throws Exception {
        mockMvc.perform(get("/admin/customers/create").with(user(buildClient())))
                .andExpect(status().isForbidden());
    }

    // ── Tests validation du formulaire de création ─────────────────

    /**
     * Soumettre le formulaire avec un mot de passe faible (< 8 chars,
     * pas de majuscule, pas de chiffre) doit rester sur le formulaire
     * avec un message d'erreur (HTTP 200 + attribut "error" dans le modèle).
     */
    @Test
    void create_withInvalidPassword_returnsFormWithError() throws Exception {
        mockMvc.perform(post("/admin/customers/create")
                        .with(csrf())
                        .with(user(buildAdmin()))
                        .param("username", "nouveauclient")
                        .param("password", "weak")
                        .param("name", "Client Test")
                        .param("delay", "30"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("error"));
    }

    /**
     * Soumettre le formulaire avec un nom d'utilisateur déjà existant
     * doit rester sur le formulaire avec un message d'erreur.
     */
    @Test
    void create_withUsernameAlreadyTaken_returnsFormWithError() throws Exception {
        when(userRepository.findByUsername("existant")).thenReturn(Optional.of(new User()));

        mockMvc.perform(post("/admin/customers/create")
                        .with(csrf())
                        .with(user(buildAdmin()))
                        .param("username", "existant")
                        .param("password", "Admin123!")
                        .param("name", "Client Test")
                        .param("delay", "30"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("error"));
    }

    /**
     * Soumettre le formulaire avec des données valides doit créer le client
     * et rediriger vers /admin/customers.
     */
    @Test
    void create_withValidData_redirectsToList() throws Exception {
        when(userRepository.findByUsername("nouveauclient")).thenReturn(Optional.empty());
        doNothing().when(customerService).createCustomerWithAccount(any());

        mockMvc.perform(post("/admin/customers/create")
                        .with(csrf())
                        .with(user(buildAdmin()))
                        .param("username", "nouveauclient")
                        .param("password", "Admin123!")
                        .param("name", "Client Test")
                        .param("delay", "30"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/customers"));
    }

    /**
     * La suppression d'un client (POST admin) doit rediriger vers la liste.
     */
    @Test
    void delete_asAdmin_redirectsToList() throws Exception {
        doNothing().when(customerService).deleteById(3);

        mockMvc.perform(post("/admin/customers/3/delete")
                        .with(csrf())
                        .with(user(buildAdmin())))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/customers"));
    }
}
