package fr.manu.sprinvoice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class CreateCustomerDTO {
    private String name;
    private String corporateName;
    private String address;
    private String zipcode;
    private String city;
    private int delay;

    // Infos User
    @NotBlank
    private String username;

    @NotBlank
    @Pattern(
        regexp = "^(?=.*[A-Z])(?=.*[0-9])(?=.*[^A-Za-z0-9]).{8,}$",
        message = "Le mot de passe doit contenir au moins 8 caractères, une majuscule, un chiffre et un caractère spécial."
    )
    private String password;

    // getters / setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCorporateName() { return corporateName; }
    public void setCorporateName(String corporateName) { this.corporateName = corporateName; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getZipcode() { return zipcode; }
    public void setZipcode(String zipcode) { this.zipcode = zipcode; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public int getDelay() { return delay; }
    public void setDelay(int delay) { this.delay = delay; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
