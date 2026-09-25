package com.uniloop.controller;

import com.uniloop.model.Item;
import com.uniloop.repository.ItemRepository;
import com.uniloop.service.ItemService;
import com.uniloop.service.RequestService;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class UniLoopController {

    private final ItemRepository itemRepository = new ItemRepository();

    private final ItemService itemService = new ItemService();

    private final RequestService requestService = new RequestService();


    @GetMapping("/")
    public String home() {

        return "forward:/index.html";
    }


    @GetMapping("/items")
    public String getItems(Model model) {

        List<Item> items = itemRepository.getAllItems();

        model.addAttribute("items", items);

        return "items";
    }


    @PostMapping("/sell")
    public String sellItem(
            @RequestParam int sellerId,
            @RequestParam int categoryId,
            @RequestParam String itemName,
            @RequestParam String description,
            @RequestParam double price,
            @RequestParam(required = false) String imagePath,
            @RequestParam String condition,
            @RequestParam String listingType) {

        itemService.addItem(
                sellerId,
                categoryId,
                itemName,
                description,
                price,
                imagePath,
                condition,
                listingType
        );

        return "redirect:/items";
    }


    @PostMapping("/request")
    public String requestItem(
            @RequestParam int itemId,
            @RequestParam int buyerId) {

        requestService.requestItem(itemId, buyerId);

        return "redirect:/items";
    }


    @GetMapping("/requests")
    public String viewRequests(Model model) {

        model.addAttribute(
                "requests",
                requestService.getRequestsForSeller(1)
        );

        return "requests";
    }


    @PostMapping("/accept-request")
    public String acceptRequest(
            @RequestParam int requestId) {

        requestService.updateRequestStatus(
                requestId,
                "ACCEPTED"
        );

        return "redirect:/requests";
    }


    @PostMapping("/reject-request")
    public String rejectRequest(
            @RequestParam int requestId) {

        requestService.updateRequestStatus(
                requestId,
                "REJECTED"
        );

        return "redirect:/requests";
    }
}