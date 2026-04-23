package fr.manu.sprinvoice.models;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "INVOICE")
public class Invoice {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "invoice_seq")
    @SequenceGenerator(name = "invoice_seq", sequenceName = "INVOICE_SEQ", allocationSize = 1)
    private int id;

    @Column(name = "DESIGNATION")
    private String designation;

    @Column(name = "CREATED_AT")
    private LocalDateTime createdAt;

    @Column(name = "INVOICED_AT")
    private LocalDateTime invoicedAt;

    @Column(name = "PAID_AT")
    private LocalDateTime paidAt;

    @ManyToOne()
    @JoinColumn(name ="CUSTOMER_ID")
    private Customer customer;

    public LocalDateTime getPaidAt() {
        return paidAt;
    }

    public void setPaidAt(LocalDateTime paidAt) {
        this.paidAt = paidAt;
    }

    public LocalDateTime getInvoicedAt() {
        return invoicedAt;
    }

    public void setInvoicedAt(LocalDateTime invoicedAt) {
        this.invoicedAt = invoicedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getDesignation() {
        return designation;
    }

    public void setDesignation(String designation) {
        this.designation = designation;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InvoiceRow> rows;

    public List<InvoiceRow> getRows() { return rows; }
    public void setRows(List<InvoiceRow> rows) { this.rows = rows; }

    public float total() {
        if (rows == null) return 0f;
        return rows.stream()
                .map(InvoiceRow::amount)
                .reduce(0f, Float::sum);
    }
}
