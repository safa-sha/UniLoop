package com.uniloop.controller;

import com.uniloop.model.Request;
import com.uniloop.model.Item;
import com.uniloop.model.User;
import com.uniloop.repository.ItemRepository;
import com.uniloop.repository.UserRepository;
import com.uniloop.service.ItemService;
import com.uniloop.service.RequestService;
import com.uniloop.service.UserService;
import com.uniloop.service.UserService.PasswordChangeResult;
import com.uniloop.service.UserService.ProfileUpdateResult;
import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.sql.SQLException;
import java.time.Year;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

@Controller
public class UniLoopController {

    private static final Logger LOGGER = Logger.getLogger(UniLoopController.class.getName());

    private final ItemRepository itemRepository =
            new ItemRepository();

    private final UserService userService =
            new UserService(new UserRepository());

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

    @GetMapping("/profile")
    public String profile(@RequestParam(name = "edit", defaultValue = "false") boolean edit,
                          Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        Integer userId = authenticatedUserId(session);
        if (userId == null) return loginRedirect("/profile");

        try {
            User user = userService.getUserById(userId);
            if (user == null) {
                redirectAttributes.addFlashAttribute("profileError", "Your account could not be found.");
                return "redirect:/";
            }
            boolean profileComplete = user.getDepartment() != null && !user.getDepartment().isBlank()
                    && user.getAdmissionYear() != null
                    && user.getCourseProgram() != null && !user.getCourseProgram().isBlank();
            model.addAttribute("profileName", user.getName());
            model.addAttribute("profileEmail", user.getEmail());
            model.addAttribute("userDepartment", user.getDepartment());
            model.addAttribute("userAdmissionYear", user.getAdmissionYear());
            model.addAttribute("userCourseProgram", user.getCourseProgram());
            model.addAttribute("userPhone", user.getPhone());
            model.addAttribute("profileComplete", profileComplete);
            model.addAttribute("editProfile", edit || !profileComplete);
            model.addAttribute("profileInitials", profileInitials(user.getName()));
            model.addAttribute("maximumAdmissionYear", Year.now().getValue() + 1);
            return "profile";
        } catch (SQLException exception) {
            LOGGER.log(Level.WARNING, "Could not load profile", exception);
            redirectAttributes.addFlashAttribute("profileError", "We could not load your profile. Please try again.");
            return "redirect:/";
        }
    }

    @PostMapping("/profile")
    public String updateProfile(@RequestParam(name = "phone", required = false) String phone,
                                @RequestParam(name = "department", required = false) String department,
                                @RequestParam(name = "admissionYear", required = false) String admissionYearInput,
                                @RequestParam(name = "courseProgram", required = false) String courseProgram,
                                HttpSession session, RedirectAttributes redirectAttributes) {
        Integer userId = authenticatedUserId(session);
        if (userId == null) return loginRedirect("/profile");

        Integer admissionYear = null;
        boolean invalidAdmissionYear = admissionYearInput == null || admissionYearInput.isBlank();
        if (!invalidAdmissionYear) {
            try {
                admissionYear = Integer.valueOf(admissionYearInput.trim());
            } catch (NumberFormatException exception) {
                invalidAdmissionYear = true;
            }
        }

        ProfileUpdateResult result = invalidAdmissionYear
                ? ProfileUpdateResult.INVALID_ADMISSION_YEAR
                : userService.updateProfile(userId, phone, department, admissionYear, courseProgram);
        if (result == ProfileUpdateResult.SUCCESS) {
            redirectAttributes.addFlashAttribute("profileSuccess", "Your profile was updated.");
        } else {
            redirectAttributes.addFlashAttribute("profileError", profileUpdateMessage(result));
            redirectAttributes.addFlashAttribute("profilePhone", phone);
            redirectAttributes.addFlashAttribute("profileDepartment", department);
            redirectAttributes.addFlashAttribute("profileAdmissionYear", admissionYearInput);
            redirectAttributes.addFlashAttribute("profileCourseProgram", courseProgram);
            return "redirect:/profile?edit=true";
        }
        return "redirect:/profile";
    }

    @GetMapping("/settings")
    public String settings(Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        Integer userId = authenticatedUserId(session);
        if (userId == null) return loginRedirect("/settings");

        try {
            User user = userService.getUserById(userId);
            if (user == null) {
                redirectAttributes.addFlashAttribute("settingsError", "Your account could not be found.");
                return "redirect:/";
            }
            model.addAttribute("userName", user.getName());
            model.addAttribute("userEmail", user.getEmail());
            return "settings";
        } catch (SQLException exception) {
            LOGGER.log(Level.WARNING, "Could not load account settings", exception);
            redirectAttributes.addFlashAttribute("settingsError",
                    "We could not load your account settings. Please try again.");
            return "redirect:/";
        }
    }

    @PostMapping("/settings/password")
    public String changePassword(@RequestParam(name = "currentPassword", required = false) String currentPassword,
                                 @RequestParam(name = "newPassword", required = false) String newPassword,
                                 @RequestParam(name = "confirmPassword", required = false) String confirmPassword,
                                 HttpSession session, RedirectAttributes redirectAttributes) {
        Integer userId = authenticatedUserId(session);
        if (userId == null) return loginRedirect("/settings");

        if (newPassword == null || !newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("settingsError", "The new password and confirmation do not match.");
            return "redirect:/settings";
        }

        PasswordChangeResult result = userService.changePassword(userId, currentPassword, newPassword);
        if (result == PasswordChangeResult.SUCCESS) {
            redirectAttributes.addFlashAttribute("settingsSuccess", "Your password was changed.");
        } else {
            redirectAttributes.addFlashAttribute("settingsError", passwordChangeMessage(result));
        }
        return "redirect:/settings";
    }

