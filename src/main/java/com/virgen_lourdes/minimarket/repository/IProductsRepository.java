package com.virgen_lourdes.minimarket.repository;

import com.virgen_lourdes.minimarket.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IProductsRepository extends JpaRepository<Product, Long> {
    List<Product> findByName(String name);
    Optional<Product> findByCode(String code);
    boolean existsByCode(String code);
}
