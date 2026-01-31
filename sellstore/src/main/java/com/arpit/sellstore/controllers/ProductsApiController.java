package com.arpit.sellstore.controllers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.arpit.sellstore.models.Product;
import com.arpit.sellstore.models.ProductDto;
import com.arpit.sellstore.services.ProductsRepository;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/products")
public class ProductsApiController {

	@Autowired
	private ProductsRepository repo;

	private final String uploadDir = "/sellstore/Public/images";

	@GetMapping
	public List<Product> getAllProducts() {
		return repo.findAll();
	}

	@GetMapping("/{id}")
	public Product getProduct(@PathVariable int id) {
		return repo.findById(id).orElse(null);
	}

	@PostMapping
	public Product createProduct(@Valid @ModelAttribute ProductDto productDto) throws IOException {

		if (productDto.getImageFile() == null || productDto.getImageFile().isEmpty()) {
			throw new RuntimeException("Image file is required");
		}

		Product product = new Product();
		product.setName(productDto.getName());
		product.setBrand(productDto.getBrand());
		product.setCategory(productDto.getCategory());
		product.setPrice(productDto.getPrice());
		product.setDescription(productDto.getDescription());
		product.setCreatedAt(new Date());

		MultipartFile imageFile = productDto.getImageFile();
		String filename = System.currentTimeMillis() + "_" + imageFile.getOriginalFilename();
		Path uploadPath = Paths.get(uploadDir);
		if (!Files.exists(uploadPath)) {
			Files.createDirectories(uploadPath);
		}
		imageFile.transferTo(uploadPath.resolve(filename));
		product.setImageFilename(filename);

		return repo.save(product);
	}

	@PutMapping("/{id}")
	public Product updateProduct(@PathVariable int id, @Valid @ModelAttribute ProductDto productDto)
			throws IOException {

		Product product = repo.findById(id).orElseThrow(() -> new RuntimeException("Product not found"));

		product.setName(productDto.getName());
		product.setBrand(productDto.getBrand());
		product.setCategory(productDto.getCategory());
		product.setPrice(productDto.getPrice());
		product.setDescription(productDto.getDescription());

		MultipartFile imageFile = productDto.getImageFile();
		if (imageFile != null && !imageFile.isEmpty()) {
			String filename = System.currentTimeMillis() + "_" + imageFile.getOriginalFilename();
			Path uploadPath = Paths.get(uploadDir);
			if (!Files.exists(uploadPath)) {
				Files.createDirectories(uploadPath);
			}
			imageFile.transferTo(uploadPath.resolve(filename));
			product.setImageFilename(filename);
		}

		return repo.save(product);
	}

	@DeleteMapping("/{id}")
	public String deleteProduct(@PathVariable int id) {
		repo.deleteById(id);
		return "Product deleted successfully";
	}
}
