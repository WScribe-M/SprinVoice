package fr.manu.sprinvoice.controllers;

import fr.manu.sprinvoice.dto.CreateCustomerDTO;
import fr.manu.sprinvoice.repositories.UserRepository;
import fr.manu.sprinvoice.services.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/customers")
public class AdminCustomerController {

    @Autowired private CustomerService customerService;
    @Autowired private UserRepository userRepository;

    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("dto", new CreateCustomerDTO());
        return "admin/customers/create";
    }

    @PostMapping("/create")
    public String create(@ModelAttribute CreateCustomerDTO dto, Model model) {

        // vérifier si le username est déjà pris
        if (userRepository.findByUsername(dto.getUsername()).isPresent()) {
            model.addAttribute("error", "Ce nom d'utilisateur est déjà pris.");
            model.addAttribute("dto", dto);
            return "admin/customers/create";
        }

        customerService.createCustomerWithAccount(dto);
        return "redirect:/admin/customers";
    }
}