package fr.manu.sprinvoice.repositories;

import fr.manu.sprinvoice.models.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InvoiceRepository extends JpaRepository<Invoice, Integer> {
    List<Invoice> findByCustomerId(int customerId);
}
