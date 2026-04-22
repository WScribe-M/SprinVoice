package fr.manu.sprinvoice.repositories;

import fr.manu.sprinvoice.models.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface InvoiceRepository extends JpaRepository<Invoice, Integer> {
    List<Invoice> findByCustomerId(int customerId);
    
    @Query("SELECT i FROM Invoice i WHERE YEAR(i.invoicedAt) = YEAR(CURRENT_DATE) AND MONTH(i.invoicedAt) = MONTH(CURRENT_DATE)")
    List<Invoice> findByCurrentMonth();
    
    @Query("SELECT i FROM Invoice i WHERE i.paidAt IS NULL AND i.invoicedAt IS NOT NULL AND i.invoicedAt < CURRENT_DATE")
    List<Invoice> findOverdueInvoices();
    
    @Query("SELECT i FROM Invoice i WHERE i.invoicedAt >= :startDate AND i.invoicedAt < :endDate")
    List<Invoice> findByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
}
