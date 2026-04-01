package fr.manu.sprinvoice.repositories;

import fr.manu.sprinvoice.models.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Integer> {
}
