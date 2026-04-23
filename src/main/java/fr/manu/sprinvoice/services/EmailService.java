package fr.manu.sprinvoice.services;

import fr.manu.sprinvoice.models.Invoice;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.from}")
    private String from;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendInvoice(Invoice invoice, byte[] pdf) throws MessagingException {
        String invoiceNum = invoice.getInvoiceNumber() != null ? invoice.getInvoiceNumber() : "N°" + invoice.getId();
        String to = invoice.getCustomer().getEmail();

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setFrom(from);
        helper.setTo(to);
        helper.setSubject("Votre facture " + invoiceNum);
        helper.setText(buildBody(invoice, invoiceNum));
        helper.addAttachment(invoiceNum + ".pdf", new ByteArrayResource(pdf), "application/pdf");

        mailSender.send(message);
    }

    private String buildBody(Invoice invoice, String invoiceNum) {
        String clientName = invoice.getCustomer() != null ? invoice.getCustomer().getName() : "";
        StringBuilder sb = new StringBuilder();
        sb.append("Bonjour").append(clientName.isBlank() ? "," : " " + clientName + ",").append("\n\n");
        sb.append("Veuillez trouver ci-joint votre facture ").append(invoiceNum);
        if (invoice.getDesignation() != null) sb.append(" – ").append(invoice.getDesignation());
        sb.append(".\n\n");
        sb.append(String.format("Montant total TTC : %.2f €%n", invoice.totalTtc()));
        if (invoice.getInvoicedAt() != null && invoice.getCustomer() != null) {
            String echeance = invoice.getInvoicedAt()
                    .plusDays(invoice.getCustomer().getDelay())
                    .format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            sb.append("Date d'échéance : ").append(echeance).append("\n");
        }
        sb.append("\nCordialement,");
        return sb.toString();
    }
}
