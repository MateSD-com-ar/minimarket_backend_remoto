package com.virgen_lourdes.minimarket.service.impl;

import com.virgen_lourdes.minimarket.dto.requestDto.SaleDetailsProductRequestDto;
import com.virgen_lourdes.minimarket.dto.responseDto.SaleDetailsProductResponseDto;
import com.virgen_lourdes.minimarket.entity.Product;
import com.virgen_lourdes.minimarket.entity.Sale;
import com.virgen_lourdes.minimarket.entity.SaleDetailsProduct;
import com.virgen_lourdes.minimarket.entity.enums.PaymentStatus;
import com.virgen_lourdes.minimarket.exceptions.customExceptions.NotFoundException;
import com.virgen_lourdes.minimarket.exceptions.customExceptions.ProductNotFoundException;
import com.virgen_lourdes.minimarket.repository.IProductsRepository;
import com.virgen_lourdes.minimarket.repository.ISaleDetailsProductRepository;
import com.virgen_lourdes.minimarket.repository.ISaleRepository;
import com.virgen_lourdes.minimarket.service.ISaleDetailsProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

            // Actualizar el precio total de la venta
            updateSaleTotalPrice(saleDetailsProductList.get(0).getSale());

            // Actualizar el stock de productos
            updateProductStock(saleDetailsProductList.get(0).getSale());

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

    private void updateSaleTotalPrice(Sale sale) {
        // Calcula el subtotal sumando los precios totales de los detalles de la venta
        double subtotal = sale.getSaleDetailsProducts().stream()
                .mapToDouble(SaleDetailsProduct::getTotalPriceDetail)
                .sum();
        sale.setSubtotal(subtotal);

        // Maneja el interés y el descuento, asegurando que no sean nulos
        double interest = sale.getInterest() != null ? sale.getInterest() : 0.0;
        double discount = sale.getDiscount() != null ? sale.getDiscount() : 0.0;

        // Calcula el total de la venta
        double total = subtotal + interest - discount;
        sale.setTotal(total);

        saleRepository.save(sale);
    }

    private static SaleDetailsProduct getSaleDetailsProduct(SaleDetailsProductRequestDto item, Product product, Sale sale) {
        SaleDetailsProduct saleDetailsProduct = new SaleDetailsProduct();
        /* Si el producto es carne o verdura se establece el precio que viene de la solicitud, sino se establece el
        mismo precio unitario del producto */
        if (product.getName().equals("carne") || product.getName().equals("verduleria")) {
            if (item.getUnitPrice() == null) {
                throw new IllegalArgumentException("El precio unitario es requerido para los productos de carnicería y verdulería");
            }
            if (item.getUnitMeasure() == null) {
                throw new IllegalArgumentException("La unidad de medida es requerido para los productos de carnicería y verdulería");
            }
            saleDetailsProduct.setUnitMeasure(item.getUnitMeasure());
            saleDetailsProduct.setUnitPrice(item.getUnitPrice());
        } else {
            saleDetailsProduct.setUnitPrice(product.getPrice());
        }

        /* Se valida que la cantidad solicitada no sea mayor al stock del producto */
        if (product.getRoleProduct().toString().equals("Almacen")) {
            if (item.getQuantity() > product.getStock()) {
                System.out.println(product.getStock());
                System.out.println(item.getQuantity());
                throw new IllegalArgumentException("No hay suficiente stock del producto");
            }
        }

        saleDetailsProduct.setQuantity(item.getQuantity());
        saleDetailsProduct.setTotalPriceDetail(saleDetailsProduct.getQuantity() * saleDetailsProduct.getUnitPrice());
        saleDetailsProduct.setProduct(product);
        saleDetailsProduct.setSale(sale);
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
            updateSaleTotalPrice(sale);

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
                    product.setUnitMeasure(saleDetailsProductRequestDto.getUnitMeasure());
                }
            } else {
                if (saleDetailsProductRequestDto.getUnitPrice() != null || saleDetailsProductRequestDto.getUnitMeasure() != null) {
                    throw new IllegalArgumentException("Se requiere el precio unitario y la unidad de medida para este producto");
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
            saleDetailsProductRepository.save(saleDetailsProduct);

            // Actualizar el precio total de la venta
            updateSaleTotalPrice(saleDetailsProduct.getSale());

        } catch (ProductNotFoundException e) {
            throw new ProductNotFoundException(e.getMessage());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException(e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException("Error edit details. Check that there are no empty fields");
        }
    }

    /**
     * Método para actualizar el stock de los productos cuando se crea un detalle de venta, resta la cantidad solicitada
     * del stock del producto
     *
     * @param sale venta a actualizar
     */
    public void updateProductStock(Sale sale) {
        // Obtiene los detalles de la venta
        List<SaleDetailsProduct> saleDetailsProducts = sale.getSaleDetailsProducts();

        for (SaleDetailsProduct saleDetailsProduct : saleDetailsProducts) {
            Product product = saleDetailsProduct.getProduct();

            // Verifica si el producto pertenece a la categoría 'Almacen'
            if ("Almacen".equalsIgnoreCase(product.getRoleProduct().toString())) {
                int currentStock = product.getStock();
                double quantity = saleDetailsProduct.getQuantity();
                currentStock -= (int) quantity;

                // Establece el nuevo stock y guarda los cambios en el producto
                product.setStock(currentStock);
                productsRepository.save(product);
            }
        }
    }
}
