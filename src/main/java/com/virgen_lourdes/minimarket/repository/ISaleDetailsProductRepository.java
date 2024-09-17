package com.virgen_lourdes.minimarket.repository;

import com.virgen_lourdes.minimarket.entity.Sale;
import com.virgen_lourdes.minimarket.entity.SaleDetailsProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ISaleDetailsProductRepository extends JpaRepository<SaleDetailsProduct, Long> {

    List<SaleDetailsProduct> findBySale(Sale sale);
}
