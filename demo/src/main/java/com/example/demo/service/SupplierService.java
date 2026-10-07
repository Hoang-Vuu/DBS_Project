package com.example.demo.service;

import com.example.demo.entity.Supplier;
import com.example.demo.repository.SupplierRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SupplierService {

    private final SupplierRepository repository;

    public SupplierService(SupplierRepository repository) {
        this.repository = repository;
    }

    public List<Supplier> getAll() {
        return repository.findAll();
    }

    public Supplier getById(Integer id) {
        return repository.findById(id).orElse(null);
    }

    public Supplier save(Supplier supplier) {
        return repository.save(supplier);
    }

    public void delete(Integer id) {
        repository.deleteById(id);
    }
}