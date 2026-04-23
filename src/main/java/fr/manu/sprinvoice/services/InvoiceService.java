package fr.manu.sprinvoice.services;

import fr.manu.sprinvoice.models.Invoice;
import fr.manu.sprinvoice.repositories.InvoiceRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;

    public InvoiceService(InvoiceRepository invoiceRepository) {
        this.invoiceRepository = invoiceRepository;
    }

    public List<Invoice> findAll() {
        return invoiceRepository.findAll();
    }

    public List<Invoice> findByCustomerId(int customerId) {
        return invoiceRepository.findByCustomerId(customerId);
    }

    public Invoice findById(int id) {
        return invoiceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Facture introuvable : " + id));
    }

    public Invoice save(Invoice invoice) {
        if (invoice.getInvoiceNumber() == null) {
            invoice.setInvoiceNumber(generateInvoiceNumber(LocalDateTime.now().getYear()));
        }
        return invoiceRepository.save(invoice);
    }

    public void deleteById(int id) {
        invoiceRepository.deleteById(id);
    }

    private String generateInvoiceNumber(int year) {
        String prefix = "FAC-" + year + "-%";
        Integer max = invoiceRepository.findMaxCounterByYearPrefix(prefix);
        int next = (max != null ? max : 0) + 1;
        return String.format("FAC-%d-%03d", year, next);
    }
}
