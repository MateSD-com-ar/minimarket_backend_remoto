package com.virgen_lourdes.minimarket.dto.requestDto;

import com.virgen_lourdes.minimarket.entity.enums.PaymentMethod;
import com.virgen_lourdes.minimarket.entity.enums.PaymentStatus;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
public class SaleRequestDto {

    private Long id;

    @NotBlank(message = "El nombre no puede estar vacío")
    @Size(min = 3, message = "El nombre debe tener al menos 3 caracteres")
    private String client;

    private String cuil;
    private LocalDateTime paymentDate;
    private PaymentMethod paymentMethod;

    @PositiveOrZero(message = "El descuento debe ser un valor positivo o cero")
    private Double discount;

    @PositiveOrZero(message = "El interés debe ser un valor positivo o cero")
    private Double interest;
    private Double subtotal;
    private Double total;
    private PaymentStatus paymentStatus;

    @NotNull(message = "La venta debe tener un empleado vendedor asociado")
    private Long userId;

//    @NotEmpty(message = "La venta debe tener al menos un producto")
//    private List<SaleDetailsProductRequestDto> saleDetailsProducts;

    private LocalDateTime createdAt;

}
