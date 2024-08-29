package com.virgen_lourdes.minimarket.repository;

import com.virgen_lourdes.minimarket.entity.Expenditure;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface IExpenditureRepository extends JpaRepository<Expenditure, Long> {
    List<Expenditure> findByDateExpenditure(LocalDate dateExpenditure);
}
