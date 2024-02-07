package com.example.batchprocessing.repository;

import java.io.Serializable;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.batchprocessing.model.Customer;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Serializable>{

}
