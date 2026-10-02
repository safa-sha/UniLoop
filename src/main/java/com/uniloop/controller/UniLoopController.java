package com.uniloop.controller;

import com.uniloop.model.Request;
import com.uniloop.model.Item;
import com.uniloop.repository.ItemRepository;
import com.uniloop.service.ItemService;
import com.uniloop.service.RequestService;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Controller
public class UniLoopController {

    private final ItemRepository itemRepository =
            new ItemRepository();

    private final ItemService itemService =
            new ItemService();

    private final RequestService requestService =
            new RequestService();


    @GetMapping("/")
    public String home(Model model) {

        model.addAttribute(
                "latestItems",
                itemRepository.getLatestItems()
        );

        return "index";
    }


    @GetMapping("/items")
    public String getItems(
            @RequestParam(name = "category", required = false)
            Integer category,
            Model model) {

        List<Item> items;

        if (category != null) {
            items =
                    itemRepository.getItemsByCategory(category);
        } else {
            items =
                    itemRepository.getAllItems();
        }

        int buyerId = 2;

        for (Item item : items) {

            Request request =
                    requestService.getLatestRequest(
                            item.getItemId(),
                            buyerId
                    );

            if (request != null) {

                item.setRequestId(
                        request.getRequestId()
                );

                item.setRequestStatus(
                        request.getRequestStatus()
                );
            }
        }

        model.addAttribute(
                "items",
                items
        );

        return "items";
    }


    @PostMapping("/sell")
    public String sellItem(
            @RequestParam(name = "sellerId")
            int sellerId,

            @RequestParam(name = "categoryId")
            int categoryId,

            @RequestParam(name = "itemName")
            String itemName,

            @RequestParam(name = "description")
            String description,

            @RequestParam(name = "price", defaultValue = "0")
            double price,

            @RequestParam(
                    name = "image",
                    required = false
            )
            MultipartFile image,

            @RequestParam(name = "condition")
            String condition,

            @RequestParam(name = "listingType")
            String listingType) {

        String imagePath = null;

        // Handle image upload
        if (image != null && !image.isEmpty()) {

            try {

                String uploadDirectory =
                        "src/main/resources/static/uploads";

                Files.createDirectories(
                        Paths.get(uploadDirectory)
                );

                String originalName =
                        image.getOriginalFilename();

                String fileExtension = "";

                if (originalName != null
                        && originalName.contains(".")) {

                    fileExtension =
                            originalName.substring(
                                    originalName.lastIndexOf(".")
                            );
                }

                String fileName =
                        UUID.randomUUID()
                                + fileExtension;

                Path filePath =
                        Paths.get(
                                uploadDirectory,
                                fileName
                        );

                Files.write(
                        filePath,
                        image.getBytes()
                );

                imagePath =
                        "/uploads/" + fileName;

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

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

        return "redirect:/sell";
    }


    @PostMapping("/request")
    public String requestItem(
            @RequestParam(name = "itemId")
            int itemId,

            @RequestParam(name = "buyerId")
            int buyerId,

            @RequestParam(
                    name = "source",
                    defaultValue = "items"
            )
            String source) {

        requestService.requestItem(
                itemId,
                buyerId
        );

        if (source.equals("item")) {
            return "redirect:/item?id=" + itemId;
        }

        return "redirect:/items";
    }


    @GetMapping("/requests")
    public String viewRequests(Model model) {

        model.addAttribute(
                "requestGroups",
                requestService.getRequestGroupsForSeller(1)
        );

        model.addAttribute(
                "historyGroups",
                requestService.getRequestHistoryGroupsForSeller(1)
        );

        return "requests";
    }
    
    @GetMapping("/my-requests")
public String myRequests(Model model) {

    int buyerId = 2;

    model.addAttribute(
            "myRequests",
            requestService.getRequestsForBuyer(buyerId)
    );

    model.addAttribute(
            "historyGroups",
            requestService.getRequestHistoryGroupsForBuyer(buyerId)
    );

    return "my-requests";
}


    @PostMapping("/accept-request")
    public String acceptRequest(
            @RequestParam(name = "requestId")
            int requestId) {

        requestService.updateRequestStatus(
                requestId,
                "ACCEPTED"
        );

        return "redirect:/requests";
    }


    @PostMapping("/reject-request")
    public String rejectRequest(
            @RequestParam(name = "requestId")
            int requestId) {

        requestService.updateRequestStatus(
                requestId,
                "REJECTED"
        );

        return "redirect:/requests";
    }


    @PostMapping("/cancel-request")
    public String cancelRequest(
            @RequestParam(name = "requestId")
            int requestId,

            @RequestParam(name = "buyerId")
            int buyerId,

            @RequestParam(
                    name = "source",
                    defaultValue = "items"
            )
            String source) {

        int itemId =
                requestService.getItemIdByRequestId(requestId);

        requestService.cancelRequest(
                requestId,
                buyerId
        );

        if (source.equals("item")) {
            return "redirect:/item?id=" + itemId;
        }

        return "redirect:/items";
    }


    @GetMapping("/sell")
public String sellPage(Model model) {

    int sellerId = 1;

    model.addAttribute(
            "myListings",
            itemRepository.getAvailableItemsBySeller(sellerId)
    );

    model.addAttribute(
            "listingHistory",
            itemRepository.getUnavailableItemsBySeller(sellerId)
    );

    return "sell";
}
@GetMapping("/list-item")
public String listItemPage(
        @RequestParam(name = "id", required = false) Integer id,
        Model model) {

    if (id != null) {

        Item item =
                itemRepository.getItemById(id);

        if (item != null) {
            model.addAttribute("item", item);
            model.addAttribute("listAgain", true);
        }
    }

    return "list-item";
}

    @GetMapping("/item")
    public String itemDetails(
            @RequestParam(name = "id")
            int id,

            Model model) {

        Item item =
                itemRepository.getItemById(id);

        if (item == null) {
            return "redirect:/items";
        }

        int buyerId = 2;

        Request request =
                requestService.getLatestRequest(
                        id,
                        buyerId
                );

        model.addAttribute(
                "item",
                item
        );

        model.addAttribute(
                "request",
                request
        );

        return "item-details";
    }
    @PostMapping("/delete-listing")
public String deleteListing(
        @RequestParam(name = "itemId") int itemId) {

    itemRepository.markItemUnavailable(itemId);

    return "redirect:/sell";
}
}