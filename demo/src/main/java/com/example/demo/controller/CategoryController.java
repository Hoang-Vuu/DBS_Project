package com.example.demo.controller;

import com.example.demo.entity.ProductCategory;
import com.example.demo.service.CategoryService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/categories")
public class CategoryController {

    private final CategoryService service;

    public CategoryController(CategoryService service) {
        this.service = service;
    }

    @GetMapping
    public List<ProductCategory> getAll() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    public ProductCategory getById(@PathVariable Integer id) {
        return service.getById(id);
    }

    @PostMapping
    public ProductCategory create(
            @RequestBody ProductCategory category) {
        return service.save(category);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Integer id) {
        service.delete(id);
    }
}