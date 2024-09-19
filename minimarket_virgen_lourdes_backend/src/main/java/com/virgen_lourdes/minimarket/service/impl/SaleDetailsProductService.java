package com.virgen_lourdes.minimarket.service.impl;

import com.virgen_lourdes.minimarket.dto.requestDto.SaleDetailsProductRequestDto;
import com.virgen_lourdes.minimarket.dto.responseDto.SaleDetailsProductResponseDto;
import com.virgen_lourdes.minimarket.entity.Product;
import com.virgen_lourdes.minimarket.entity.Sale;
import com.virgen_lourdes.minimarket.entity.SaleDetailsProduct;
import com.virgen_lourdes.minimarket.entity.enums.PaymentStatus;
import com.virgen_lourdes.minimarket.entity.enums.RoleProduct;
import com.virgen_lourdes.minimarket.exceptions.customExceptions.NotFoundException;
import com.virgen_lourdes.minimarket.exceptions.customExceptions.ProductNotFoundException;
import com.virgen_lourdes.minimarket.repository.IProductsRepository;
import com.virgen_lourdes.minimarket.repository.ISaleDetailsProductRepository;
import com.virgen_lourdes.minimarket.repository.ISaleRepository;
import com.virgen_lourdes.minimarket.service.ISaleDetailsProductService;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SaleDetailsProductService implements ISaleDetailsProductService {

    @Autowired
    private ISaleDetailsProductRepository saleDetailsProductRepository;

    @Autowired
    private IProductsRepository productsRepository;

    @Autowired
    private ISaleRepository saleRepository;

    @Autowired
    EntityManager entityManager;

    @Override
    public List<SaleDetailsProduct> getAllDetails() {
        List<SaleDetailsProduct> saleDetailsProductList = saleDetailsProductRepository.findAll();
        if (saleDetailsProductList.isEmpty()) {
            throw new RuntimeException("There are no sales details");
        }
        return saleDetailsProductList;

    }

    @Override
    public SaleDetailsProduct getDetailsById(Long idDetails) {
        return saleDetailsProductRepository.findById(idDetails)
                .orElseThrow(() -> new NotFoundException("Details not found"));
    }

    @Override
    @Transactional
    public List<SaleDetailsProductResponseDto> createDetails(List<SaleDetailsProductRequestDto> saleDetailsProductRequestDto) {
        try {
            List<SaleDetailsProduct> saleDetailsProductList = saleDetailsProductRequestDto.stream().map(item -> {
                Product product = productsRepository.findById(item.getProduct())
                        .orElseThrow(() -> new ProductNotFoundException("No existe un producto con el id proporcionado"));

                Sale sale = saleRepository.findById(item.getSaleId())
                        .orElseThrow(() -> new NotFoundException("No existe una venta con el id proporcionado"));
                if (!PaymentStatus.PENDING.equals(sale.getPaymentStatus())) {
                    throw new RuntimeException("La venta esta cerrada y no se pueden agregar detalles de venta");
                }
                return getSaleDetailsProduct(item, product, sale);
            }).toList();

            saleDetailsProductRepository.saveAll(saleDetailsProductList);
            entityManager.flush();

            // Actualizar el precio total de la venta
            Sale sale = saleRepository.findById(saleDetailsProductRequestDto.get(0).getSaleId())
                    .orElseThrow(() -> new NotFoundException("No existe una venta con el id proporcionado"));
            List<SaleDetailsProduct> saleDetailsProducts = saleDetailsProductRepository.findBySale(sale);
            updateSaleTotalPrice(sale, saleDetailsProducts);

            return saleDetailsProductList.stream()
                    .map(SaleDetailsProductResponseDto::of).collect(Collectors.toList());
        } catch (ProductNotFoundException e) {
            throw new ProductNotFoundException(e.getMessage());
        } catch (NotFoundException e) {
            throw new NotFoundException(e.getMessage());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException("Error creando los detalles de venta", e.getCause());
        }
    }

    private void updateSaleTotalPrice(Sale sale, List<SaleDetailsProduct> saleDetailsProductList) {
        // Calcula el subtotal sumando todos los precios, ya contiene la lista de detalles total de la venta
        double newSubtotal = saleDetailsProductList.stream()
                .mapToDouble(SaleDetailsProduct::getTotalPriceDetail)
                .sum();

        sale.setSubtotal(newSubtotal);
        sale.setTotal(newSubtotal);

        saleRepository.save(sale);
    }

    private static SaleDetailsProduct getSaleDetailsProduct(SaleDetailsProductRequestDto item, Product product, Sale sale) {
        RoleProduct roleAlmacen = RoleProduct.Almacen;

        /* Buscar si el producto ya existe en los detalles de la venta */
        SaleDetailsProduct existingDetail = sale.getSaleDetailsProducts().stream()
                .filter(detail -> detail.getProduct().getName().equals(product.getName()))
                .findFirst()
                .orElse(null);

        /* Si ya existe, actualizar la cantidad y el precio */
        if (existingDetail != null) {
            double unitPrice = item.getUnitPrice() != null ? item.getUnitPrice() : existingDetail.getUnitPrice();
            boolean isSamePrice = existingDetail.getUnitPrice().equals(unitPrice);

            if (isSamePrice) {
                // Actualizar la cantidad
                double newQuantity = existingDetail.getQuantity() + item.getQuantity();

                /* Validación de stock para productos de almacen */
                if (roleAlmacen.equals(product.getRoleProduct())) {
                    if (newQuantity > product.getStock()) {
                        throw new IllegalArgumentException("No hay suficiente stock del producto");
                    }

                    // Actualizar el stock del producto
                    product.setStock((int) ((product.getStock() + existingDetail.getQuantity()) - newQuantity));
                }

                /* Actualizar la descripcion, cantidad y precio total del detalle existente */
                existingDetail.setDescription(item.getDescription());
                existingDetail.setQuantity(newQuantity);
                existingDetail.setTotalPriceDetail(existingDetail.getQuantity() * existingDetail.getUnitPrice());
                return existingDetail;
            }
        }

        /* Si el producto es nuevo en la venta, crear un nuevo detalle */
        SaleDetailsProduct saleDetailsProduct = new SaleDetailsProduct();

        /* Si el producto es carne o verdura se establece el precio que viene de la solicitud, sino se establece el
        mismo precio unitario del producto */
        if (!roleAlmacen.equals(product.getRoleProduct())) {
            if (item.getUnitPrice() == null) {
                throw new IllegalArgumentException("El precio unitario es requerido para los productos de carnicería y verdulería");
            }
            if (item.getUnitMeasure() == null) {
                throw new IllegalArgumentException("La unidad de medida es requerida para los productos de carnicería y verdulería");
            }
            saleDetailsProduct.setUnitMeasure(item.getUnitMeasure());
            saleDetailsProduct.setUnitPrice(item.getUnitPrice());
        } else {
            saleDetailsProduct.setUnitPrice(product.getPrice());
        }

        /* Validación y seteo de stock para productos de almacen */
        if (roleAlmacen.equals(product.getRoleProduct())) {
            if (item.getQuantity() > product.getStock()) {
                throw new IllegalArgumentException("No hay suficiente stock del producto");
            } else {
                product.setStock((int) (product.getStock() - item.getQuantity()));
            }
        }

        saleDetailsProduct.setQuantity(item.getQuantity());
        saleDetailsProduct.setTotalPriceDetail(saleDetailsProduct.getQuantity() * saleDetailsProduct.getUnitPrice());
        saleDetailsProduct.setProduct(product);
        saleDetailsProduct.setSale(sale);
        saleDetailsProduct.setDescription(item.getDescription());

        return saleDetailsProduct;
    }


    @Override
    public void deleteDetails(Long idDetails) {
        try {
            SaleDetailsProduct saleDetailsProduct = getDetailsById(idDetails);
            Product product = saleDetailsProduct.getProduct();
            Sale sale = saleDetailsProduct.getSale();
            saleDetailsProductRepository.deleteById(idDetails);

            // Actualizar el precio total de la venta
            Double total = sale.getTotal() - saleDetailsProduct.getTotalPriceDetail();
            sale.setSubtotal(total);
            sale.setTotal(total);
            saleRepository.save(sale);

            // Actualizar stock de productos si es de categoría Almacen
            if (product.getRoleProduct().toString().equals("Almacen")) {
                product.setStock((int) (product.getStock() + saleDetailsProduct.getQuantity()));
                productsRepository.save(product);
            }

        } catch (NotFoundException e) {
            throw new NotFoundException(e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException("Error deleted details", e.getCause());
        }
    }

    /**
     * Método para editar los detalles de una venta, permite editar los atributos de precio unitario y unida de medida
     * solo si los productos son carnes o verduras.
     *
     * @param idDetails                    id de los detalles de la venta a editar
     * @param saleDetailsProductRequestDto objeto con los nuevos datos a editar
     * @throws ProductNotFoundException si el producto no existe o no se encuentra
     * @throws RuntimeException         si ocurre un error al editar los detalles
     */
    @Override
    @Transactional
    public void editDetails(Long idDetails, SaleDetailsProductRequestDto saleDetailsProductRequestDto) {
        try {
            SaleDetailsProduct saleDetailsProduct = getDetailsById(idDetails);

            Product product = saleDetailsProduct.getProduct();
            String productName = product.getName();

            if ("verduleria".equals(productName) || "carne".equals(productName)) {
                if (saleDetailsProductRequestDto.getUnitPrice() != null) {
                    saleDetailsProduct.setUnitPrice(saleDetailsProductRequestDto.getUnitPrice());
                }
                if (saleDetailsProductRequestDto.getUnitMeasure() != null) {
                    saleDetailsProduct.setUnitMeasure(saleDetailsProductRequestDto.getUnitMeasure());
                }
            } else {
                if (saleDetailsProductRequestDto.getUnitPrice() != null || saleDetailsProductRequestDto.getUnitMeasure() != null) {
                    throw new IllegalArgumentException("Se requiere el precio unitario, la descripcion y la unidad de medida para este producto");
                }
            }

            if (saleDetailsProductRequestDto.getQuantity() != null) {
                // Actualizar stock de productos de categoria Almacen
                if (product.getRoleProduct().toString().equals("Almacen")) {
                    if (saleDetailsProductRequestDto.getQuantity() < saleDetailsProduct.getQuantity()) {
                        product.setStock((int) (product.getStock() + (saleDetailsProduct.getQuantity() - saleDetailsProductRequestDto.getQuantity())));
                    } else if (saleDetailsProductRequestDto.getQuantity() > saleDetailsProduct.getQuantity()) {
                        if (saleDetailsProductRequestDto.getQuantity() > product.getStock()) {
                            throw new IllegalArgumentException("No hay suficiente stock del producto");
                        }
                        product.setStock((int) (product.getStock() - (saleDetailsProductRequestDto.getQuantity() - saleDetailsProduct.getQuantity())));
                    }
                    productsRepository.save(product);
                }
                saleDetailsProduct.setQuantity(saleDetailsProductRequestDto.getQuantity());
                saleDetailsProduct.setTotalPriceDetail(saleDetailsProduct.getQuantity() * saleDetailsProduct.getUnitPrice());
            }
            if (saleDetailsProductRequestDto.getDescription() != null) {
                saleDetailsProduct.setDescription(saleDetailsProductRequestDto.getDescription());
            }
            saleDetailsProductRepository.save(saleDetailsProduct);

            // Actualizar el precio total de la venta
            Sale sale = saleDetailsProduct.getSale();
            List<SaleDetailsProduct> saleDetailsProductList = sale.getSaleDetailsProducts();
            updateSaleTotalPrice(sale, saleDetailsProductList);

        } catch (ProductNotFoundException e) {
            throw new ProductNotFoundException(e.getMessage());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException(e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException("Error edit details. Check that there are no empty fields");
        }
    }
}
