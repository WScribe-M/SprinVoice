package fr.manu.sprinvoice.models;

import jakarta.persistence.*;

@Entity
@Table(name = "CUSTOMER")
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "customer_seq")
    @SequenceGenerator(name = "customer_seq", sequenceName = "CUSTOMER_SEQ", allocationSize = 1)
    private int id;

    @Column(name= "NAME")
    private String name;

    @Column(name = "CORPORATE_NAME")
    private String corporateName;

    @Column(name = "ADDRESS")
    private String address;

    @Column(name = "ZIPCODE")
    private String zipcode;

    @Column(name = "CITY")
    private String city;

    @Column(name = "DELAY")
    private int delay;

    @Column(name = "EMAIL")
    private String email;

    public String getCorporateName() {
        return corporateName;
    }

    public void setCorporateName(String corporateName) {
        this.corporateName = corporateName;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getZipcode() {
        return zipcode;
    }

    public void setZipcode(String zipcode) {
        this.zipcode = zipcode;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public int getDelay() {
        return delay;
    }

    public void setDelay(int delay) {
        this.delay = delay;
    }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}
