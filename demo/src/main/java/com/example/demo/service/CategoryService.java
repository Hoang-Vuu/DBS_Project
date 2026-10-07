package com.example.demo.service;

import com.example.demo.entity.ProductCategory;
import com.example.demo.repository.CategoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {

    private final CategoryRepository repository;

    public CategoryService(CategoryRepository repository) {
        this.repository = repository;
    }

    public List<ProductCategory> getAll() {
        return repository.findAll();
    }

    public ProductCategory getById(Integer id) {
        return repository.findById(id).orElse(null);
    }

    public ProductCategory save(ProductCategory category) {
        return repository.save(category);
    }

    public void delete(Integer id) {
        repository.deleteById(id);
    }
}
