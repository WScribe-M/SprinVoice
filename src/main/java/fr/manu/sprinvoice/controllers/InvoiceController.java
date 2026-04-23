package fr.manu.sprinvoice.controllers;

import fr.manu.sprinvoice.dto.InvoiceFormDTO;
import fr.manu.sprinvoice.dto.RowFormDTO;
import fr.manu.sprinvoice.models.Invoice;
import fr.manu.sprinvoice.models.InvoiceRow;
import fr.manu.sprinvoice.models.User;
import fr.manu.sprinvoice.services.CustomerService;
import fr.manu.sprinvoice.services.InvoicePdfService;
import fr.manu.sprinvoice.services.InvoiceRowService;
import fr.manu.sprinvoice.services.InvoiceService;
import fr.manu.sprinvoice.services.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class InvoiceController {

    @Autowired private InvoiceService invoiceService;
    @Autowired private CustomerService customerService;
    @Autowired private ProductService productService;
    @Autowired private InvoiceRowService invoiceRowService;
    @Autowired private InvoicePdfService invoicePdfService;

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
    public String detail(@PathVariable int id, Model model, Authentication authentication) {
        Invoice invoice = invoiceService.findById(id);
        User user = (User) authentication.getPrincipal();
        if (!user.getRole().getName().equals("ROLE_ADMIN")) {
            if (user.getCustomer() == null || invoice.getCustomer() == null
                    || user.getCustomer().getId() != invoice.getCustomer().getId()) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN);
            }
        }
        model.addAttribute("invoice", invoice);
        model.addAttribute("rows", invoiceRowService.findByInvoiceId(id));
        model.addAttribute("products", productService.findAll());
        return "invoices/detail";
    }

    @GetMapping("/invoices/{id}/pdf")
    public ResponseEntity<byte[]> downloadPdf(@PathVariable int id, Authentication authentication) {
        Invoice invoice = invoiceService.findById(id);
        User user = (User) authentication.getPrincipal();
        if (!user.getRole().getName().equals("ROLE_ADMIN")) {
            if (user.getCustomer() == null || invoice.getCustomer() == null
                    || user.getCustomer().getId() != invoice.getCustomer().getId()) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN);
            }
        }
        List<InvoiceRow> rows = invoiceRowService.findByInvoiceId(id);
        byte[] pdf = invoicePdfService.generate(invoice, rows);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "facture-" + id + ".pdf");
        return new ResponseEntity<>(pdf, headers, HttpStatus.OK);
    }

    // ── ADMIN seulement (sous /admin/invoices/**) ─────────────────

    @GetMapping("/admin/invoices/new")
    public String createForm(Model model) {
        model.addAttribute("dto", new InvoiceFormDTO());
        model.addAttribute("customers", customerService.findAll());
        model.addAttribute("products", productService.findAll());
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

        List<RowFormDTO> rowDTOs = invoiceRowService.findByInvoiceId(id).stream()
                .map(r -> { RowFormDTO rd = new RowFormDTO(); rd.setProductId(r.getProduct().getId()); rd.setQuantity(r.getQuantity()); return rd; })
                .collect(Collectors.toList());
        dto.setRows(rowDTOs);

        model.addAttribute("dto", dto);
        model.addAttribute("customers", customerService.findAll());
        model.addAttribute("products", productService.findAll());
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
        invoiceRowService.replaceRows(saved.getId(), dto.getRows());
        return "redirect:/invoices/" + saved.getId();
    }

    @PostMapping("/admin/invoices/{id}/rows/add")
    public String addRow(@PathVariable int id,
                         @RequestParam int productId,
                         @RequestParam int quantity) {
        invoiceRowService.addRow(id, productId, quantity);
        return "redirect:/invoices/" + id;
    }

    @PostMapping("/admin/invoices/{invoiceId}/rows/{rowId}/delete")
    public String deleteRow(@PathVariable int invoiceId, @PathVariable int rowId) {
        invoiceRowService.deleteById(rowId);
        return "redirect:/invoices/" + invoiceId;
    }

    @PostMapping("/admin/invoices/{id}/delete")
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
