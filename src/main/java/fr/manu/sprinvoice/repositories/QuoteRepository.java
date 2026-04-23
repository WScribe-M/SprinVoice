package fr.manu.sprinvoice.repositories;

import fr.manu.sprinvoice.models.Quote;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface QuoteRepository extends JpaRepository<Quote, Integer> {
    List<Quote> findByCustomerId(int customerId);
}
