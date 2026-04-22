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

import java.util.List;

@Service
public class CustomerService {

    @Autowired private CustomerRepository customerRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private RoleRepository roleRepository;
    @Autowired private PasswordEncoder passwordEncoder;

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

    public void deleteById(int id) {
        customerRepository.deleteById(id);
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