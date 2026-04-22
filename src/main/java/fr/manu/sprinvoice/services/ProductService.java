package fr.manu.sprinvoice.services;

import fr.manu.sprinvoice.models.Product;
import fr.manu.sprinvoice.repositories.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {

    @Autowired private ProductRepository productRepository;

    public List<Product> findAll() {
        return productRepository.findAll();
    }

    public Product findById(int id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Produit introuvable : " + id));
    }

    public Product save(Product product) {
        return productRepository.save(product);
    }

    public void deleteById(int id) {
        productRepository.deleteById(id);
    }
}
