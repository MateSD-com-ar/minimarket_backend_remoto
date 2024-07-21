package com.virgen_lourdes.minimarket.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class UserData {
    public List<Map<String, Object>> jsonData() throws JsonProcessingException {
        String json="["+
                "{\"username\": \"rafaelmarengo\", \"password\": \"hashed_password_1\"," +
                "{\"username\": \"joserojas\", \"password\": \"hashed_password_2\"," +
                "{\"username\": \"facundofunes\", \"password\": \"hashed_password_3\"," +
                "]"
                ;
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(json, List.class);
    }
}
