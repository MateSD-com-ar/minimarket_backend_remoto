package com.virgen_lourdes.minimarket.entity;

import com.virgen_lourdes.minimarket.entity.enums.RoleProduct;
import com.virgen_lourdes.minimarket.validation.ValidProduct;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@ValidProduct //aplicamos la validacion
@Table(name = "products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idProduct;
    private String name;
    private String brand;
    private String code;
    private Double price;
    @Enumerated(EnumType.STRING)
    private RoleProduct roleProduct;
    private String unitMeasure;
    private int stock;

}
