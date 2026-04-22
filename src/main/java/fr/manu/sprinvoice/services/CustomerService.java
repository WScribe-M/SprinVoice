package fr.manu.sprinvoice.services;

import fr.manu.sprinvoice.dto.CreateCustomerDTO;
import fr.manu.sprinvoice.models.Customer;
import fr.manu.sprinvoice.models.Role;
import fr.manu.sprinvoice.models.User;
import fr.manu.sprinvoice.repositories.CustomerRepository;
import fr.manu.sprinvoice.repositories.RoleRepository;
import fr.manu.sprinvoice.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomerService {

    @Autowired private CustomerRepository customerRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private RoleRepository roleRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    @Transactional // si une des deux insertions échoue, les deux sont annulées
    public void createCustomerWithAccount(CreateCustomerDTO dto) {

        // 1. Créer le Customer
        Customer customer = new Customer();
        customer.setName(dto.getName());
        customer.setCorporateName(dto.getCorporateName());
        customer.setAddress(dto.getAddress());
        customer.setZipcode(dto.getZipcode());
        customer.setCity(dto.getCity());
        customer.setDelay(dto.getDelay());
        customerRepository.save(customer);

        // 2. Récupérer le rôle CLIENT
        Role clientRole = roleRepository.findByName("ROLE_CLIENT")
                .orElseThrow(() -> new RuntimeException("Rôle ROLE_CLIENT introuvable"));

        // 3. Créer le User lié à ce Customer
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRole(clientRole);
        user.setCustomer(customer);
        userRepository.save(user);
    }
}