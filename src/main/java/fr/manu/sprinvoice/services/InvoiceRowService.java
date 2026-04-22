package fr.manu.sprinvoice.services;

import fr.manu.sprinvoice.models.InvoiceRow;
import fr.manu.sprinvoice.repositories.InvoiceRepository;
import fr.manu.sprinvoice.repositories.InvoiceRowRepository;
import fr.manu.sprinvoice.repositories.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InvoiceRowService {

    @Autowired private InvoiceRowRepository invoiceRowRepository;
    @Autowired private InvoiceRepository invoiceRepository;
    @Autowired private ProductRepository productRepository;

    public List<InvoiceRow> findByInvoiceId(int invoiceId) {
        return invoiceRowRepository.findByInvoiceId(invoiceId);
    }

    public void addRow(int invoiceId, int productId, int quantity) {
        InvoiceRow row = new InvoiceRow();
        row.setInvoice(invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new RuntimeException("Facture introuvable : " + invoiceId)));
        row.setProduct(productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Produit introuvable : " + productId)));
        row.setQuantity(quantity);
        invoiceRowRepository.save(row);
    }

    public void deleteById(int id) {
        invoiceRowRepository.deleteById(id);
    }
}
