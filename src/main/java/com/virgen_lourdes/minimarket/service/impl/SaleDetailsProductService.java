package com.virgen_lourdes.minimarket.service.impl;

import com.virgen_lourdes.minimarket.dto.requestDto.SaleDetailsProductRequestDto;
import com.virgen_lourdes.minimarket.entity.Product;
import com.virgen_lourdes.minimarket.entity.Sale;
import com.virgen_lourdes.minimarket.entity.SaleDetailsProduct;
import com.virgen_lourdes.minimarket.exceptions.customExceptions.NotFoundException;
import com.virgen_lourdes.minimarket.exceptions.customExceptions.ProductNotFoundException;
import com.virgen_lourdes.minimarket.repository.IProductsRepository;
import com.virgen_lourdes.minimarket.repository.ISaleDetailsProductRepository;
import com.virgen_lourdes.minimarket.repository.ISaleRepository;
import com.virgen_lourdes.minimarket.service.ISaleDetailsProductService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.query.JSqlParserUtils;
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
                .orElseThrow(() -> new RuntimeException("Details not found"));
    }

    @Override
    public List<SaleDetailsProduct> createDetails(List<SaleDetailsProductRequestDto> saleDetailsProductRequestDto, Sale sale) {
        return saleDetailsProductRequestDto.stream().map(item -> {
            Product product = productsRepository.findById(item.getProduct())
                    .orElseThrow(() -> new ProductNotFoundException("Product does not exist or product not found"));
            SaleDetailsProduct saleDetailsProduct = new SaleDetailsProduct();

            // Si el producto es carne o verdura se establece el precio que viene de la solictud, sino se establece el
            // mismo precio del producto
            System.out.println("NOMBRE DEL PRODUCTO: " + product.getName());
            if (product.getName().equals("carne") || product.getName().equals("verduleria")) {
                saleDetailsProduct.setUnitMeasure(item.getUnitMeasure());
                saleDetailsProduct.setUnitPrice(item.getUnitPrice());
            } else {
                saleDetailsProduct.setUnitPrice(product.getPrice());
            }

            saleDetailsProduct.setQuantity(item.getQuantity());
            saleDetailsProduct.setTotalPriceDetail(saleDetailsProduct.getQuantity() * saleDetailsProduct.getUnitPrice());
            saleDetailsProduct.setProduct(product);
            saleDetailsProduct.setSale(sale);
            saleDetailsProductRepository.save(saleDetailsProduct);

            return saleDetailsProduct;
        }).collect(Collectors.toList());
    }

    @Override
    public void deleteDetails(Long idDetails) {
        try {
            saleDetailsProductRepository.findById(idDetails).
                    orElseThrow(() -> new NotFoundException("Details not found"));
            saleDetailsProductRepository.deleteById(idDetails);
        } catch (NotFoundException e) {
            throw new NotFoundException(e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException("Error deleted details");
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
            if (saleDetailsProductRequestDto.getProduct() != null) {
                saleDetailsProduct.setProduct(productsRepository.findById(saleDetailsProductRequestDto.getProduct())
                        .orElseThrow(() -> new ProductNotFoundException("Product does not exist or product not found")));
            }

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
                    throw new RuntimeException("Unit price or measure cannot be modified for this product");
                }
            }

            if (saleDetailsProductRequestDto.getQuantity() != null) {
                saleDetailsProduct.setQuantity(saleDetailsProductRequestDto.getQuantity());
                saleDetailsProduct.setTotalPriceDetail(saleDetailsProduct.getQuantity() * saleDetailsProduct.getUnitPrice());
            }
            saleDetailsProductRepository.save(saleDetailsProduct);
        } catch (ProductNotFoundException e) {
            throw new ProductNotFoundException(e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException("Error edit details. Check that there are no empty fields");
        }
    }
}
