package com.virgen_lourdes.minimarket.repository;

import com.virgen_lourdes.minimarket.entity.SaleDetailsProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ISaleDetailsProductRepository extends JpaRepository<SaleDetailsProduct, Long> {
}
