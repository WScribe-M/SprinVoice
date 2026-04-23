package fr.manu.sprinvoice.controllers;

import fr.manu.sprinvoice.dto.QuoteFormDTO;
import fr.manu.sprinvoice.dto.RowFormDTO;
import fr.manu.sprinvoice.models.Invoice;
import fr.manu.sprinvoice.models.Quote;
import fr.manu.sprinvoice.models.QuoteRow;
import fr.manu.sprinvoice.models.QuoteStatus;
import fr.manu.sprinvoice.models.User;
import fr.manu.sprinvoice.services.CustomerService;
import fr.manu.sprinvoice.services.ProductService;
import fr.manu.sprinvoice.services.QuoteRowService;
import fr.manu.sprinvoice.services.QuoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
public class QuoteController {

    @Autowired private QuoteService quoteService;
    @Autowired private QuoteRowService quoteRowService;
    @Autowired private CustomerService customerService;
    @Autowired private ProductService productService;

    // ── Accès CLIENT + ADMIN ──────────────────────────────────────

    @GetMapping("/quotes")
    public String list(Model model, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        List<Quote> quotes;
        if (user.getRole().getName().equals("ROLE_ADMIN")) {
            quotes = quoteService.findAll();
        } else {
            quotes = quoteService.findByCustomerId(user.getCustomer().getId());
        }
        model.addAttribute("quotes", quotes);
        return "quotes/list";
    }

    @GetMapping("/quotes/{id}")
    public String detail(@PathVariable int id, Model model, Authentication authentication) {
        Quote quote = quoteService.findById(id);
        User user = (User) authentication.getPrincipal();
        if (!user.getRole().getName().equals("ROLE_ADMIN")) {
            if (user.getCustomer() == null || quote.getCustomer() == null
                    || user.getCustomer().getId() != quote.getCustomer().getId()) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN);
            }
        }
        model.addAttribute("quote", quote);
        model.addAttribute("rows", quoteRowService.findByQuoteId(id));
        model.addAttribute("products", productService.findAll());
        return "quotes/detail";
    }

    // ── ADMIN seulement (sous /admin/quotes/**) ───────────────────

    @GetMapping("/admin/quotes/new")
    public String createForm(Model model) {
        model.addAttribute("dto", new QuoteFormDTO());
        model.addAttribute("customers", customerService.findAll());
        model.addAttribute("statuses", QuoteStatus.values());
        model.addAttribute("products", productService.findAll());
        return "quotes/form";
    }

    @GetMapping("/admin/quotes/{id}/edit")
    public String editForm(@PathVariable int id, Model model) {
        Quote quote = quoteService.findById(id);
        QuoteFormDTO dto = new QuoteFormDTO();
        dto.setId(quote.getId());
        dto.setDesignation(quote.getDesignation());
        dto.setCustomerId(quote.getCustomer() != null ? quote.getCustomer().getId() : 0);
        dto.setExpiresAt(quote.getExpiresAt() != null ? quote.getExpiresAt().toLocalDate().toString() : "");
        dto.setStatus(quote.getStatus() != null ? quote.getStatus().name() : QuoteStatus.BROUILLON.name());

        List<RowFormDTO> rowDTOs = quoteRowService.findByQuoteId(id).stream()
                .map(r -> { RowFormDTO rd = new RowFormDTO(); rd.setProductId(r.getProduct().getId()); rd.setQuantity(r.getQuantity()); return rd; })
                .collect(Collectors.toList());
        dto.setRows(rowDTOs);

        model.addAttribute("dto", dto);
        model.addAttribute("customers", customerService.findAll());
        model.addAttribute("statuses", QuoteStatus.values());
        model.addAttribute("products", productService.findAll());
        return "quotes/form";
    }

    @PostMapping("/admin/quotes/save")
    public String save(@ModelAttribute QuoteFormDTO dto) {
        Quote quote = dto.getId() > 0
                ? quoteService.findById(dto.getId())
                : new Quote();

        if (dto.getId() == 0) {
            quote.setCreatedAt(LocalDateTime.now());
            quote.setStatus(QuoteStatus.BROUILLON);
        }
        quote.setDesignation(dto.getDesignation());
        quote.setCustomer(customerService.findById(dto.getCustomerId()));
        quote.setExpiresAt(toDateTime(dto.getExpiresAt()));
        if (dto.getStatus() != null && !dto.getStatus().isBlank()) {
            quote.setStatus(QuoteStatus.valueOf(dto.getStatus()));
        }
        Quote saved = quoteService.save(quote);
        quoteRowService.replaceRows(saved.getId(), dto.getRows());
        return "redirect:/quotes/" + saved.getId();
    }

    @PostMapping("/admin/quotes/{id}/rows/add")
    public String addRow(@PathVariable int id,
                         @RequestParam int productId,
                         @RequestParam int quantity) {
        quoteRowService.addRow(id, productId, quantity);
        return "redirect:/quotes/" + id;
    }

    @PostMapping("/admin/quotes/{quoteId}/rows/{rowId}/delete")
    public String deleteRow(@PathVariable int quoteId, @PathVariable int rowId) {
        quoteRowService.deleteById(rowId);
        return "redirect:/quotes/" + quoteId;
    }

    @PostMapping("/admin/quotes/{id}/delete")
    public String delete(@PathVariable int id) {
        quoteService.deleteById(id);
        return "redirect:/quotes";
    }

    @PostMapping("/admin/quotes/{id}/convert")
    public String convertToInvoice(@PathVariable int id) {
        Invoice invoice = quoteService.convertToInvoice(id);
        return "redirect:/invoices/" + invoice.getId();
    }

    // ── Utilitaire ────────────────────────────────────────────────

    private LocalDateTime toDateTime(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) return null;
        return LocalDate.parse(dateStr).atStartOfDay();
    }
}
