package fr.manu.sprinvoice.controllers;

import fr.manu.sprinvoice.models.Invoice;
import fr.manu.sprinvoice.models.User;
import fr.manu.sprinvoice.services.InvoiceService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/invoices")
public class InvoiceController {

    private final InvoiceService invoiceService;

    public InvoiceController(InvoiceService invoiceService) {
        this.invoiceService = invoiceService;
    }

    @GetMapping
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

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("invoice", new Invoice());
        return "invoices/form";
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable int id, Model model) {
        model.addAttribute("invoice", invoiceService.findById(id));
        return "invoices/form";
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PostMapping("/save")
    public String save(@ModelAttribute Invoice invoice) {
        invoiceService.save(invoice);
        return "redirect:/invoices";
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping("/{id}/delete")
    public String delete(@PathVariable int id) {
        invoiceService.deleteById(id);
        return "redirect:/invoices";
    }
}