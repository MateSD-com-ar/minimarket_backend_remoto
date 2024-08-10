package com.virgen_lourdes.minimarket.service.impl;

import com.virgen_lourdes.minimarket.dto.requestDto.SaleDetailsProductRequestDto;
import com.virgen_lourdes.minimarket.entity.SaleDetailsProduct;
import com.virgen_lourdes.minimarket.repository.IProductsRepository;
import com.virgen_lourdes.minimarket.repository.ISaleDetailsProductRepository;
import com.virgen_lourdes.minimarket.repository.ISaleRepository;
import com.virgen_lourdes.minimarket.service.ISaleDetailsProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

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
    public void createDetails(SaleDetailsProductRequestDto saleDetailsProductRequestDto) {
        try{
            SaleDetailsProduct saleDetailsProduct = new SaleDetailsProduct();
            saleDetailsProduct.setAmount(saleDetailsProductRequestDto.getAmount());
            saleDetailsProduct.setTotalPriceDetail(saleDetailsProductRequestDto.getTotalPriceDetail());
            saleDetailsProduct.setUnitPrice(saleDetailsProductRequestDto.getUnitPrice());
            saleDetailsProduct.setProduct(productsRepository.findById(saleDetailsProductRequestDto.getProduct()).orElseThrow(()->new RuntimeException("Product does not exist or product not found")));
            saleDetailsProduct.setSale(saleRepository.findById(saleDetailsProductRequestDto.getSale()).orElseThrow(()->new RuntimeException("Sale does not exist or sale not found")));
            saleDetailsProductRepository.save(saleDetailsProduct);
        } catch (Exception e){
            throw new RuntimeException("Error creating Details. Check that there are no empty fields");
        }
    }

    @Override
    public void deleteDetails(Long idDetails) {
        try{
            saleDetailsProductRepository.deleteById(idDetails);
        } catch (Exception e){
            throw new RuntimeException("Error deleted details");
        }
    }

    @Override
    public void editDetails(Long idDetails, SaleDetailsProductRequestDto saleDetailsProductRequestDto) {
        try{
            SaleDetailsProduct saleDetailsProduct = saleDetailsProductRepository.findById(idDetails).orElseThrow(() -> new RuntimeException("Details not found"));
            if (saleDetailsProductRequestDto.getAmount()!=null){
                saleDetailsProduct.setAmount(saleDetailsProductRequestDto.getAmount());
            }
            if (saleDetailsProductRequestDto.getTotalPriceDetail()!=null){
                saleDetailsProduct.setTotalPriceDetail(saleDetailsProductRequestDto.getTotalPriceDetail());
            }
            if (saleDetailsProductRequestDto.getUnitPrice()!=null){
                saleDetailsProduct.setUnitPrice(saleDetailsProductRequestDto.getUnitPrice());
            }
            if (saleDetailsProductRequestDto.getProduct()!=null){
                saleDetailsProduct.setProduct(productsRepository.findById(saleDetailsProductRequestDto.getProduct()).orElse(null));
            }
            if (saleDetailsProductRequestDto.getSale()!=null){
                saleDetailsProduct.setSale(saleRepository.findById(saleDetailsProductRequestDto.getSale()).orElse(null));
            }
            saleDetailsProductRepository.save(saleDetailsProduct);
        } catch (Exception e){
            throw new RuntimeException("Error edit details. Check that there are no empty fields");
        }
    }
}
