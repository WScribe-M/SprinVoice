package fr.manu.sprinvoice.services;

import fr.manu.sprinvoice.models.*;
import fr.manu.sprinvoice.repositories.QuoteRepository;
import fr.manu.sprinvoice.repositories.InvoiceRowRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class QuoteService {

    private final QuoteRepository quoteRepository;
    private final InvoiceService invoiceService;
    private final InvoiceRowRepository invoiceRowRepository;

    public QuoteService(QuoteRepository quoteRepository,
                        InvoiceService invoiceService,
                        InvoiceRowRepository invoiceRowRepository) {
        this.quoteRepository = quoteRepository;
        this.invoiceService = invoiceService;
        this.invoiceRowRepository = invoiceRowRepository;
    }

    public List<Quote> findAll() {
        return quoteRepository.findAll();
    }

    public List<Quote> findByCustomerId(int customerId) {
        return quoteRepository.findByCustomerId(customerId);
    }

    public Quote findById(int id) {
        return quoteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Devis introuvable : " + id));
    }

    public Quote save(Quote quote) {
        return quoteRepository.save(quote);
    }

    public void deleteById(int id) {
        quoteRepository.deleteById(id);
    }

    @Transactional
    public Invoice convertToInvoice(int quoteId) {
        Quote quote = findById(quoteId);

        Invoice invoice = new Invoice();
        invoice.setDesignation(quote.getDesignation());
        invoice.setCustomer(quote.getCustomer());
        invoice.setCreatedAt(LocalDateTime.now());
        Invoice saved = invoiceService.save(invoice);

        if (quote.getRows() != null) {
            for (QuoteRow qr : quote.getRows()) {
                InvoiceRow ir = new InvoiceRow();
                ir.setInvoice(saved);
                ir.setProduct(qr.getProduct());
                ir.setQuantity(qr.getQuantity());
                invoiceRowRepository.save(ir);
            }
        }

        quote.setStatus(QuoteStatus.CONVERTI);
        quoteRepository.save(quote);

        return saved;
    }
}
