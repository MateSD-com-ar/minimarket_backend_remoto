package com.virgen_lourdes.minimarket.service.impl;

import com.virgen_lourdes.minimarket.dto.requestDto.SaleDetailsProductRequestDto;
import com.virgen_lourdes.minimarket.dto.requestDto.SaleRequestDto;
import com.virgen_lourdes.minimarket.dto.responseDto.SaleResponseDto;
import com.virgen_lourdes.minimarket.entity.Product;
import com.virgen_lourdes.minimarket.entity.Sale;
import com.virgen_lourdes.minimarket.entity.SaleDetailsProduct;
import com.virgen_lourdes.minimarket.entity.User;
import com.virgen_lourdes.minimarket.entity.enums.PaymentMethod;
import com.virgen_lourdes.minimarket.entity.enums.Status;
import com.virgen_lourdes.minimarket.exceptions.customExceptions.NotFoundException;
import com.virgen_lourdes.minimarket.repository.IProductsRepository;
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
import java.util.stream.Collectors;

@Service
public class SaleService implements ICrudService<SaleRequestDto, SaleResponseDto, Long> {

    @Autowired
    private ISaleRepository saleRepository;

    @Autowired
    private IUserRepository userRepository;

    @Autowired
    private SaleDetailsProductService saleDetailsProductService;

    @Autowired
    private IProductsRepository productsRepository;

