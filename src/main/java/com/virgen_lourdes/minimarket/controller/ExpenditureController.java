package com.virgen_lourdes.minimarket.controller;

import com.virgen_lourdes.minimarket.entity.Expenditure;
import com.virgen_lourdes.minimarket.exceptions.customExceptions.ExpenditureException;
import com.virgen_lourdes.minimarket.service.IExpenditureService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/expenditure")
public class ExpenditureController {
    @Autowired
    private IExpenditureService expenditureService;

    @GetMapping("/get")
    public ResponseEntity<?> getAll() {
        try {
            List<Expenditure> expenditureList = expenditureService.getAll();
            return ResponseEntity.ok(expenditureList);
        } catch (ExpenditureException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping("/get/{idExpenditure}")
    public ResponseEntity<?> getById(@PathVariable Long idExpenditure) {
        try {
            Expenditure expenditure = expenditureService.getById(idExpenditure);
            return ResponseEntity.ok(expenditure);
        } catch (ExpenditureException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping("/get/date")
    public ResponseEntity<?> getByDate(@RequestParam("date") @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate dateExpenditure) {
        try {
            List<Expenditure> expenditureList = expenditureService.getByDate(dateExpenditure);
            return ResponseEntity.ok(expenditureList);
        } catch (ExpenditureException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @DeleteMapping("/delete/{idExpenditure}")
    public ResponseEntity<?> deleteExpenditure(@PathVariable Long idExpenditure) {
        try {
            expenditureService.deletedExpenditure(idExpenditure);
            return ResponseEntity.ok("Expenditure deleted");
        } catch (ExpenditureException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PostMapping("/create")
    public ResponseEntity<?> createExpenditure(@Valid @RequestBody Expenditure expenditure) {
        expenditureService.saveExpenditure(expenditure);
        return ResponseEntity.ok("Expenditure created");
    }

    @PutMapping("/edit/{idExpenditure}")
    public ResponseEntity<?> editExpenditure(@PathVariable Long idExpenditure, @RequestBody Expenditure expenditure) {
        try {
            expenditureService.editExpenditure(idExpenditure, expenditure);
            return ResponseEntity.ok(expenditureService.getById(idExpenditure));
        } catch (ExpenditureException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

}
