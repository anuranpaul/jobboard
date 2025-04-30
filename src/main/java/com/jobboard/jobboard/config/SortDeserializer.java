package com.jobboard.jobboard.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.domain.Sort;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SortDeserializer extends JsonDeserializer<Sort> {
    @Override
    public Sort deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        ObjectMapper mapper = (ObjectMapper) p.getCodec();
        JsonNode node = mapper.readTree(p);
        
        // If empty or no orders, return empty sort
        if (node.isEmpty() || !node.has("orders")) {
            return Sort.unsorted();
        }
        
        JsonNode ordersNode = node.get("orders");
        if (ordersNode.isEmpty()) {
            return Sort.unsorted();
        }
        
        List<Sort.Order> orders = new ArrayList<>();
        for (JsonNode orderNode : ordersNode) {
            String property = orderNode.get("property").asText();
            Sort.Direction direction = Sort.Direction.valueOf(orderNode.get("direction").asText());
            orders.add(new Sort.Order(direction, property));
        }
        
        return Sort.by(orders);
    }
} 