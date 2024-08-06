package com.virgen_lourdes.minimarket.entity;

import com.virgen_lourdes.minimarket.validation.ValidProduct;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "saledetailsproduct")
public class SaleDetailsProduct {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idDetails;
    private Double amount;
    private Double unitPrice;
    private Double totalPriceDetail;
    @ManyToOne
    @JoinColumn(name = "sale_id", nullable = false)
    private Sale sale;
    @ManyToOne
    @JoinColumn(name = "id_product", nullable = false)
    private Product product;

}
