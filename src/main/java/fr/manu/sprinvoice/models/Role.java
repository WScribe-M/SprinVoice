package fr.manu.sprinvoice.models;

import jakarta.persistence.*;

@Entity
@Table(name = "ROLE")
public class Role {
    //Nous sommes obligés de creer une séquence car nous sommes sur Oracle
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "role_seq")
    @SequenceGenerator(name = "role_seq", sequenceName = "ROLE_SEQ", allocationSize = 1)
    private int id;

    private String name; // ADMIN / CLIENT

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}