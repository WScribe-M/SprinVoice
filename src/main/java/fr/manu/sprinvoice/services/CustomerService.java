package fr.manu.sprinvoice.services;

import fr.manu.sprinvoice.dto.CreateCustomerDTO;
import fr.manu.sprinvoice.models.Customer;
import fr.manu.sprinvoice.models.Role;
import fr.manu.sprinvoice.models.User;
import fr.manu.sprinvoice.repositories.CustomerRepository;
import fr.manu.sprinvoice.repositories.InvoiceRepository;
import fr.manu.sprinvoice.repositories.QuoteRepository;
import fr.manu.sprinvoice.repositories.RoleRepository;
import fr.manu.sprinvoice.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CustomerService {

    @Autowired private CustomerRepository customerRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private RoleRepository roleRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private InvoiceRepository invoiceRepository;
    @Autowired private QuoteRepository quoteRepository;

    public List<Customer> findAll() {
        return customerRepository.findAll();
    }

    public Customer findById(int id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Client introuvable : " + id));
    }

    public Customer save(Customer customer) {
        return customerRepository.save(customer);
    }

    @Transactional
    public void anonymize(int customerId) {
        Customer customer = findById(customerId);
        customer.setName("Client anonymisé");
        customer.setCorporateName(null);
        customer.setAddress(null);
        customer.setZipcode(null);
        customer.setCity(null);
        customer.setEmail(null);
        customerRepository.save(customer);

        // Détache le lien User→Customer avant suppression pour éviter le cascade ALL
        userRepository.findByCustomerId(customerId).ifPresent(user -> {
            user.setCustomer(null);
            userRepository.save(user);
            userRepository.delete(user);
        });
    }

    @Transactional
    public void deleteCustomerWithAllData(int customerId) {
        // Supprime les factures (cascade vers InvoiceRow)
        invoiceRepository.deleteAll(invoiceRepository.findByCustomerId(customerId));
        // Supprime les devis (cascade vers QuoteRow)
        quoteRepository.deleteAll(quoteRepository.findByCustomerId(customerId));
        // Supprime l'utilisateur lié — le cascade ALL sur User.customer supprime aussi le Customer
        userRepository.findByCustomerId(customerId)
                .ifPresentOrElse(
                        userRepository::delete,
                        () -> customerRepository.deleteById(customerId)
                );
    }

    @Transactional
    public void createCustomerWithAccount(CreateCustomerDTO dto) {
        Customer customer = new Customer();
        customer.setName(dto.getName());
        customer.setCorporateName(dto.getCorporateName());
        customer.setAddress(dto.getAddress());
        customer.setZipcode(dto.getZipcode());
        customer.setCity(dto.getCity());
        customer.setDelay(dto.getDelay());
        customer.setEmail(dto.getEmail());
        customerRepository.save(customer);

        Role clientRole = roleRepository.findByName("ROLE_CLIENT")
                .orElseThrow(() -> new RuntimeException("Rôle ROLE_CLIENT introuvable"));

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRole(clientRole);
        user.setCustomer(customer);
        userRepository.save(user);
    }
}
