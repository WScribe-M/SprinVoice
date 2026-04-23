package fr.manu.sprinvoice.repositories;

import fr.manu.sprinvoice.models.QuoteRow;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface QuoteRowRepository extends JpaRepository<QuoteRow, Integer> {
    List<QuoteRow> findByQuoteId(int quoteId);
}
