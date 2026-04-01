package fr.manu.sprinvoice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;

@SpringBootApplication
@EntityScan(basePackages = "fr.manu.sprinvoice.models")
public class SprinvoiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(SprinvoiceApplication.class, args);
	}

}
