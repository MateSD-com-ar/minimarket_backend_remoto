package com.virgen_lourdes.minimarket.service.impl;

import com.virgen_lourdes.minimarket.dto.requestDto.SaleRequestDto;
import com.virgen_lourdes.minimarket.dto.responseDto.SaleResponseDto;
import com.virgen_lourdes.minimarket.entity.Sale;
import com.virgen_lourdes.minimarket.entity.SaleDetailsProduct;
import com.virgen_lourdes.minimarket.entity.User;
import com.virgen_lourdes.minimarket.entity.enums.Status;
import com.virgen_lourdes.minimarket.exceptions.customExceptions.NotFoundException;
import com.virgen_lourdes.minimarket.repository.ISaleRepository;
import com.virgen_lourdes.minimarket.repository.IUserRepository;
import com.virgen_lourdes.minimarket.service.ICrudService;
import jakarta.validation.ValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class SaleService implements ICrudService<SaleRequestDto, SaleResponseDto, Long> {

    @Autowired
    private ISaleRepository saleRepository;

    @Autowired
    private IUserRepository userRepository;

    @Override
    public SaleResponseDto create(SaleRequestDto saleRequestDto) {
        try {
            User user = userRepository.findById(saleRequestDto.getUserId())
                    .orElseThrow(() -> new NotFoundException("No se encontró al empleado vendedor con el ID proporcionado"));

            Double total = calculateTotal(saleRequestDto.getSaleDetailsProducts());
            System.out.println(saleRequestDto.getSaleDetailsProducts());
            Sale sale = Sale.builder()
                    .CUIL(saleRequestDto.getCUIL())
                    .client(saleRequestDto.getClient())
                    .subtotal(total)
                    .total(total)
                    .status(Status.PENDING)
                    .user(user)
                    .saleDetailsProducts(saleRequestDto.getSaleDetailsProducts())
                    .build();
            saleRepository.save(sale);
            return SaleResponseDto.of(sale);
        } catch (ValidationException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    private Double calculateTotal(List<SaleDetailsProduct> saleDetailsProducts) {
        Double totalPrice = saleDetailsProducts.stream()
                .mapToDouble(detail -> detail.getTotalPriceDetail() * detail.getQuantity())
                .sum();

        DecimalFormat df = new DecimalFormat("#.##");
        return Double.parseDouble(df.format(totalPrice));
    }

    @Override
    public List<SaleResponseDto> read(SaleRequestDto saleRequestDto) {
        try {
            List<Sale> filteredSales = filterSales(saleRequestDto, saleRepository.findAll());
            return filteredSales.stream().map(SaleResponseDto::of).toList();
        } catch (MethodArgumentTypeMismatchException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public SaleResponseDto update(SaleRequestDto saleRequestDto, Long id) {
        try {
            Sale sale = saleRepository.findById(id)
                    .orElseThrow(() -> new NotFoundException("No se encontró la venta con el ID proporcionado"));

            if (saleRequestDto.getDiscount() != null) sale.setTotal(sale.getSubtotal() - saleRequestDto.getDiscount());
            if ((saleRequestDto.getInterest() != null
                    && sale.getStatus().equals(Status.CREDIT))) {
                sale.setTotal(sale.getTotal() + saleRequestDto.getInterest());
            }
            if (saleRequestDto.getStatus() != null) {
                if (saleRequestDto.getStatus().equals(Status.PAID)) {
                    sale.setStatus(Status.PAID);
                    sale.setPaymentDate(LocalDateTime.now());
                }
                if (saleRequestDto.getStatus().equals(Status.CREDIT)) {
                    sale.setStatus(Status.CREDIT);
                }
                sale.setPaymentMethod(saleRequestDto.getPaymentMethod());
            }
            if (saleRequestDto.getSaleDetailsProducts() != null) {
                sale.setSaleDetailsProducts(saleRequestDto.getSaleDetailsProducts());
            }
            if (saleRequestDto.getSubtotal() != null) sale.setSubtotal(saleRequestDto.getSubtotal());
            if (saleRequestDto.getTotal() != null) sale.setTotal(saleRequestDto.getTotal());

            sale = saleRepository.save(sale);
            return SaleResponseDto.of(sale);
        } catch (MethodArgumentTypeMismatchException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public void delete(Long id) {
        Sale sale = saleRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("No se encontró la venta con el ID proporcionado"));
        saleRepository.delete(sale);
    }

    List<Sale> filterSales(SaleRequestDto saleRequestDto, List<Sale> sales) {
        List<Sale> filteredSales = sales.stream()
                .filter(sale -> saleRequestDto.getId() == null ||
                        sale.getId().equals(saleRequestDto.getId()))
                .filter(sale -> saleRequestDto.getCUIL() == null ||
                        sale.getCUIL().toLowerCase().contains(saleRequestDto.getCUIL().toLowerCase()))
                .filter(sale -> saleRequestDto.getClient() == null ||
                        sale.getClient().toLowerCase().contains((saleRequestDto.getClient().toLowerCase())))
                .filter(sale -> saleRequestDto.getPaymentDate() == null ||
                        sale.getPaymentDate().equals(saleRequestDto.getPaymentDate()))
                .filter(sale -> saleRequestDto.getPaymentMethod() == null ||
                        sale.getPaymentMethod().equals(saleRequestDto.getPaymentMethod()))
                .filter(sale -> saleRequestDto.getStatus() == null ||
                        sale.getStatus().equals(saleRequestDto.getStatus()))
                .filter(sale -> saleRequestDto.getUserId() == null ||
                        sale.getUser().getId().equals(saleRequestDto.getUserId()))
                .filter(sale -> saleRequestDto.getCreatedAt() == null ||
                        sale.getCreatedAt().equals(saleRequestDto.getCreatedAt()))
                .toList();

        if (filteredSales.isEmpty()) {
            throw new NotFoundException("No se encontraron ventas");
        }
        return filteredSales;
    }

}
