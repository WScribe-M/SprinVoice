package fr.manu.sprinvoice.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.manu.sprinvoice.models.User;
import fr.manu.sprinvoice.services.DashboardService;
import fr.manu.sprinvoice.services.InvoiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/dashboard")
public class DashboardController {

    @Autowired private DashboardService dashboardService;
    @Autowired private InvoiceService invoiceService;

    @GetMapping("")
    public String dashboard(Model model, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        ObjectMapper objectMapper = new ObjectMapper();
        
        // Données générales (admin seulement)
        if (user.getRole().getName().equals("ROLE_ADMIN")) {
            model.addAttribute("monthlyRevenue", dashboardService.getMonthlyRevenue());
            model.addAttribute("overdueCount", dashboardService.getOverdueCount());
            model.addAttribute("overdueAmount", dashboardService.getOverdueAmount());
            model.addAttribute("overdueInvoices", dashboardService.getOverdueInvoices());
            
            // Données pour les graphiques - sérialisées en JSON
            try {
                model.addAttribute("last12MonthsJson", objectMapper.writeValueAsString(dashboardService.getLast12MonthsRevenue()));
                model.addAttribute("invoicesByStatusJson", objectMapper.writeValueAsString(dashboardService.getInvoicesByStatus()));
                model.addAttribute("revenueByCustomerJson", objectMapper.writeValueAsString(dashboardService.getRevenueByCustomer()));
            } catch (Exception e) {
                model.addAttribute("last12MonthsJson", "{}");
                model.addAttribute("invoicesByStatusJson", "{}");
                model.addAttribute("revenueByCustomerJson", "{}");
            }
            
            // Garder les originaux pour compatibility
            model.addAttribute("last12Months", dashboardService.getLast12MonthsRevenue());
            model.addAttribute("invoicesByStatus", dashboardService.getInvoicesByStatus());
            model.addAttribute("revenueByCustomer", dashboardService.getRevenueByCustomer());
        } else {
            // Pour les clients, afficher seulement leurs factures
            var customerInvoices = invoiceService.findByCustomerId(user.getCustomer().getId());
            float totalRevenue = customerInvoices.stream()
                    .map(i -> i.total())
                    .reduce(0f, Float::sum);
            
            long paidCount = customerInvoices.stream()
                    .filter(i -> i.getPaidAt() != null)
                    .count();
            
            model.addAttribute("monthlyRevenue", totalRevenue);
            model.addAttribute("paidCount", paidCount);
            model.addAttribute("totalCount", customerInvoices.size());
            model.addAttribute("overdueInvoices", customerInvoices.stream()
                    .filter(i -> i.getPaidAt() == null && i.getInvoicedAt() != null && 
                            i.getInvoicedAt().toLocalDate().isBefore(java.time.LocalDate.now()))
                    .toList());
        }
        
        return "dashboard/index";
    }
}
