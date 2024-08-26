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
        SaleDetailsProduct saleDetailsProduct = saleDetailsProductRepository.findById(idDetails).orElseThrow(() -> new RuntimeException("Details not found"));
        return saleDetailsProduct;
    }

    @Override
    public List<SaleDetailsProduct> createDetails(List<SaleDetailsProductRequestDto> saleDetailsProductRequestDto, Sale sale) {
        return saleDetailsProductRequestDto.stream().map(item -> {
            Product product = productsRepository.findById(item.getProduct())
                    .orElseThrow(() -> new ProductNotFoundException("Product does not exist or product not found"));
            SaleDetailsProduct saleDetailsProduct = new SaleDetailsProduct();
            saleDetailsProduct.setQuantity(item.getQuantity());
            saleDetailsProduct.setUnitPrice(product.getPrice());
            saleDetailsProduct.setTotalPriceDetail(saleDetailsProduct.getQuantity() * saleDetailsProduct.getUnitPrice());
            saleDetailsProduct.setProduct(product);
            saleDetailsProduct.setSale(sale);

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

    @Override
    public void editDetails(Long idDetails, SaleDetailsProductRequestDto saleDetailsProductRequestDto) {
        try {
            SaleDetailsProduct saleDetailsProduct = saleDetailsProductRepository.findById(idDetails)
                    .orElseThrow(() -> new RuntimeException("Details not found"));
            if (saleDetailsProductRequestDto.getProduct() != null) {
                saleDetailsProduct.setProduct(productsRepository.findById(saleDetailsProductRequestDto.getProduct())
                        .orElseThrow(() -> new ProductNotFoundException("Product does not exist or product not found")));
            }
            if (saleDetailsProductRequestDto.getUnitPrice() != null) {
                saleDetailsProduct.setUnitPrice(saleDetailsProduct.getProduct().getPrice());
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
