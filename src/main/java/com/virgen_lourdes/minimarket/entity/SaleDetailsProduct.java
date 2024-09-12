package com.virgen_lourdes.minimarket.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"sale"})
@Table(name = "saledetailsproduct")
public class SaleDetailsProduct {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idDetails;
    private Double quantity;
    private Double unitPrice;
    private Double totalPriceDetail;
    private String unitMeasure;
    private String description;
    @ManyToOne
    @JoinColumn(name = "id_product", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_sale", nullable = false)
    private Sale sale;

}
