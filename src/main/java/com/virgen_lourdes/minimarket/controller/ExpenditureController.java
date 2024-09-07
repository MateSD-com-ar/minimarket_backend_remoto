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
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/expenditure")
public class ExpenditureController {
    @Autowired
    private IExpenditureService expenditureService;

    @GetMapping("/get")
    public ResponseEntity<?> getAll() {
        List<Expenditure> expenditureList = expenditureService.getAll();
        return new ResponseEntity<>(expenditureList, HttpStatus.OK);
    }

    @GetMapping("/get/{idExpenditure}")
    public ResponseEntity<?> getById(@PathVariable Long idExpenditure) {
        Expenditure expenditure = expenditureService.getById(idExpenditure);
        return new ResponseEntity<>(expenditure, HttpStatus.OK);
    }

    @GetMapping("/get/date")
    public ResponseEntity<?> getByDate(@RequestParam("date") @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate dateExpenditure) {
        List<Expenditure> expenditureList = expenditureService.getByDate(dateExpenditure);
        return new ResponseEntity<>(expenditureList, HttpStatus.OK);
    }

    @DeleteMapping("/delete/{idExpenditure}")
    public ResponseEntity<?> deleteExpenditure(@PathVariable Long idExpenditure) {
        expenditureService.deletedExpenditure(idExpenditure);
        return ResponseEntity.ok("Expenditure deleted");
    }

    @PostMapping("/create")
    public ResponseEntity<?> createExpenditure(@Valid @RequestBody Expenditure expenditure) {
        expenditureService.saveExpenditure(expenditure);
        return ResponseEntity.ok(Collections.singletonMap("message", "Expenditure created"));
    }

    @PutMapping("/edit/{idExpenditure}")
    public ResponseEntity<?> editExpenditure(@PathVariable Long idExpenditure, @RequestBody Expenditure expenditure) {
        expenditureService.editExpenditure(idExpenditure, expenditure);
        return ResponseEntity.ok(expenditureService.getById(idExpenditure));
    }

}
