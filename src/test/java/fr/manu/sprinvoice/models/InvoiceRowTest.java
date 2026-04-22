package fr.manu.sprinvoice.models;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * TYPE : Test unitaire (JUnit 5 pur, aucun contexte Spring)
 *
 * Vérifie le calcul du montant d'une ligne de facturation (InvoiceRow.amount()).
 * Le montant = quantité × prix unitaire du produit.
 */
class InvoiceRowTest {

    /**
     * Si le produit est null, le montant doit être 0 (pas de NullPointerException).
     */
    @Test
    void amount_withNullProduct_returnsZero() {
        InvoiceRow row = new InvoiceRow();
        row.setQuantity(5);
        row.setProduct(null);

        assertThat(row.amount()).isEqualTo(0f);
    }

    /**
     * Si la quantité est null, le montant doit être 0.
     */
    @Test
    void amount_withNullQuantity_returnsZero() {
        Product product = new Product();
        product.setUnitPrice(10.00f);

        InvoiceRow row = new InvoiceRow();
        row.setQuantity(null);
        row.setProduct(product);

        assertThat(row.amount()).isEqualTo(0f);
    }

    /**
     * Calcul standard : quantité × prix unitaire.
     * 3 × 12,50 € = 37,50 €
     */
    @Test
    void amount_returnsQuantityTimesUnitPrice() {
        Product product = new Product();
        product.setUnitPrice(12.50f);

        InvoiceRow row = new InvoiceRow();
        row.setQuantity(3);
        row.setProduct(product);

        assertThat(row.amount()).isEqualTo(37.50f);
    }

    /**
     * Quantité de 1 → le montant est égal au prix unitaire.
     */
    @Test
    void amount_withQuantityOne_returnsUnitPrice() {
        Product product = new Product();
        product.setUnitPrice(99.99f);

        InvoiceRow row = new InvoiceRow();
        row.setQuantity(1);
        row.setProduct(product);

        assertThat(row.amount()).isEqualTo(99.99f);
    }
}
