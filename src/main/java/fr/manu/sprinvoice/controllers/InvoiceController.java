package fr.manu.sprinvoice.controllers;

import fr.manu.sprinvoice.models.Invoice;
import fr.manu.sprinvoice.services.InvoiceService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/invoices")
public class InvoiceController {

    private final InvoiceService invoiceService;

    public InvoiceController(InvoiceService invoiceService) {
        this.invoiceService = invoiceService;
    }

    // Liste
    @GetMapping
    public String list(Model model) {
        model.addAttribute("invoices", invoiceService.findAll());
        return "invoices/list";
    }

    // Formulaire création
    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("invoice", new Invoice());
        return "invoices/form";
    }

    // Formulaire édition
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable int id, Model model) {
        model.addAttribute("invoice", invoiceService.findById(id));
        return "invoices/form";
    }

    // Sauvegarde (create + update)
    @PostMapping("/save")
    public String save(@ModelAttribute Invoice invoice) {
        invoiceService.save(invoice);
        return "redirect:/invoices";
    }

    // Suppression
    @GetMapping("/{id}/delete")
    public String delete(@PathVariable int id) {
        invoiceService.deleteById(id);
        return "redirect:/invoices";
    }

    @GetMapping("/test")
    @ResponseBody
    public String test() {
        Invoice invoice = invoiceService.findById(1);
        return "Total : " + invoice.total();
    }
}