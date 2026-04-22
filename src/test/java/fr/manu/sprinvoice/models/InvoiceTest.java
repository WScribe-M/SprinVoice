package fr.manu.sprinvoice.models;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * TYPE : Test unitaire (JUnit 5 pur, aucun contexte Spring)
 *
 * Vérifie le calcul du total d'une facture (Invoice.total()).
 * La méthode somme les montants de chaque ligne de facturation.
 */
class InvoiceTest {

    // --- Utilitaire ---

    private InvoiceRow buildRow(float unitPrice, int quantity) {
        Product product = new Product();
        product.setUnitPrice(unitPrice);

        InvoiceRow row = new InvoiceRow();
        row.setProduct(product);
        row.setQuantity(quantity);
        return row;
    }

    // --- Tests ---

    /**
     * Une facture sans lignes (rows = null) doit retourner 0.
     */
    @Test
    void total_withNullRows_returnsZero() {
        Invoice invoice = new Invoice();
        // rows non initialisé → null

        assertThat(invoice.total()).isEqualTo(0f);
    }

    /**
     * Une facture avec une liste vide de lignes doit retourner 0.
     */
    @Test
    void total_withEmptyRows_returnsZero() {
        Invoice invoice = new Invoice();
        invoice.setRows(List.of());

        assertThat(invoice.total()).isEqualTo(0f);
    }

    /**
     * Une facture avec plusieurs lignes doit retourner la somme correcte.
     * Exemple : 2 × 10,00 € + 3 × 5,50 € = 36,50 €
     */
    @Test
    void total_withMultipleRows_returnsSumOfAmounts() {
        Invoice invoice = new Invoice();
        invoice.setRows(List.of(
                buildRow(10.00f, 2),  // 20,00 €
                buildRow(5.50f,  3)   // 16,50 €
        ));

        assertThat(invoice.total()).isEqualTo(36.50f);
    }

    /**
     * Une seule ligne → le total est égal au montant de cette ligne.
     */
    @Test
    void total_withOneRow_returnsRowAmount() {
        Invoice invoice = new Invoice();
        invoice.setRows(List.of(buildRow(25.00f, 4)));

        assertThat(invoice.total()).isEqualTo(100.00f);
    }
}
