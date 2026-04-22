package fr.manu.sprinvoice.services;

import fr.manu.sprinvoice.models.Invoice;
import fr.manu.sprinvoice.repositories.InvoiceRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

/**
 * TYPE : Test unitaire de service (Mockito, aucun contexte Spring)
 *
 * Vérifie la logique d'InvoiceService en isolant complètement
 * le repository avec un mock. Aucune base de données n'est utilisée.
 */
@ExtendWith(MockitoExtension.class)
class InvoiceServiceTest {

    @Mock
    private InvoiceRepository invoiceRepository;

    @InjectMocks
    private InvoiceService invoiceService;

    /**
     * findAll() doit déléguer au repository et retourner toutes les factures.
     */
    @Test
    void findAll_returnsAllInvoices() {
        Invoice f1 = new Invoice(); f1.setDesignation("Facture A");
        Invoice f2 = new Invoice(); f2.setDesignation("Facture B");
        when(invoiceRepository.findAll()).thenReturn(List.of(f1, f2));

        List<Invoice> result = invoiceService.findAll();

        assertThat(result).hasSize(2);
        assertThat(result).extracting(Invoice::getDesignation)
                .containsExactly("Facture A", "Facture B");
    }

    /**
     * findByCustomerId() doit retourner uniquement les factures du client donné.
     */
    @Test
    void findByCustomerId_returnsMatchingInvoices() {
        Invoice f = new Invoice(); f.setDesignation("Facture client 7");
        when(invoiceRepository.findByCustomerId(7)).thenReturn(List.of(f));

        List<Invoice> result = invoiceService.findByCustomerId(7);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getDesignation()).isEqualTo("Facture client 7");
    }

    /**
     * findById() avec un ID existant doit retourner la facture correspondante.
     */
    @Test
    void findById_existingId_returnsInvoice() {
        Invoice invoice = new Invoice();
        invoice.setId(42);
        invoice.setDesignation("Facture existante");
        when(invoiceRepository.findById(42)).thenReturn(Optional.of(invoice));

        Invoice result = invoiceService.findById(42);

        assertThat(result.getId()).isEqualTo(42);
        assertThat(result.getDesignation()).isEqualTo("Facture existante");
    }

    /**
     * findById() avec un ID inconnu doit lever une RuntimeException
     * avec un message descriptif.
     */
    @Test
    void findById_unknownId_throwsRuntimeException() {
        when(invoiceRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> invoiceService.findById(99))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("99");
    }

    /**
     * save() doit déléguer au repository et retourner la facture persistée.
     */
    @Test
    void save_delegatesToRepository() {
        Invoice invoice = new Invoice();
        invoice.setDesignation("Nouvelle facture");
        when(invoiceRepository.save(invoice)).thenReturn(invoice);

        Invoice result = invoiceService.save(invoice);

        assertThat(result).isSameAs(invoice);
        verify(invoiceRepository).save(invoice);
    }

    /**
     * deleteById() doit appeler deleteById() sur le repository une seule fois.
     */
    @Test
    void deleteById_delegatesToRepository() {
        invoiceService.deleteById(10);

        verify(invoiceRepository, times(1)).deleteById(10);
    }
}
