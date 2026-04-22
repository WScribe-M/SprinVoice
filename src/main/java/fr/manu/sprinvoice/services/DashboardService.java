package fr.manu.sprinvoice.services;

import fr.manu.sprinvoice.models.Invoice;
import fr.manu.sprinvoice.repositories.InvoiceRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    private final InvoiceRepository invoiceRepository;

    public DashboardService(InvoiceRepository invoiceRepository) {
        this.invoiceRepository = invoiceRepository;
    }

    public float getMonthlyRevenue() {
        List<Invoice> invoices = invoiceRepository.findByCurrentMonth();
        return invoices.stream()
                .map(Invoice::total)
                .reduce(0f, Float::sum);
    }

    public List<Invoice> getOverdueInvoices() {
        return invoiceRepository.findOverdueInvoices();
    }

    public float getOverdueAmount() {
        return getOverdueInvoices().stream()
                .map(Invoice::total)
                .reduce(0f, Float::sum);
    }

    public int getOverdueCount() {
        return getOverdueInvoices().size();
    }

    public Map<String, Float> getLast12MonthsRevenue() {
        Map<String, Float> data = new LinkedHashMap<>();
        
        for (int i = 11; i >= 0; i--) {
            YearMonth yearMonth = YearMonth.now().minusMonths(i);
            LocalDateTime startDate = yearMonth.atDay(1).atStartOfDay();
            LocalDateTime endDate = yearMonth.atEndOfMonth().atTime(23, 59, 59);
            
            List<Invoice> invoices = invoiceRepository.findByDateRange(startDate, endDate);
            float revenue = invoices.stream()
                    .map(Invoice::total)
                    .reduce(0f, Float::sum);
            
            data.put(yearMonth.toString(), revenue);
        }
        
        return data;
    }

    public Map<String, Integer> getInvoicesByStatus() {
        List<Invoice> allInvoices = invoiceRepository.findAll();
        
        int paid = (int) allInvoices.stream()
                .filter(i -> i.getPaidAt() != null)
                .count();
        
        int unpaid = (int) allInvoices.stream()
                .filter(i -> i.getPaidAt() == null && i.getInvoicedAt() != null)
                .count();
        
        int draft = (int) allInvoices.stream()
                .filter(i -> i.getInvoicedAt() == null)
                .count();
        
        Map<String, Integer> status = new LinkedHashMap<>();
        status.put("Payées", paid);
        status.put("En attente", unpaid);
        status.put("Brouillons", draft);
        
        return status;
    }

    public Map<String, Float> getRevenueByCustomer() {
        List<Invoice> allInvoices = invoiceRepository.findAll();
        
        return allInvoices.stream()
                .filter(i -> i.getCustomer() != null)
                .collect(Collectors.groupingBy(
                        i -> i.getCustomer().getName() != null ? i.getCustomer().getName() : "Sans client",
                        LinkedHashMap::new,
                        Collectors.reducing(0f, Invoice::total, Float::sum)
                ))
                .entrySet().stream()
                .sorted((a, b) -> Float.compare(b.getValue(), a.getValue()))
                .limit(10)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));
    }
}
