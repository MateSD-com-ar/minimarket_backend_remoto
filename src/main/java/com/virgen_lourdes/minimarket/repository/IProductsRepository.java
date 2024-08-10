package com.virgen_lourdes.minimarket.repository;

import com.virgen_lourdes.minimarket.entity.Product;
import com.virgen_lourdes.minimarket.entity.enums.RoleProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IProductsRepository extends JpaRepository<Product, Long> {
    List<Product> findByName(String name);
    Optional<Product> findByCode(String code);
    boolean existsByCode(String code);
    List<Product> findByRoleProduct(RoleProduct roleProduct);
}
