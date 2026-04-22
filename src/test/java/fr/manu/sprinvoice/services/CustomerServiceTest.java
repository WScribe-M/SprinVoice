package fr.manu.sprinvoice.services;

import fr.manu.sprinvoice.dto.CreateCustomerDTO;
import fr.manu.sprinvoice.models.Customer;
import fr.manu.sprinvoice.models.Role;
import fr.manu.sprinvoice.models.User;
import fr.manu.sprinvoice.repositories.CustomerRepository;
import fr.manu.sprinvoice.repositories.RoleRepository;
import fr.manu.sprinvoice.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * TYPE : Test unitaire de service (Mockito, aucun contexte Spring)
 *
 * Vérifie la logique de CustomerService, en particulier la création
 * atomique d'un client + compte utilisateur (createCustomerWithAccount).
 */
@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock private CustomerRepository customerRepository;
    @Mock private UserRepository userRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks
    private CustomerService customerService;

    /**
     * findAll() doit retourner la liste complète des clients du repository.
     */
    @Test
    void findAll_returnsAllCustomers() {
        Customer c1 = new Customer(); c1.setName("Dupont");
        Customer c2 = new Customer(); c2.setName("Martin");
        when(customerRepository.findAll()).thenReturn(List.of(c1, c2));

        List<Customer> result = customerService.findAll();

        assertThat(result).hasSize(2);
        assertThat(result).extracting(Customer::getName).containsExactly("Dupont", "Martin");
    }

    /**
     * findById() avec un ID valide doit retourner le client correspondant.
     */
    @Test
    void findById_existingId_returnsCustomer() {
        Customer customer = new Customer();
        customer.setId(5);
        customer.setName("Dupont");
        when(customerRepository.findById(5)).thenReturn(Optional.of(customer));

        Customer result = customerService.findById(5);

        assertThat(result.getName()).isEqualTo("Dupont");
    }

    /**
     * findById() avec un ID inconnu doit lever une RuntimeException.
     */
    @Test
    void findById_unknownId_throwsRuntimeException() {
        when(customerRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> customerService.findById(99))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("99");
    }

    /**
     * createCustomerWithAccount() doit :
     * 1. Sauvegarder le client avec les données du DTO
     * 2. Sauvegarder l'utilisateur avec le rôle ROLE_CLIENT
     * 3. Ne PAS stocker le mot de passe en clair (il doit être encodé)
     */
    @Test
    void createCustomerWithAccount_savesCustomerAndUser() {
        Role clientRole = new Role();
        clientRole.setName("ROLE_CLIENT");
        when(roleRepository.findByName("ROLE_CLIENT")).thenReturn(Optional.of(clientRole));
        when(customerRepository.save(any(Customer.class))).thenAnswer(i -> i.getArgument(0));
        when(passwordEncoder.encode("Admin123!")).thenReturn("{bcrypt}hashed");

        CreateCustomerDTO dto = new CreateCustomerDTO();
        dto.setName("Dupont");
        dto.setCorporateName("Dupont SARL");
        dto.setCity("Paris");
        dto.setZipcode("75001");
        dto.setAddress("1 rue de la Paix");
        dto.setDelay(30);
        dto.setUsername("dupont");
        dto.setPassword("Admin123!");

        customerService.createCustomerWithAccount(dto);

        // Vérifie que le client a bien été sauvegardé
        ArgumentCaptor<Customer> customerCaptor = ArgumentCaptor.forClass(Customer.class);
        verify(customerRepository).save(customerCaptor.capture());
        assertThat(customerCaptor.getValue().getName()).isEqualTo("Dupont");
        assertThat(customerCaptor.getValue().getCity()).isEqualTo("Paris");

        // Vérifie que l'utilisateur a bien été sauvegardé
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();
        assertThat(savedUser.getUsername()).isEqualTo("dupont");
        assertThat(savedUser.getRole().getName()).isEqualTo("ROLE_CLIENT");
        // Le mot de passe ne doit PAS être stocké en clair
        assertThat(savedUser.getPassword()).isNotEqualTo("Admin123!");
        assertThat(savedUser.getPassword()).isEqualTo("{bcrypt}hashed");
    }

    /**
     * createCustomerWithAccount() doit lever une exception si le rôle ROLE_CLIENT
     * n'existe pas en base (données manquantes).
     */
    @Test
    void createCustomerWithAccount_missingRole_throwsException() {
        when(customerRepository.save(any(Customer.class))).thenAnswer(i -> i.getArgument(0));
        when(roleRepository.findByName("ROLE_CLIENT")).thenReturn(Optional.empty());

        CreateCustomerDTO dto = new CreateCustomerDTO();
        dto.setUsername("test");
        dto.setPassword("Admin123!");

        assertThatThrownBy(() -> customerService.createCustomerWithAccount(dto))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("ROLE_CLIENT");
    }
}
