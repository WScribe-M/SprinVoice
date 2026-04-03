package fr.manu.sprinvoice.controllers;

import org.springframework.ui.Model;
import fr.manu.sprinvoice.models.Role;
import fr.manu.sprinvoice.models.User;
import fr.manu.sprinvoice.repositories.RoleRepository;
import fr.manu.sprinvoice.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/auth")
public class AuthController {

    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private UserRepository userRepository;

    @GetMapping("/login")
    public String loginPage() {
        return "auth/login"; // → templates/login.html
    }

//    @GetMapping("/register")
//    public String registerPage() {
//        return "auth/register"; // → templates/register.html
//    }

    //@Autowired private RoleRepository roleRepository;

//    @PostMapping("/register")
//    public String register(@RequestParam String username,
//                           @RequestParam String password,
//                           Model model) {
//        if (userRepository.findByUsername(username).isPresent()) {
//            model.addAttribute("error", "Ce nom d'utilisateur est déjà pris.");
//            return "auth/register";
//        }
//
//        // récupère le rôle CLIENT depuis la BDD
//        Role defaultRole = roleRepository.findByName("CLIENT")
//                .orElseThrow(() -> new RuntimeException("Rôle CLIENT introuvable en BDD"));
//
//        User user = new User();
//        user.setUsername(username);
//        user.setPassword(passwordEncoder.encode(password));
//        user.setRole(defaultRole);
//        userRepository.save(user);
//
//        return "redirect:/auth/login";
//    }
}