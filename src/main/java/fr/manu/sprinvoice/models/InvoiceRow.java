package fr.manu.sprinvoice.models;

import jakarta.persistence.*;

@Entity
@Table(name = "INVOICE_ROW")
public class InvoiceRow {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "invoice_row_seq")
    @SequenceGenerator(name = "invoice_row_seq", sequenceName = "INVOICE_ROW_SEQ", allocationSize = 1)
    private Integer id;

    @Column(name = "QUANTITY")
    private Integer quantity;

    @ManyToOne
    @JoinColumn(name = "INVOICE_ID")
    private Invoice invoice;

    @ManyToOne
    @JoinColumn(name = "PRODUCT_ID")
    private Product product;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Invoice getInvoice() {
        return invoice;
    }

    public void setInvoice(Invoice invoice) {
        this.invoice = invoice;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public float amount() {
        if (product == null || quantity == null) return 0f;
        return quantity * product.getUnitPrice();
    }
}
