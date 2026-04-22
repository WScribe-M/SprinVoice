package fr.manu.sprinvoice.controllers;

import fr.manu.sprinvoice.dto.InvoiceFormDTO;
import fr.manu.sprinvoice.models.Invoice;
import fr.manu.sprinvoice.models.User;
import fr.manu.sprinvoice.services.CustomerService;
import fr.manu.sprinvoice.services.InvoiceRowService;
import fr.manu.sprinvoice.services.InvoiceService;
import fr.manu.sprinvoice.services.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Controller
public class InvoiceController {

    @Autowired private InvoiceService invoiceService;
    @Autowired private CustomerService customerService;
    @Autowired private ProductService productService;
    @Autowired private InvoiceRowService invoiceRowService;

    // ── Accès CLIENT + ADMIN ──────────────────────────────────────

    @GetMapping("/invoices")
    public String list(Model model, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        List<Invoice> invoices;
        if (user.getRole().getName().equals("ROLE_ADMIN")) {
            invoices = invoiceService.findAll();
        } else {
            invoices = invoiceService.findByCustomerId(user.getCustomer().getId());
        }
        model.addAttribute("invoices", invoices);
        return "invoices/list";
    }

    @GetMapping("/invoices/{id}")
    public String detail(@PathVariable int id, Model model) {
        model.addAttribute("invoice", invoiceService.findById(id));
        model.addAttribute("rows", invoiceRowService.findByInvoiceId(id));
        model.addAttribute("products", productService.findAll());
        return "invoices/detail";
    }

    // ── ADMIN seulement (sous /admin/invoices/**) ─────────────────

    @GetMapping("/admin/invoices/new")
    public String createForm(Model model) {
        model.addAttribute("dto", new InvoiceFormDTO());
        model.addAttribute("customers", customerService.findAll());
        return "invoices/form";
    }

    @GetMapping("/admin/invoices/{id}/edit")
    public String editForm(@PathVariable int id, Model model) {
        Invoice invoice = invoiceService.findById(id);
        InvoiceFormDTO dto = new InvoiceFormDTO();
        dto.setId(invoice.getId());
        dto.setDesignation(invoice.getDesignation());
        dto.setCustomerId(invoice.getCustomer() != null ? invoice.getCustomer().getId() : 0);
        dto.setInvoicedAt(invoice.getInvoicedAt() != null ? invoice.getInvoicedAt().toLocalDate().toString() : "");
        dto.setPaidAt(invoice.getPaidAt() != null ? invoice.getPaidAt().toLocalDate().toString() : "");
        model.addAttribute("dto", dto);
        model.addAttribute("customers", customerService.findAll());
        return "invoices/form";
    }

    @PostMapping("/admin/invoices/save")
    public String save(@ModelAttribute InvoiceFormDTO dto) {
        Invoice invoice = dto.getId() > 0
                ? invoiceService.findById(dto.getId())
                : new Invoice();

        if (dto.getId() == 0) {
            invoice.setCreatedAt(LocalDateTime.now());
        }
        invoice.setDesignation(dto.getDesignation());
        invoice.setCustomer(customerService.findById(dto.getCustomerId()));
        invoice.setInvoicedAt(toDateTime(dto.getInvoicedAt()));
        invoice.setPaidAt(toDateTime(dto.getPaidAt()));
        Invoice saved = invoiceService.save(invoice);
        return "redirect:/invoices/" + saved.getId();
    }

    @PostMapping("/admin/invoices/{id}/rows/add")
    public String addRow(@PathVariable int id,
                         @RequestParam int productId,
                         @RequestParam int quantity) {
        invoiceRowService.addRow(id, productId, quantity);
        return "redirect:/invoices/" + id;
    }

    @GetMapping("/admin/invoices/{invoiceId}/rows/{rowId}/delete")
    public String deleteRow(@PathVariable int invoiceId, @PathVariable int rowId) {
        invoiceRowService.deleteById(rowId);
        return "redirect:/invoices/" + invoiceId;
    }

    @GetMapping("/admin/invoices/{id}/delete")
    public String delete(@PathVariable int id) {
        invoiceService.deleteById(id);
        return "redirect:/invoices";
    }

    // ── Utilitaire ────────────────────────────────────────────────

    private LocalDateTime toDateTime(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) return null;
        return LocalDate.parse(dateStr).atStartOfDay();
    }
}
