package fr.manu.sprinvoice.repositories;

import fr.manu.sprinvoice.models.InvoiceRow;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvoiceRowRepository extends JpaRepository<InvoiceRow, Integer> {

}