    @Override
    public SaleResponseDto create(SaleRequestDto saleRequestDto) {
        try {
            User user = userRepository.findById(saleRequestDto.getUserId())
                    .orElseThrow(() -> new NotFoundException("No se encontró al empleado vendedor con el ID proporcionado"));

//            List<SaleDetailsProduct> saleDetailsProduct = createDetailsProduct(saleRequestDto.getSaleDetailsProducts());


            Sale sale = Sale.builder()
                    .CUIL(saleRequestDto.getCuil())
                    .client(saleRequestDto.getClient())
                    .status(Status.PENDING)
                    .user(user)
                    .build();

            List<SaleDetailsProduct> saleDetailsProduct = createDetailsProduct(saleRequestDto.getSaleDetailsProducts(), sale);

            Double total = calculateTotal(saleDetailsProduct);

            sale.setSubtotal(total);
            sale.setTotal(total);
            sale.setSaleDetailsProducts(saleDetailsProduct);

            saleRepository.save(sale);
            return SaleResponseDto.of(sale);
        } catch (ValidationException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    private Double calculateTotal(List<SaleDetailsProduct> saleDetailsProducts) {
        Double totalPrice = saleDetailsProducts.stream()
                .mapToDouble(SaleDetailsProduct::getTotalPriceDetail)
                .sum();

        DecimalFormat df = new DecimalFormat("#.##");
        return Double.parseDouble(df.format(totalPrice));
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
            if (saleRequestDto.getCuil() != null) sale.setCUIL(saleRequestDto.getCuil());

            // Actualizar interes si se proporciona y si la venta está en crédito
            if (saleRequestDto.getInterest() != null) {
                Status currentStatus = sale.getStatus();
                Status requestedStatus = saleRequestDto.getStatus();

                if (Status.CREDIT.equals(currentStatus) || Status.CREDIT.equals(requestedStatus)) {
                    sale.setTotal(sale.getTotal() + saleRequestDto.getInterest());
                    sale.setInterest(saleRequestDto.getInterest());
                } else {
                    throw new ValidationException("La venta necesita estar a crédito para agregar intereses");
                }
            }

            // Actualizar descuento si se proporciona, a su vez actualizar el total de la venta
            if (saleRequestDto.getDiscount() != null) {
                sale.setTotal(sale.getTotal() - saleRequestDto.getDiscount());
                sale.setDiscount(saleRequestDto.getDiscount());
            }

            // Actualizar el estado de la venta, el método de pago es requerido
            if (saleRequestDto.getStatus() != null) {
                if (sale.getStatus().equals(Status.PAID)) {
                    throw new ValidationException("No se puede cambiar el estado de una venta pagada");
                }
                if (sale.getStatus().equals(Status.CREDIT) && saleRequestDto.getStatus().equals(Status.PENDING)) {
                    throw new ValidationException("No se puede cambiar el estado de la venta a pendiente");
                }
                if (saleRequestDto.getStatus().equals(Status.CREDIT)) {
                    sale.setStatus(Status.CREDIT);
                    sale.setPaymentMethod(PaymentMethod.CURRENT_ACCOUNT);
                }
                if (saleRequestDto.getStatus().equals(Status.PAID)) {
                    if (saleRequestDto.getPaymentMethod() != null) {
                        sale.setStatus(Status.PAID);
                        sale.setPaymentMethod(saleRequestDto.getPaymentMethod());
                        sale.setPaymentDate(LocalDateTime.now());
                    } else {
                        throw new ValidationException("Se requiere indicar un método de pago para cambiar el estado de la venta");
                    }
                }
            }

            // Actualizar los detalles de la venta si se proporciona y si el estado de la venta es pendiente
            if (saleRequestDto.getSaleDetailsProducts() != null) {
                if (sale.getStatus().equals(Status.PENDING)) {
                    updateSaleDetails(sale, saleRequestDto.getSaleDetailsProducts());
                } else {
                    throw new ValidationException("No se pueden actualizar los productos de una venta cerrada");
                }
            }
            if (saleRequestDto.getSubtotal() != null) sale.setSubtotal(saleRequestDto.getSubtotal());
            if (saleRequestDto.getTotal() != null) sale.setTotal(saleRequestDto.getTotal());

            sale = saleRepository.save(sale);
            return SaleResponseDto.of(sale);
        } catch (MethodArgumentTypeMismatchException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public void updateSaleDetails(Sale sale, List<SaleDetailsProductRequestDto> newDetailsDtos) {
        List<SaleDetailsProduct> existingDetails = sale.getSaleDetailsProducts();

        // Crear una lista de nuevas entidades a partir de los DTOs
        List<SaleDetailsProduct> newDetails = createDetailsProduct(newDetailsDtos, sale);

        // Eliminar detalles que ya no están en la nueva lista
        existingDetails.removeIf(detail -> !newDetails.contains(detail));

        // Agregar o actualizar los detalles de la nueva lista
        for (SaleDetailsProduct newDetail : newDetails) {
            if (!existingDetails.contains(newDetail)) {
                existingDetails.add(newDetail);
            } else {
                // Actualizar los detalles existentes si es necesario
//                SaleDetailsProduct existingDetail = existingDetails.get(existingDetails.indexOf(newDetail));
//                updateExistingDetail(existingDetail, newDetail);
                SaleDetailsProductRequestDto requestDto = newDetailsDtos.stream()
                        .filter(dto -> dto.getIdDetails().equals(newDetail.getIdDetails()))
                        .findFirst()
                        .orElseThrow(() -> new NotFoundException("No se encontró el detalle de venta con el ID proporcionado"));
                System.out.println(requestDto);
                saleDetailsProductService.editDetails(requestDto.getIdDetails(), requestDto);
            }
        }

        sale.setSaleDetailsProducts(existingDetails);
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
                .filter(sale -> saleRequestDto.getCuil() == null || sale.getCUIL() != null &&
                        sale.getCUIL().toLowerCase().contains(saleRequestDto.getCuil().toLowerCase()))
                .filter(sale -> saleRequestDto.getClient() == null ||
                        sale.getClient().toLowerCase().contains((saleRequestDto.getClient().toLowerCase())))
                .filter(sale -> saleRequestDto.getPaymentDate() == null || sale.getPaymentDate() != null &&
                        sale.getPaymentDate().equals(saleRequestDto.getPaymentDate()))
                .filter(sale -> saleRequestDto.getPaymentMethod() == null || sale.getPaymentMethod() != null &&
                        sale.getPaymentMethod().equals(saleRequestDto.getPaymentMethod()))
                .filter(sale -> saleRequestDto.getStatus() == null ||
                        sale.getStatus().equals(saleRequestDto.getStatus()))
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


    List<SaleDetailsProduct> createDetailsProduct(List<SaleDetailsProductRequestDto> saleDetailsProducts, Sale sale) {
        return saleDetailsProducts.stream().map(item -> {
            SaleDetailsProduct saleDetailsProduct = new SaleDetailsProduct();
            if (item.getIdDetails() != null) saleDetailsProduct.setIdDetails(item.getIdDetails());
            saleDetailsProduct.setQuantity(item.getQuantity());
            saleDetailsProduct.setTotalPriceDetail(item.getTotalPriceDetail());
            saleDetailsProduct.setUnitPrice(item.getUnitPrice());
            saleDetailsProduct.setProduct(productsRepository.findById(item.getProduct())
                    .orElseThrow(() -> new NotFoundException("Product not found")));
            saleDetailsProduct.setSale(sale);
            return saleDetailsProduct;
        }).collect(Collectors.toList());
    }
}
