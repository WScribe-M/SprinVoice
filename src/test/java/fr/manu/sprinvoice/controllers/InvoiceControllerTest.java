package fr.manu.sprinvoice.controllers;

import fr.manu.sprinvoice.models.*;
import fr.manu.sprinvoice.services.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;

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
 * Les services sont remplacés par des mocks Mockito.
 * Aucun serveur HTTP réel n'est démarré.
 *
 * Scénarios couverts :
 * - Accès non authentifié → redirection vers login
 * - Accès admin → accès complet à toutes les factures
 * - Accès client → uniquement ses propres factures
 * - IDOR : un client ne peut PAS voir la facture d'un autre → 403
 * - Routes /admin/** inaccessibles aux clients → 403
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ActiveProfiles("test")
class InvoiceControllerTest {

    @Autowired
    private WebApplicationContext context;

    @MockitoBean private InvoiceService invoiceService;
    @MockitoBean private CustomerService customerService;
    @MockitoBean private ProductService productService;
    @MockitoBean private InvoiceRowService invoiceRowService;

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

    private User buildClient(int customerId) {
        Role role = new Role();
        role.setName("ROLE_CLIENT");
        Customer customer = new Customer();
        customer.setId(customerId);
        User u = new User();
        u.setId(100 + customerId);
        u.setUsername("client" + customerId);
        u.setPassword("pass");
        u.setRole(role);
        u.setCustomer(customer);
        return u;
    }

    private Invoice buildInvoice(int invoiceId, int customerId) {
        Customer customer = new Customer();
        customer.setId(customerId);
        Invoice invoice = new Invoice();
        invoice.setId(invoiceId);
        invoice.setDesignation("Facture " + invoiceId);
        invoice.setCustomer(customer);
        invoice.setRows(List.of());
        return invoice;
    }

    // ── Tests liste des factures (/invoices) ───────────────────────

    /**
     * Un utilisateur non authentifié sur /invoices doit être redirigé
     * vers la page de login (comportement Spring Security par défaut).
     */
    @Test
    void list_unauthenticated_redirectsToLogin() throws Exception {
        mockMvc.perform(get("/invoices"))
                .andExpect(status().is3xxRedirection());
    }

    /**
     * Un admin authentifié doit obtenir HTTP 200 sur /invoices.
     * invoiceService.findAll() est appelé car l'admin voit tout.
     */
    @Test
    void list_asAdmin_returns200() throws Exception {
        when(invoiceService.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/invoices").with(user(buildAdmin())))
                .andExpect(status().isOk());
    }

    /**
     * Un client authentifié doit obtenir HTTP 200 sur /invoices.
     * invoiceService.findByCustomerId() est appelé (filtré sur son ID client).
     */
    @Test
    void list_asClient_returns200() throws Exception {
        when(invoiceService.findByCustomerId(1)).thenReturn(List.of());

        mockMvc.perform(get("/invoices").with(user(buildClient(1))))
                .andExpect(status().isOk());
    }

    // ── Tests détail d'une facture (/invoices/{id}) ────────────────

    /**
     * Un admin peut accéder au détail de n'importe quelle facture,
     * même celle d'un autre client.
     */
    @Test
    void detail_asAdmin_canAccessAnyInvoice() throws Exception {
        Invoice invoice = buildInvoice(5, 99);
        when(invoiceService.findById(5)).thenReturn(invoice);
        when(invoiceRowService.findByInvoiceId(5)).thenReturn(List.of());
        when(productService.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/invoices/5").with(user(buildAdmin())))
                .andExpect(status().isOk());
    }

    /**
     * Un client peut accéder au détail d'une facture qui lui appartient.
     * La vérification client.id == invoice.customer.id passe → 200.
     */
    @Test
    void detail_asClientOwner_canAccessOwnInvoice() throws Exception {
        Invoice invoice = buildInvoice(5, 1); // facture du client 1
        when(invoiceService.findById(5)).thenReturn(invoice);
        when(invoiceRowService.findByInvoiceId(5)).thenReturn(List.of());
        when(productService.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/invoices/5").with(user(buildClient(1))))
                .andExpect(status().isOk());
    }

    /**
     * IDOR – Protection contre l'accès non autorisé :
     * Le client 1 tente d'accéder à /invoices/5 qui appartient au client 2.
     * Le contrôleur doit rejeter la requête avec HTTP 403 Forbidden.
     */
    @Test
    void detail_asClientNonOwner_returns403() throws Exception {
        Invoice invoice = buildInvoice(5, 2); // appartient au client 2
        when(invoiceService.findById(5)).thenReturn(invoice);

        mockMvc.perform(get("/invoices/5").with(user(buildClient(1)))) // client 1 tente l'accès
                .andExpect(status().isForbidden());
    }

    // ── Tests contrôle d'accès aux routes admin ────────────────────

    /**
     * La route /admin/** est interdite aux clients (Spring Security → 403).
     */
    @Test
    void adminRoute_asClient_returns403() throws Exception {
        mockMvc.perform(get("/admin/invoices/new").with(user(buildClient(1))))
                .andExpect(status().isForbidden());
    }

    /**
     * Un admin peut accéder au formulaire de création de facture.
     */
    @Test
    void newInvoiceForm_asAdmin_returns200() throws Exception {
        when(customerService.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/admin/invoices/new").with(user(buildAdmin())))
                .andExpect(status().isOk());
    }

    /**
     * Après suppression d'une facture (POST), l'admin est redirigé vers /invoices.
     */
    @Test
    void delete_asAdmin_redirectsToList() throws Exception {
        mockMvc.perform(post("/admin/invoices/5/delete")
                        .with(csrf())
                        .with(user(buildAdmin())))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/invoices"));
    }
}
