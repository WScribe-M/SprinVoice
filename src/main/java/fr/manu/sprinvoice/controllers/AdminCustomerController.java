package fr.manu.sprinvoice.controllers;

import fr.manu.sprinvoice.dto.CreateCustomerDTO;
import fr.manu.sprinvoice.models.Customer;
import fr.manu.sprinvoice.repositories.UserRepository;
import fr.manu.sprinvoice.services.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/customers")
public class AdminCustomerController {

    @Autowired private CustomerService customerService;
    @Autowired private UserRepository userRepository;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("customers", customerService.findAll());
        return "admin/customers/list";
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("dto", new CreateCustomerDTO());
        return "admin/customers/create";
    }

    @PostMapping("/create")
    public String create(@ModelAttribute CreateCustomerDTO dto, Model model) {
        if (userRepository.findByUsername(dto.getUsername()).isPresent()) {
            model.addAttribute("error", "Ce nom d'utilisateur est déjà pris.");
            model.addAttribute("dto", dto);
            return "admin/customers/create";
        }
        customerService.createCustomerWithAccount(dto);
        return "redirect:/admin/customers";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable int id, Model model) {
        model.addAttribute("customer", customerService.findById(id));
        return "admin/customers/edit";
    }

    @PostMapping("/{id}/edit")
    public String edit(@PathVariable int id, @ModelAttribute Customer customer) {
        customer.setId(id);
        customerService.save(customer);
        return "redirect:/admin/customers";
    }

    @GetMapping("/{id}/delete")
    public String delete(@PathVariable int id) {
        customerService.deleteById(id);
        return "redirect:/admin/customers";
    }
}