package com.virgen_lourdes.minimarket.utils;

import com.virgen_lourdes.minimarket.entity.Product;
import com.virgen_lourdes.minimarket.entity.User;
import com.virgen_lourdes.minimarket.entity.enums.Role;
import com.virgen_lourdes.minimarket.entity.enums.RoleProduct;
import com.virgen_lourdes.minimarket.repository.IProductsRepository;
import com.virgen_lourdes.minimarket.repository.IUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class DataLoader implements CommandLineRunner {

    @Autowired
    private IUserRepository userRepository;

    @Autowired
    private IProductsRepository productsRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    UserData userData;

    @Override
    public void run(String... args) throws Exception {
        loadData();
    }

    private void loadData() {
        if (userRepository.count()==0){
            User user1 = new User();
            user1.setName("Rafael Marengo");
            user1.setUsername("rafaelmarengo");
            user1.setPassword(passwordEncoder.encode("328459251987"));
            user1.setRole(Role.ADMIN);
            user1.setIsActive(true);
            user1.setCreatedAt(LocalDateTime.now());
            user1.setUpdatedAt(LocalDateTime.now());
            userRepository.save(user1);
            User user2 = new User();
            user2.setName("José Rojas");
            user2.setUsername("joserojas");
            user2.setPassword(passwordEncoder.encode("298743901983"));
            user2.setRole(Role.EMPLOYEE);
            user2.setIsActive(true);
            user2.setCreatedAt(LocalDateTime.now());
            user2.setUpdatedAt(LocalDateTime.now());
            userRepository.save(user2);
            User user3 = new User();
            user3.setName("Facundo Funes");
            user3.setUsername("facundofunes");
            user3.setPassword(passwordEncoder.encode("366285681992"));
            user3.setRole(Role.EMPLOYEE);
            user3.setIsActive(true);
            user3.setCreatedAt(LocalDateTime.now());
            user3.setUpdatedAt(LocalDateTime.now());
            userRepository.save(user3);
        }
        if(productsRepository.count()==0){
            Product product1 = new Product();
            product1.setName("carne");
            product1.setBrand("");
            product1.setCode("");
            product1.setPrice(0.0);
            product1.setRoleProduct(RoleProduct.Carniceria);
            product1.setUnitMeasure("");
            product1.setStock(0);
            productsRepository.save(product1);
            Product product2 = new Product();
            product2.setName("verduleria");
            product2.setBrand("");
            product2.setCode("");
            product2.setPrice(0.0);
            product2.setRoleProduct(RoleProduct.Verduleria);
            product2.setUnitMeasure("");
            product2.setStock(0);
            productsRepository.save(product2);
        }
    }
}
