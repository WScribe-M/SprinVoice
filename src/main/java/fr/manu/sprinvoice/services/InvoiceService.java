package fr.manu.sprinvoice.services;

import fr.manu.sprinvoice.models.Invoice;
import fr.manu.sprinvoice.repositories.InvoiceRepository;
import org.springframework.stereotype.Service;
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

    public Invoice findById(int id) {
        return invoiceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Facture introuvable : " + id));
    }

    public Invoice save(Invoice invoice) {
        return invoiceRepository.save(invoice);
    }

    public void deleteById(int id) {
        invoiceRepository.deleteById(id);
    }
}

