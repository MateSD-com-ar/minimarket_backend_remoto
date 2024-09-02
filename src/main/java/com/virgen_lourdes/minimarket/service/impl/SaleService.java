package com.virgen_lourdes.minimarket.service.impl;

import com.virgen_lourdes.minimarket.dto.requestDto.SaleDetailsProductRequestDto;
import com.virgen_lourdes.minimarket.dto.requestDto.SaleRequestDto;
import com.virgen_lourdes.minimarket.dto.responseDto.SaleDetailsProductResponseDto;
import com.virgen_lourdes.minimarket.dto.responseDto.SaleResponseDto;
import com.virgen_lourdes.minimarket.entity.Product;
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
import org.springframework.stereotype.Service;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

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
                } else {
                    throw new ValidationException("La venta necesita estar a crédito para agregar intereses");
                }
            }

            // Actualizar descuento si se proporciona
            if (saleRequestDto.getDiscount() != null) {
                // El descuento no puede ser mayor al total de la venta
                if (saleRequestDto.getDiscount() > sale.getTotal()) {
                    throw new ValidationException("El interés no puede ser mayor al total de la venta");
                }
                sale.setDiscount(saleRequestDto.getDiscount());
            }

            // Actualizar el estado de la venta, el método de pago es requerido
            if (saleRequestDto.getPaymentStatus() != null) {
                if (sale.getPaymentStatus().equals(PaymentStatus.PAID)) {
                    throw new ValidationException("No se puede cambiar el estado de una venta pagada");
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
                        sale.setPaymentStatus(PaymentStatus.PAID);
                        sale.setPaymentMethod(saleRequestDto.getPaymentMethod());
                        sale.setPaymentDate(LocalDateTime.now());
                    } else {
                        throw new ValidationException("Se requiere indicar un método de pago para cambiar el estado de la venta");
                    }
                }
            }

            // Actualizar precio total de la venta
            if (sale.getInterest() != null) {
                sale.setTotal(sale.getSubtotal() + sale.getInterest());
            } else if (sale.getDiscount() != null) {
                sale.setTotal(sale.getSubtotal() - sale.getDiscount());
            } else {
                sale.setTotal(sale.getSubtotal());
            }

            sale = saleRepository.save(sale);
            SaleResponseDto saleResponseDto = SaleResponseDto.of(sale);
            saleResponseDto.setSaleDetailsProducts(toListDetailsDto(sale));

            return saleResponseDto;
//            return SaleResponseDto.of(sale);
        } catch (MethodArgumentTypeMismatchException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    List<SaleDetailsProductResponseDto> toListDetailsDto(Sale sale) {
        List<SaleDetailsProduct> filteredDetails = sale.getSaleDetailsProducts()
                .stream().filter(item -> item.getTotalPriceDetail() > 0).toList();
        return filteredDetails.stream().map(SaleDetailsProductResponseDto::of).toList();
    }

//    public void updateSaleDetails(Sale sale, List<SaleDetailsProductRequestDto> newDetailsDtos) {
//        List<SaleDetailsProduct> existingDetails = sale.getSaleDetailsProducts();
//
    // Filtrar los IDs de detalles existentes
//        List<Long> dtoIds = newDetailsDtos.stream()
//                .map(SaleDetailsProductRequestDto::getIdDetails)
//                .filter(Objects::nonNull)
//                .toList();
//
//        // Filtrar los detalles existentes que no están en la lista de IDs
//        List<SaleDetailsProduct> detailsToRemove = existingDetails.stream()
//                .filter(detail -> !dtoIds.contains(detail.getIdDetails()))
//                .toList();
//
//        // Eliminar los detalles que ya no están presentes en la lista de DTOs
//        detailsToRemove.forEach(detail -> sale.getSaleDetailsProducts().remove(detail));
//
//        // Recorrer la lista de nuevos detalles y actualizar o crear los detalles de la venta
//        for (SaleDetailsProductRequestDto newDetailsDto : newDetailsDtos) {
//            if (newDetailsDto.getIdDetails() != null) {
//                saleDetailsProductService.editDetails(newDetailsDto.getIdDetails(), newDetailsDto);
//            } else {
//                Product product = productsRepository.findById(newDetailsDto.getProduct())
//                        .orElseThrow(() -> new NotFoundException("Product does not exist or product not found"));
//                SaleDetailsProduct saleDetailsProduct = new SaleDetailsProduct();
//                saleDetailsProduct.setQuantity(newDetailsDto.getQuantity());
//                saleDetailsProduct.setUnitPrice(product.getPrice());
//                saleDetailsProduct.setTotalPriceDetail(saleDetailsProduct.getQuantity() * saleDetailsProduct.getUnitPrice());
//                saleDetailsProduct.setProduct(product);
//                saleDetailsProduct.setSale(sale);
//                sale.getSaleDetailsProducts().add(saleDetailsProduct);
//            }
//        }
//    }

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


//    List<SaleDetailsProduct> createDetailsProduct(List<SaleDetailsProductRequestDto> saleDetailsProducts, Sale sale) {
//        return saleDetailsProductService.createDetails(saleDetailsProducts, sale);
//    }
}
