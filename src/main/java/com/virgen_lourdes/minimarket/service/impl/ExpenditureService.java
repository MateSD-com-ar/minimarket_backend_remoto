package com.virgen_lourdes.minimarket.service.impl;

import com.virgen_lourdes.minimarket.entity.Expenditure;
import com.virgen_lourdes.minimarket.entity.enums.TypeExpenditure;
import com.virgen_lourdes.minimarket.exceptions.customExceptions.ExpenditureException;
import com.virgen_lourdes.minimarket.exceptions.customExceptions.ProductCreationException;
import com.virgen_lourdes.minimarket.repository.IExpenditureRepository;
import com.virgen_lourdes.minimarket.service.IExpenditureService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class ExpenditureService implements IExpenditureService {

    @Autowired
    private IExpenditureRepository expenditureRepository;

    @Override
    public void saveExpenditure(Expenditure expenditure) {
        expenditureRepository.save(expenditure);
    }

    @Override
    public void deletedExpenditure(Long idExpenditure) {
        expenditureRepository.findById(idExpenditure).orElseThrow(() -> new ExpenditureException("Expenditure not found"));
        try {
            expenditureRepository.deleteById(idExpenditure);
        } catch (Exception e) {
            throw new ExpenditureException("Error deleted expenditure");
        }
    }

    @Override
    public void editExpenditure(Long idExpenditure, Expenditure expenditure) {
        Expenditure expenditure1 = expenditureRepository.findById(idExpenditure).orElseThrow(() -> new ExpenditureException("Expenditure not found"));

        if (expenditure.getDateExpenditure() != null) {
            expenditure1.setDateExpenditure(expenditure.getDateExpenditure());
        }
        if (expenditure.getTypeExpenditure() != null) {
            expenditure1.setTypeExpenditure(expenditure.getTypeExpenditure());
        }
        if (expenditure.getReason() != null) {
            expenditure1.setReason(expenditure.getReason());
        }
        if (expenditure.getAmountMoney() != null && expenditure.getAmountMoney() > 0) {
            expenditure1.setAmountMoney(expenditure.getAmountMoney());
        }
        try {
            expenditureRepository.save(expenditure1);
        } catch (Exception e) {
            throw new RuntimeException("Error editing expenditure: Empty required fields");
        }
    }

    @Override
    public List<Expenditure> getAll() {
        List<Expenditure> expenditureList = expenditureRepository.findAll();
        if (expenditureList.isEmpty()) {
            throw new ExpenditureException("There are no registered expenditure");
        }
        return expenditureList;

    }

    @Override
    public Expenditure getById(Long idExpenditure) {
        Expenditure expenditure = expenditureRepository.findById(idExpenditure).orElseThrow(() -> new ExpenditureException("Expenditure not found"));
        return expenditure;
    }

    @Override
    public List<Expenditure> getByDate(LocalDate dateExpenditure) {
        List<Expenditure> expenditureList = expenditureRepository.findByDateExpenditure(dateExpenditure);
        if (expenditureList.isEmpty()) {
            throw new ExpenditureException("There are no registered expenditures on this date");
        }
        return expenditureList;
    }
}
