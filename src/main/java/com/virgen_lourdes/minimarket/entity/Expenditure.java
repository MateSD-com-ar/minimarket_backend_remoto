package com.virgen_lourdes.minimarket.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.virgen_lourdes.minimarket.entity.enums.TypeExpenditure;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "expenditures")
public class Expenditure {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idExpenditure;
    @CreationTimestamp
    @Column(nullable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    private LocalDate dateExpenditure;
    @Enumerated(EnumType.STRING)
    @NotNull(message = "Type Expenditure must not be null")
    private TypeExpenditure typeExpenditure;
    @NotNull(message = "Reason must not be null")
    @Size(min = 1, message = "The field must have at least 1 character")
    private String reason;
    @NotNull(message = "Quantity must not be null")
    @Positive(message = "Quantity must be greater than zero")
    private Double amountMoney;
}
