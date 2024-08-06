package com.virgen_lourdes.minimarket.repository;

import com.virgen_lourdes.minimarket.entity.Sale;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ISaleRepository extends JpaRepository<Sale, Long> {
}
