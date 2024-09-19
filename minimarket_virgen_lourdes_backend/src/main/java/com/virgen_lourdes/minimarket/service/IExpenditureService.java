package com.virgen_lourdes.minimarket.service;

import com.virgen_lourdes.minimarket.entity.Expenditure;

import java.time.LocalDate;
import java.util.List;

public interface IExpenditureService {
    public void saveExpenditure(Expenditure expenditure);
    public void deletedExpenditure(Long idExpenditure);
    public void editExpenditure(Long idExpenditure, Expenditure expenditure);
    public List<Expenditure> getAll();
    public Expenditure getById(Long idExpenditure);
    public List<Expenditure> getByDate(LocalDate dateExpenditure);
}
