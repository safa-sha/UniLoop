package com.uniloop.controller;

import com.uniloop.model.Request;
import com.uniloop.model.Item;
import com.uniloop.repository.ItemRepository;
import com.uniloop.service.ItemService;
import com.uniloop.service.RequestService;
import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

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
            Model model,
            HttpSession session) {

        Integer buyerId = authenticatedUserId(session);
        List<Item> items;

        if (category != null) {
            items =
                    itemRepository.getItemsByCategory(category, buyerId);
        } else {
            items =
                    itemRepository.getAllItems(buyerId);
        }

        if (buyerId != null) for (Item item : items) {

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
            @RequestParam(name = "itemId", required = false) Integer itemId,

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
            String listingType,
            HttpSession session) {

        Integer sellerId = authenticatedUserId(session);
        if (sellerId == null) return loginRedirect(itemId == null ? "/sell" : "/list-item?id=" + itemId + "&edit=true");
        if (itemId != null) {
            Item existingItem = itemRepository.getItemById(itemId);
            if (existingItem == null || existingItem.getSellerId() != sellerId) {
                return "redirect:/sell";
            }
        }

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

        if (itemId != null) {
            itemRepository.updateItem(itemId, sellerId, categoryId, itemName, description,
                    price, imagePath, condition, listingType);
            return "redirect:/item?id=" + itemId;
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

            @RequestParam(
                    name = "source",
                    defaultValue = "items"
            )
            String source,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        Integer buyerId = authenticatedUserId(session);
        if (buyerId == null) return loginRedirect(destinationForSource(source, itemId));

        Item requestedItem = itemRepository.getItemById(itemId);
        if (requestedItem != null && requestedItem.getSellerId() == buyerId) {
            redirectAttributes.addFlashAttribute("requestError", "You cannot request your own listing.");
            return "redirect:/item?id=" + itemId;
        }

        requestService.requestItem(
                itemId,
                buyerId
        );

        if (source.equals("item")) {
    return "redirect:/item?id=" + itemId;
}

if (source.equals("my-requests")) {
    return "redirect:/my-requests";
}

return "redirect:/items#item-" + itemId;
    }


    @GetMapping("/requests")
    public String viewRequests(Model model, HttpSession session) {

        Integer sellerId = authenticatedUserId(session);
        if (sellerId == null) return loginRedirect("/requests");

        model.addAttribute(
                "requestGroups",
                requestService.getRequestGroupsForSeller(sellerId)
        );

        model.addAttribute(
                "historyGroups",
                requestService.getRequestHistoryGroupsForSeller(sellerId)
        );

        return "requests";
    }
    
    @GetMapping("/my-requests")
public String myRequests(Model model, HttpSession session) {

    Integer buyerId = authenticatedUserId(session);
    if (buyerId == null) return loginRedirect("/my-requests");

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
            int requestId,
            HttpSession session) {

        Integer sellerId = authenticatedUserId(session);
        if (sellerId == null) return loginRedirect("/requests");

        requestService.updateRequestStatus(
                requestId,
                "ACCEPTED",
                sellerId
        );

        return "redirect:/requests";
    }


    @PostMapping("/reject-request")
    public String rejectRequest(
            @RequestParam(name = "requestId")
            int requestId,
            HttpSession session) {

        Integer sellerId = authenticatedUserId(session);
        if (sellerId == null) return loginRedirect("/requests");

        requestService.updateRequestStatus(
                requestId,
                "REJECTED",
                sellerId
        );

        return "redirect:/requests";
    }


    @PostMapping("/cancel-request")
    public String cancelRequest(
            @RequestParam(name = "requestId")
            int requestId,

            @RequestParam(
                    name = "source",
                    defaultValue = "items"
            )
            String source,
            HttpSession session) {

        Integer buyerId = authenticatedUserId(session);
        int itemId = requestService.getItemIdByRequestId(requestId);
        if (buyerId == null) return loginRedirect(destinationForSource(source, itemId));

        requestService.cancelRequest(
                requestId,
                buyerId
        );

        if (source.equals("item")) {
    return "redirect:/item?id=" + itemId;
}

if (source.equals("my-requests")) {
    return "redirect:/my-requests";
}

return "redirect:/items#item-" + itemId;
    }


    @GetMapping("/sell")
public String sellPage(Model model, HttpSession session) {

    Integer sellerId = authenticatedUserId(session);
    if (sellerId == null) return loginRedirect("/sell");

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
        @RequestParam(name = "edit", defaultValue = "false") boolean edit,
        Model model,
        HttpSession session) {

    if (authenticatedUserId(session) == null) return loginRedirect(id == null ? "/list-item" : "/list-item?id=" + id + (edit ? "&edit=true" : ""));

    if (id != null) {

        Item item =
                itemRepository.getItemById(id);

        if (item != null && item.getSellerId() == authenticatedUserId(session)) {
            model.addAttribute("item", item);
            model.addAttribute("listAgain", true);
            model.addAttribute("editing", edit);
        } else return "redirect:/sell";
    }

    return "list-item";
}

    @GetMapping("/item")
    public String itemDetails(
            @RequestParam(name = "id")
            int id,

            Model model,
            HttpSession session) {

        Item item =
                itemRepository.getItemById(id);

        if (item == null) {
            return "redirect:/items";
        }

        Integer buyerId = authenticatedUserId(session);

        Request request = buyerId == null ? null :
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

        boolean isSeller = buyerId != null && item.getSellerId() == buyerId;
        model.addAttribute("isSeller", isSeller);
        if (isSeller) {
            model.addAttribute("itemRequests", requestService.getActiveRequestsForItem(id, buyerId));
        }

        return "item-details";
    }
    @PostMapping("/delete-listing")
public String deleteListing(
        @RequestParam(name = "itemId") int itemId,
        HttpSession session) {

    Integer sellerId = authenticatedUserId(session);
    if (sellerId == null) return loginRedirect("/sell");

    itemRepository.markItemUnavailable(itemId, sellerId);

    return "redirect:/sell";
}

    private Integer authenticatedUserId(HttpSession session) {
        Object userId = session.getAttribute("authenticatedUserId");
        return userId instanceof Number ? ((Number) userId).intValue() : null;
    }

    private String loginRedirect(String returnTo) {
        return "redirect:/login?required=true&returnTo="
                + URLEncoder.encode(returnTo, StandardCharsets.UTF_8);
    }

    private String destinationForSource(String source, int itemId) {
        if ("item".equals(source) && itemId > 0) return "/item?id=" + itemId;
        if ("my-requests".equals(source)) return "/my-requests";
        return itemId > 0 ? "/items#item-" + itemId : "/items";
    }
}
