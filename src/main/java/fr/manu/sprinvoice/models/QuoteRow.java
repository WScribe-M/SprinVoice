package fr.manu.sprinvoice.models;

import jakarta.persistence.*;

@Entity
@Table(name = "QUOTE_ROW")
public class QuoteRow {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "quote_row_seq")
    @SequenceGenerator(name = "quote_row_seq", sequenceName = "QUOTE_ROW_SEQ", allocationSize = 1)
    private Integer id;

    @Column(name = "QUANTITY")
    private Integer quantity;

    @ManyToOne
    @JoinColumn(name = "QUOTE_ID")
    private Quote quote;

    @ManyToOne
    @JoinColumn(name = "PRODUCT_ID")
    private Product product;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public Quote getQuote() { return quote; }
    public void setQuote(Quote quote) { this.quote = quote; }

    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }

    public float amount() {
        if (product == null || quantity == null) return 0f;
        return quantity * product.getUnitPrice();
    }
}