    private String profileUpdateMessage(ProfileUpdateResult result) {
        return switch (result) {
            case INVALID_DEPARTMENT -> "Enter a department of up to 100 characters.";
            case INVALID_ADMISSION_YEAR -> "Enter a valid admission year.";
            case INVALID_COURSE_PROGRAM -> "Enter a course or program of up to 150 characters.";
            case INVALID_PHONE -> "Enter a phone number of up to 20 characters.";
            case INVALID_USER_ID, USER_NOT_FOUND -> "Your account could not be found.";
            default -> "We could not update your profile. Please try again.";
        };
    }

    private String passwordChangeMessage(PasswordChangeResult result) {
        return switch (result) {
            case INVALID_CURRENT_PASSWORD -> "The current password is incorrect.";
            case INVALID_NEW_PASSWORD -> "Use a new password with at least 8 characters and no more than 72 UTF-8 bytes.";
            case INVALID_USER_ID, USER_NOT_FOUND -> "Your account could not be found.";
            default -> "We could not change your password. Please try again.";
        };
    }

    private String profileInitials(String name) {
        if (name == null || name.isBlank()) return "U";
        String[] parts = name.trim().split("\\s+");
        if (parts.length == 1) return parts[0].substring(0, 1).toUpperCase();
        return (parts[0].substring(0, 1) + parts[parts.length - 1].substring(0, 1)).toUpperCase();
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
            @RequestParam(name = "relistItemId", required = false) Integer relistItemId,

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
        if (itemId != null && relistItemId != null) return "redirect:/sell";
        if (itemId != null) {
            Item existingItem = itemRepository.getItemById(itemId);
            if (existingItem == null || existingItem.getSellerId() != sellerId
                    || !"AVAILABLE".equals(existingItem.getItemStatus())) {
                return "redirect:/sell";
            }
        }

        Item itemToRelist = null;
        if (relistItemId != null) {
            itemToRelist = itemRepository.getItemById(relistItemId);
            if (itemToRelist == null || itemToRelist.getSellerId() != sellerId
                    || !"UNAVAILABLE".equals(itemToRelist.getItemStatus())) {
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

        // Reuse the prior photo for a relisted copy unless the seller uploaded a replacement.
        if (imagePath == null && itemToRelist != null) {
            imagePath = itemToRelist.getImagePath();
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
            @RequestParam(name = "navigationSource", required = false)
            String navigationSource,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        Integer buyerId = authenticatedUserId(session);
        if (buyerId == null) return loginRedirect(destinationForSource(source, itemId, navigationSource));

        Item requestedItem = itemRepository.getItemById(itemId);
        if (requestedItem != null && requestedItem.getSellerId() == buyerId) {
            redirectAttributes.addFlashAttribute("requestError", "You cannot request your own listing.");
            return "redirect:" + itemDetailsPath(itemId, navigationSource);
        }

        requestService.requestItem(
                itemId,
                buyerId
        );

        if (source.equals("item")) {
    return "redirect:" + itemDetailsPath(itemId, navigationSource);
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
            @RequestParam(name = "navigationSource", required = false)
            String navigationSource,
            HttpSession session) {

        Integer buyerId = authenticatedUserId(session);
        int itemId = requestService.getItemIdByRequestId(requestId);
        if (buyerId == null) return loginRedirect(destinationForSource(source, itemId, navigationSource));

        requestService.cancelRequest(
                requestId,
                buyerId
        );

        if (source.equals("item")) {
    return "redirect:" + itemDetailsPath(itemId, navigationSource);
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
            if (edit && !"AVAILABLE".equals(item.getItemStatus())) {
                return "redirect:/item?id=" + id;
            }
            model.addAttribute("item", item);
            model.addAttribute("listAgain", !edit && "UNAVAILABLE".equals(item.getItemStatus()));
            model.addAttribute("editing", edit);
        } else return "redirect:/sell";
    }

    return "list-item";
}

    @GetMapping("/item")
    public String itemDetails(
            @RequestParam(name = "id")
            int id,
            @RequestParam(name = "source", required = false)
            String source,

            Model model,
            HttpSession session) {

        Item item =
                itemRepository.getItemById(id);

        if (item == null) {
            return "redirect:/items";
        }

        String navigationSource = "home";
        String backHref = "/";
        String backLabel = "Back to Home";
        if ("browse".equals(source)) {
            navigationSource = "browse";
            backHref = "/items";
            backLabel = "Back to Browse";
        } else if ("sell".equals(source)) {
            navigationSource = "sell";
            backHref = "/sell";
            backLabel = "Back to My Listings";
        }
        model.addAttribute("navigationSource", navigationSource);
        model.addAttribute("detailBackHref", backHref);
        model.addAttribute("detailBackLabel", backLabel);

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
        if (isSeller && "AVAILABLE".equals(item.getItemStatus())) {
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

    private String destinationForSource(String source, int itemId, String navigationSource) {
        if ("item".equals(source) && itemId > 0) return itemDetailsPath(itemId, navigationSource);
        if ("my-requests".equals(source)) return "/my-requests";
        return itemId > 0 ? "/items#item-" + itemId : "/items";
    }

    private String itemDetailsPath(int itemId, String navigationSource) {
        String path = "/item?id=" + itemId;
        if ("browse".equals(navigationSource) || "sell".equals(navigationSource)) {
            path += "&source=" + navigationSource;
        }
        return path;
    }
}
