package fr.manu.sprinvoice.models;

import jakarta.persistence.*;

@Entity
@Table(name = "PRODUCT")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "product_seq")
    @SequenceGenerator(name = "product_seq", sequenceName = "PRODUCT_SEQ", allocationSize = 1)
    private int id;

    @Column(name = "DESIGNATION")
    private String designation;

    @Column(name = "UNIT_PRICE")
    private float unitPrice;

    @Column(name = "DESCRIPTION")
    private String description;

    @Column(name = "CATEGORY")
    private String category;

    @Column(name = "TVA_RATE")
    private Integer tvaRate; // pourcentage : 0, 10 ou 20

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDesignation() {
        return designation;
    }

    public void setDesignation(String designation) {
        this.designation = designation;
    }

    public float getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(float unitPrice) {
        this.unitPrice = unitPrice;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public int getTvaRate() {
        return tvaRate != null ? tvaRate : 0;
    }

    public void setTvaRate(Integer tvaRate) {
        this.tvaRate = tvaRate;
    }
}
