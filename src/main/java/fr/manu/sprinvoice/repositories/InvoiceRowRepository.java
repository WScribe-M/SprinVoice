package fr.manu.sprinvoice.repositories;

import fr.manu.sprinvoice.models.InvoiceRow;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface InvoiceRowRepository extends JpaRepository<InvoiceRow, Integer> {
    List<InvoiceRow> findByInvoiceId(int invoiceId);
}
