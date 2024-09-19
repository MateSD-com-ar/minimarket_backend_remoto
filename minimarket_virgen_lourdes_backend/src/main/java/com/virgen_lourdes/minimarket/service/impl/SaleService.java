package com.virgen_lourdes.minimarket.service.impl;

import com.virgen_lourdes.minimarket.dto.requestDto.SaleRequestDto;
import com.virgen_lourdes.minimarket.dto.responseDto.SaleDetailsProductResponseDto;
import com.virgen_lourdes.minimarket.dto.responseDto.SaleResponseDto;
import com.virgen_lourdes.minimarket.entity.Sale;
import com.virgen_lourdes.minimarket.entity.SaleDetailsProduct;
import com.virgen_lourdes.minimarket.entity.User;
import com.virgen_lourdes.minimarket.entity.enums.PaymentMethod;
import com.virgen_lourdes.minimarket.entity.enums.PaymentStatus;
import com.virgen_lourdes.minimarket.exceptions.customExceptions.NotFoundException;
import com.virgen_lourdes.minimarket.repository.IProductsRepository;
import com.virgen_lourdes.minimarket.repository.ISaleDetailsProductRepository;
import com.virgen_lourdes.minimarket.repository.ISaleRepository;
import com.virgen_lourdes.minimarket.repository.IUserRepository;
import com.virgen_lourdes.minimarket.service.ICrudService;
import jakarta.transaction.Transactional;
import jakarta.validation.ValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SaleService implements ICrudService<SaleRequestDto, SaleResponseDto, Long> {

    @Autowired
    private ISaleRepository saleRepository;

    @Autowired
    private IUserRepository userRepository;

    @Autowired
    private SaleDetailsProductService saleDetailsProductService;

    @Autowired
    private ISaleDetailsProductRepository saleDetailsProductRepository;

    @Autowired
    private IProductsRepository productsRepository;

    @Override
    @Transactional
    public SaleResponseDto create(SaleRequestDto saleRequestDto) {
        try {
            User user = userRepository.findById(saleRequestDto.getUserId())
                    .orElseThrow(() -> new NotFoundException("No se encontró al empleado vendedor con el ID proporcionado"));

            Sale sale = Sale.builder()
                    .CUIL(saleRequestDto.getCuil())
                    .client(saleRequestDto.getClient())
                    .paymentStatus(PaymentStatus.PENDING)
                    .user(user)
                    .build();
            sale = saleRepository.save(sale);
            return SaleResponseDto.of(sale);
        } catch (ValidationException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public List<SaleResponseDto> read(SaleRequestDto saleRequestDto) {
        try {
            List<Sale> filteredSales = filterSales(saleRequestDto, saleRepository.findAll());
            return filteredSales.stream().map(SaleResponseDto::of).toList();
        } catch (NotFoundException e) {
            throw new NotFoundException(e.getMessage());
        } catch (MethodArgumentTypeMismatchException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public SaleResponseDto update(SaleRequestDto saleRequestDto, Long id) {
        try {
            /* Verificar si el usuario tiene el rol de ADMIN */
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            boolean hasRoleAdmin = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ADMIN"));

            Sale sale = saleRepository.findById(id)
                    .orElseThrow(() -> new NotFoundException("No se encontró la venta con el ID proporcionado"));

            // Verificar si el total de la venta no es mayor a 0 para no realizar modificaciones
            if (sale.getTotal() == null || sale.getTotal() == 0) {
                throw new ValidationException("No se puede modificar una venta que no tiene detalles ni un monto total");
            }

            // Actualizar CUIL si se proporciona
            if (saleRequestDto.getCuil() != null) sale.setCUIL(saleRequestDto.getCuil());

            // Actualizar interes si se proporciona y si la venta está en crédito
            if (saleRequestDto.getInterest() != null) {
                PaymentStatus currentPaymentStatus = sale.getPaymentStatus();
                PaymentStatus requestedPaymentStatus = saleRequestDto.getPaymentStatus();

                if (PaymentStatus.CREDIT.equals(currentPaymentStatus) || PaymentStatus.CREDIT.equals(requestedPaymentStatus)) {
                    sale.setInterest(saleRequestDto.getInterest());
                    sale.setTotal((sale.getTotal() * (saleRequestDto.getInterest() / 100)) + sale.getTotal());
                } else {
                    throw new ValidationException("La venta necesita estar a crédito para agregar intereses");
                }
            }

            // Actualizar descuento si se proporciona
            if (saleRequestDto.getDiscount() != null) {
                // El descuento no puede ser mayor al total de la venta
                if (saleRequestDto.getDiscount() > sale.getTotal()) {
                    throw new ValidationException("El descuento no puede ser mayor al total de la venta");
                }
                sale.setDiscount(saleRequestDto.getDiscount());
                sale.setTotal(sale.getTotal() - saleRequestDto.getDiscount());
            }

            // Actualizar el estado de la venta, el método de pago es requerido
            // Solo el administrador puede cambiar el estado de la venta a "credito" o "pendiente"
            if (saleRequestDto.getPaymentStatus() != null) {
                if (hasRoleAdmin) {
                    if (saleRequestDto.getPaymentStatus().equals(PaymentStatus.PENDING) ||
                            saleRequestDto.getPaymentStatus().equals(PaymentStatus.CREDIT)) {
                        PaymentStatus paymentStatus = sale.getPaymentStatus();
                        if (paymentStatus.equals(PaymentStatus.CREDIT) || paymentStatus.equals(PaymentStatus.PAID)) {
                            if (saleRequestDto.getPaymentStatus().equals(PaymentStatus.CREDIT)) {
                                sale.setPaymentStatus(saleRequestDto.getPaymentStatus());
                                sale.setPaymentMethod(PaymentMethod.CURRENT_ACCOUNT);
                            }
                            if (saleRequestDto.getPaymentStatus().equals(PaymentStatus.PENDING)) {
                                sale.setPaymentStatus(saleRequestDto.getPaymentStatus());
                                sale.setPaymentMethod(null);
                            }
                        }
                    }
                }

                if (sale.getPaymentStatus().equals(PaymentStatus.PAID)) {
                    throw new ValidationException("No tienes permiso para cambiar el estado de una venta pagada");
                }
                if (sale.getPaymentStatus().equals(PaymentStatus.CREDIT) && saleRequestDto.getPaymentStatus().equals(PaymentStatus.PENDING)) {
                    throw new ValidationException("No se puede cambiar el estado de la venta a pendiente");
                }
                if (saleRequestDto.getPaymentStatus().equals(PaymentStatus.CREDIT)) {
                    sale.setPaymentStatus(PaymentStatus.CREDIT);
                    sale.setPaymentMethod(PaymentMethod.CURRENT_ACCOUNT);
                }
                if (saleRequestDto.getPaymentStatus().equals(PaymentStatus.PAID)) {

                    if (saleRequestDto.getPaymentMethod() != null) {
                        if (saleRequestDto.getPaymentMethod() != PaymentMethod.CURRENT_ACCOUNT) {
                            sale.setPaymentStatus(PaymentStatus.PAID);
                            sale.setPaymentMethod(saleRequestDto.getPaymentMethod());
                            sale.setPaymentDate(LocalDateTime.now());
                        } else {
                            throw new ValidationException("El método de pago solo puede ser efectivo, transferencia o tarjeta");
                        }
                    } else {
                        throw new ValidationException("Se requiere indicar un método de pago para cambiar el estado de la venta");
                    }
                }
            }

            sale = saleRepository.save(sale);
            SaleResponseDto saleResponseDto = SaleResponseDto.of(sale);
            saleResponseDto.setSaleDetailsProducts(toListDetailsDto(sale));

            return saleResponseDto;
        } catch (MethodArgumentTypeMismatchException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    List<SaleDetailsProductResponseDto> toListDetailsDto(Sale sale) {
        List<SaleDetailsProduct> filteredDetails = sale.getSaleDetailsProducts()
                .stream().filter(item -> item.getTotalPriceDetail() > 0).toList();
        return filteredDetails.stream().map(SaleDetailsProductResponseDto::of).toList();
    }

    @Override
    public void delete(Long id) {
        Sale sale = saleRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("No se encontró la venta con el ID proporcionado"));
        saleRepository.delete(sale);
    }

    @Override
    public void deactivateUser(Long id) {
    }

    List<Sale> filterSales(SaleRequestDto saleRequestDto, List<Sale> sales) {
        List<Sale> filteredSales = sales.stream()
                .filter(sale -> saleRequestDto.getId() == null ||
                        sale.getId().equals(saleRequestDto.getId()))
                .filter(sale -> saleRequestDto.getCuil() == null || sale.getCUIL() != null &&
                        sale.getCUIL().toLowerCase().contains(saleRequestDto.getCuil().toLowerCase()))
                .filter(sale -> saleRequestDto.getClient() == null ||
                        sale.getClient().toLowerCase().contains((saleRequestDto.getClient().toLowerCase())))
                .filter(sale -> saleRequestDto.getPaymentDate() == null || sale.getPaymentDate() != null &&
                        sale.getPaymentDate().equals(saleRequestDto.getPaymentDate()))
                .filter(sale -> saleRequestDto.getPaymentMethod() == null || sale.getPaymentMethod() != null &&
                        sale.getPaymentMethod().equals(saleRequestDto.getPaymentMethod()))
                .filter(sale -> saleRequestDto.getPaymentStatus() == null ||
                        sale.getPaymentStatus().equals(saleRequestDto.getPaymentStatus()))
                .filter(sale -> saleRequestDto.getUserId() == null ||
                        sale.getUser().getId().equals(saleRequestDto.getUserId()))
                .filter(sale -> saleRequestDto.getCreatedAt() == null ||
                        sale.getCreatedAt().equals(saleRequestDto.getCreatedAt()))
                .sorted((sale1, sale2) -> sale2.getCreatedAt().compareTo(sale1.getCreatedAt()))
                .toList();

        if (filteredSales.isEmpty()) {
            throw new NotFoundException("No se encontraron ventas");
        }
        return filteredSales;
    }
}
