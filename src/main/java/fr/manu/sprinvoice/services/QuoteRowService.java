package fr.manu.sprinvoice.services;

import fr.manu.sprinvoice.dto.RowFormDTO;
import fr.manu.sprinvoice.models.Quote;
import fr.manu.sprinvoice.models.QuoteRow;
import fr.manu.sprinvoice.repositories.ProductRepository;
import fr.manu.sprinvoice.repositories.QuoteRepository;
import fr.manu.sprinvoice.repositories.QuoteRowRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class QuoteRowService {

    @Autowired private QuoteRowRepository quoteRowRepository;
    @Autowired private QuoteRepository quoteRepository;
    @Autowired private ProductRepository productRepository;

    public List<QuoteRow> findByQuoteId(int quoteId) {
        return quoteRowRepository.findByQuoteId(quoteId);
    }

    public void addRow(int quoteId, int productId, int quantity) {
        QuoteRow row = new QuoteRow();
        row.setQuote(quoteRepository.findById(quoteId)
                .orElseThrow(() -> new RuntimeException("Devis introuvable : " + quoteId)));
        row.setProduct(productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Produit introuvable : " + productId)));
        row.setQuantity(quantity);
        quoteRowRepository.save(row);
    }

    public void replaceRows(int quoteId, List<RowFormDTO> rowDTOs) {
        quoteRowRepository.deleteAll(quoteRowRepository.findByQuoteId(quoteId));
        if (rowDTOs == null) return;
        Quote quote = quoteRepository.findById(quoteId)
                .orElseThrow(() -> new RuntimeException("Devis introuvable : " + quoteId));
        for (RowFormDTO dto : rowDTOs) {
            if (dto.getProductId() <= 0 || dto.getQuantity() <= 0) continue;
            QuoteRow row = new QuoteRow();
            row.setQuote(quote);
            row.setProduct(productRepository.findById(dto.getProductId())
                    .orElseThrow(() -> new RuntimeException("Produit introuvable : " + dto.getProductId())));
            row.setQuantity(dto.getQuantity());
            quoteRowRepository.save(row);
        }
    }

    public void deleteById(int id) {
        quoteRowRepository.deleteById(id);
    }
}
