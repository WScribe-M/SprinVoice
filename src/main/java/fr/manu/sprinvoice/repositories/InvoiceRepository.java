package fr.manu.sprinvoice.repositories;

import fr.manu.sprinvoice.models.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvoiceRepository extends JpaRepository<Invoice, Integer> {
}
