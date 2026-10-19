package com.uniloop.controller;

import com.uniloop.dto.ItemResponse;
import com.uniloop.repository.ItemRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

@RestController
@RequestMapping("/api/items")
public class ApiItemController {

    private static final Logger LOGGER = Logger.getLogger(ApiItemController.class.getName());
    private final ItemRepository itemRepository = new ItemRepository();

    @GetMapping
    public ResponseEntity<?> getAvailableItems() {
        try {
            List<ItemResponse> items = itemRepository.findAvailableItemsForApi();
            return ResponseEntity.ok(items);
        } catch (SQLException exception) {
            LOGGER.log(Level.SEVERE, "Could not load available items for API", exception);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Unable to retrieve items."));
        }
    }
}
