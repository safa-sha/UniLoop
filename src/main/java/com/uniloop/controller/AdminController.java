package com.uniloop.controller;

import com.uniloop.model.AdminCategoryCount;
import com.uniloop.model.AdminChangeResult;
import com.uniloop.model.AdminListing;
import com.uniloop.model.AdminRequest;
import com.uniloop.model.AdminUser;
import com.uniloop.repository.ItemRepository;
import com.uniloop.repository.RequestRepository;
import com.uniloop.repository.UserRepository;
import com.uniloop.service.LoginService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.sql.SQLException;
import java.util.Base64;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Controller
public class AdminController {

    private static final Logger LOGGER = Logger.getLogger(AdminController.class.getName());
    private static final String CSRF_SESSION_KEY = "adminCsrfToken";
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final List<String> REQUEST_STATUSES =
            List.of("PENDING", "ACCEPTED", "REJECTED", "CANCELLED");
    private static final List<String> LISTING_STATUSES =
            List.of("AVAILABLE", "UNAVAILABLE", "SOLD");
    private final LoginService loginService = new LoginService(new UserRepository());
    private final UserRepository userRepository = new UserRepository();
    private final ItemRepository itemRepository = new ItemRepository();
    private final RequestRepository requestRepository = new RequestRepository();

    @GetMapping("/admin")
    public String dashboard(HttpSession session, HttpServletResponse response, Model model) {
        String accessResult = requireAdmin(session, response, "/admin");
        if (accessResult != null) return accessResult;

        try {
            int totalUsers = userRepository.countUsers();
            int activeUsers = userRepository.countActiveUsers();
            int totalListings = itemRepository.countListings();
            int availableListings = itemRepository.countAvailableListings();
            int totalRequests = requestRepository.countRequests();
            int pendingRequests = requestRepository.countRequestsByStatus("PENDING");
            int acceptedRequests = requestRepository.countRequestsByStatus("ACCEPTED");
            int rejectedRequests = requestRepository.countRequestsByStatus("REJECTED");
            int cancelledRequests = requestRepository.countRequestsByStatus("CANCELLED");
            List<AdminCategoryCount> categoryCounts = itemRepository.getListingCountsByCategory();
            model.addAttribute("totalUsers", totalUsers);
            model.addAttribute("activeUsers", activeUsers);
            model.addAttribute("totalListings", totalListings);
            model.addAttribute("availableListings", availableListings);
            model.addAttribute("totalRequests", totalRequests);
            model.addAttribute("pendingRequests", pendingRequests);
            model.addAttribute("acceptedRequests", acceptedRequests);
            model.addAttribute("rejectedRequests", rejectedRequests);
            model.addAttribute("cancelledRequests", cancelledRequests);
            model.addAttribute("categoryCounts", categoryCounts);
        } catch (SQLException exception) {
            LOGGER.log(Level.SEVERE, "Could not load admin dashboard analytics", exception);
            response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            model.addAttribute("dashboardError",
                    "Dashboard analytics are temporarily unavailable. Please try again later.");
        }

        return "admin/dashboard";
    }

    @GetMapping("/admin/users")
    public String users(@RequestParam(name = "q", required = false) String search,
                        @RequestParam(name = "role", required = false) String role,
                        @RequestParam(name = "status", required = false) String status,
                        HttpSession session, HttpServletResponse response, Model model) {
        String accessResult = requireAdmin(session, response, "/admin/users");
        if (accessResult != null) return accessResult;

        model.addAttribute("roleOptions", List.of("STUDENT", "ADMIN"));
        model.addAttribute("userStatusOptions", List.of("ACTIVE", "INACTIVE"));
        try {
            String safeRole = ("STUDENT".equals(role) || "ADMIN".equals(role)) ? role : "";
            String safeStatus = ("ACTIVE".equals(status) || "INACTIVE".equals(status)) ? status : "";
            String safeSearch = search == null ? "" : search.trim();
            if (safeSearch.length() > 100) safeSearch = safeSearch.substring(0, 100);
            List<AdminUser> users = userRepository.findAllForAdmin(safeSearch, safeRole, safeStatus);
            model.addAttribute("users", users);
            model.addAttribute("search", safeSearch);
            model.addAttribute("selectedRole", safeRole);
            model.addAttribute("selectedStatus", safeStatus);
            model.addAttribute("currentAdminId",
                    ((Number) session.getAttribute("authenticatedUserId")).intValue());
            model.addAttribute("adminCsrfToken", csrfToken(session));
        } catch (SQLException exception) {
            LOGGER.log(Level.SEVERE, "Could not load admin user list", exception);
            response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            model.addAttribute("managementError",
                    "User information is temporarily unavailable. Please try again later.");
        }
        return "admin/users";
    }

    @GetMapping("/admin/listings")
    public String listings(@RequestParam(name = "q", required = false) String search,
                           @RequestParam(name = "status", required = false) String status,
                           @RequestParam(name = "category", required = false) Integer categoryId,
                           HttpSession session, HttpServletResponse response, Model model) {
        String accessResult = requireAdmin(session, response, "/admin/listings");
        if (accessResult != null) return accessResult;

        model.addAttribute("listingStatusOptions", LISTING_STATUSES);
        model.addAttribute("categories", List.of());
        try {
            String safeStatus = status != null && LISTING_STATUSES.contains(status) ? status : "";
            String safeSearch = search == null ? "" : search.trim();
            if (safeSearch.length() > 150) safeSearch = safeSearch.substring(0, 150);
            Integer safeCategoryId = categoryId != null && categoryId > 0 ? categoryId : null;
            List<AdminListing> listings =
                    itemRepository.findAllForAdmin(safeSearch, safeStatus, safeCategoryId);
            model.addAttribute("listings", listings);
            model.addAttribute("categories", itemRepository.getListingCountsByCategory());
            model.addAttribute("search", safeSearch);
            model.addAttribute("selectedStatus", safeStatus);
            model.addAttribute("selectedCategory", safeCategoryId);
            model.addAttribute("adminCsrfToken", csrfToken(session));
        } catch (SQLException exception) {
            LOGGER.log(Level.SEVERE, "Could not load admin listing list", exception);
            response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            model.addAttribute("managementError",
                    "Listing information is temporarily unavailable. Please try again later.");
        }
        return "admin/listings";
    }

    @GetMapping("/admin/listings/{itemId}")
    public String listingDetails(@PathVariable int itemId, HttpSession session,
                                 HttpServletResponse response, Model model) {
        String accessResult = requireAdmin(session, response, "/admin/listings");
        if (accessResult != null) return accessResult;
        try {
            AdminListing listing = itemRepository.findAdminListingById(itemId);
            if (listing == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return "admin/not-found";
            }
            model.addAttribute("listing", listing);
            model.addAttribute("adminCsrfToken", csrfToken(session));
            return "admin/listing-details";
        } catch (SQLException exception) {
            LOGGER.log(Level.SEVERE, "Could not load admin listing details", exception);
            response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            model.addAttribute("managementError",
                    "Listing information is temporarily unavailable. Please try again later.");
            return "admin/listing-details";
        }
    }

    @GetMapping("/admin/requests")
    public String requests(@RequestParam(name = "q", required = false) String search,
                           @RequestParam(name = "status", required = false) String status,
                           HttpSession session, HttpServletResponse response, Model model) {
        String accessResult = requireAdmin(session, response, "/admin/requests");
        if (accessResult != null) return accessResult;
        String safeStatus = status != null && REQUEST_STATUSES.contains(status) ? status : "";
        String safeSearch = search == null ? "" : search.trim();
        if (safeSearch.length() > 150) safeSearch = safeSearch.substring(0, 150);
        model.addAttribute("requestStatusOptions", REQUEST_STATUSES);
        try {
            model.addAttribute("requests",
                    requestRepository.findAllForAdmin(safeSearch, safeStatus));
            model.addAttribute("search", safeSearch);
            model.addAttribute("selectedStatus", safeStatus);
            return "admin/requests";
        } catch (SQLException exception) {
            LOGGER.log(Level.SEVERE, "Could not load admin request list", exception);
            response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            model.addAttribute("managementError",
                    "Request information is temporarily unavailable. Please try again later.");
            return "admin/requests";
        }
    }

    @GetMapping("/admin/requests/{requestId}")
    public String requestDetails(@PathVariable int requestId, HttpSession session,
                                 HttpServletResponse response, Model model) {
        String accessResult = requireAdmin(session, response, "/admin/requests");
        if (accessResult != null) return accessResult;
        try {
            AdminRequest request = requestRepository.findAdminRequestById(requestId);
            if (request == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return "admin/not-found";
            }
            model.addAttribute("adminRequest", request);
            return "admin/request-details";
        } catch (SQLException exception) {
            LOGGER.log(Level.SEVERE, "Could not load admin request details", exception);
            response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            model.addAttribute("managementError",
                    "Request information is temporarily unavailable. Please try again later.");
            return "admin/request-details";
        }
    }

    @PostMapping("/admin/users/status")
    public String changeUserStatus(@RequestParam int userId, @RequestParam String newStatus,
                                   @RequestParam("_adminCsrf") String csrf,
                                   HttpServletRequest request, HttpServletResponse response,
                                   RedirectAttributes redirectAttributes) {
        HttpSession session = request.getSession(false);
        String accessResult = requireAdminPost(session, response, "/admin/users", csrf);
        if (accessResult != null) return accessResult;
        int adminId = ((Number) session.getAttribute("authenticatedUserId")).intValue();
        if (userId <= 0 || userId == adminId
                || !List.of("ACTIVE", "INACTIVE").contains(newStatus)) {
            redirectAttributes.addFlashAttribute("adminError",
                    "That account status change is not allowed.");
            return "redirect:/admin/users";
        }

        String expectedStatus = "INACTIVE".equals(newStatus) ? "ACTIVE" : "INACTIVE";
        try {
            AdminChangeResult result = userRepository.changeAccountStatus(
                    userId, expectedStatus, newStatus);
            addChangeResult(redirectAttributes, result, "Account status updated.",
                    "Account status could not be changed.");
        } catch (SQLException exception) {
            LOGGER.log(Level.SEVERE, "Could not change account status", exception);
            redirectAttributes.addFlashAttribute("adminError",
                    "Account status could not be changed. Please try again.");
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/admin/users/role")
    public String changeUserRole(@RequestParam int userId, @RequestParam String newRole,
                                 @RequestParam("_adminCsrf") String csrf,
                                 HttpServletRequest request, HttpServletResponse response,
                                 RedirectAttributes redirectAttributes) {
        HttpSession session = request.getSession(false);
        String accessResult = requireAdminPost(session, response, "/admin/users", csrf);
        if (accessResult != null) return accessResult;
        int adminId = ((Number) session.getAttribute("authenticatedUserId")).intValue();
        if (userId <= 0 || userId == adminId
                || !List.of("ADMIN", "STUDENT").contains(newRole)) {
            redirectAttributes.addFlashAttribute("adminError",
                    "That role change is not allowed.");
            return "redirect:/admin/users";
        }

        String expectedRole = "STUDENT".equals(newRole) ? "ADMIN" : "STUDENT";
        try {
            AdminChangeResult result = userRepository.changeRole(userId, expectedRole, newRole);
            addChangeResult(redirectAttributes, result, "User role updated.",
                    "User role could not be changed.");
        } catch (SQLException exception) {
            LOGGER.log(Level.SEVERE, "Could not change user role", exception);
            redirectAttributes.addFlashAttribute("adminError",
                    "User role could not be changed. Please try again.");
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/admin/listings/unavailable")
    public String markListingUnavailable(@RequestParam int itemId,
                                         @RequestParam("_adminCsrf") String csrf,
                                         HttpServletRequest request, HttpServletResponse response,
                                         RedirectAttributes redirectAttributes) {
        HttpSession session = request.getSession(false);
        String accessResult = requireAdminPost(session, response, "/admin/listings", csrf);
        if (accessResult != null) return accessResult;
        if (itemId <= 0) {
            redirectAttributes.addFlashAttribute("adminError", "The listing could not be found.");
            return "redirect:/admin/listings";
        }

        try {
            AdminChangeResult result = itemRepository.markAvailableListingUnavailable(itemId);
            addChangeResult(redirectAttributes, result, "The listing is now unavailable.",
                    "The listing could not be changed.");
        } catch (SQLException exception) {
            LOGGER.log(Level.SEVERE, "Could not moderate listing", exception);
            redirectAttributes.addFlashAttribute("adminError",
                    "The listing could not be changed. Please try again.");
        }
        return "redirect:/admin/listings";
    }

    private String requireAdmin(HttpSession session, HttpServletResponse response, String path) {
        if (session == null) {
            return "redirect:/login?required=true&returnTo="
                    + URLEncoder.encode(path, StandardCharsets.UTF_8);
        }
        Object authenticatedUserId = session.getAttribute("authenticatedUserId");
        if (!(authenticatedUserId instanceof Number userId) || userId.intValue() <= 0) {
            return "redirect:/login?required=true&returnTo="
                    + URLEncoder.encode(path, StandardCharsets.UTF_8);
        }

        if (!loginService.isActiveAdmin(userId.intValue())) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return "admin/access-denied";
        }
        return null;
    }

    private String requireAdminPost(HttpSession session, HttpServletResponse response,
                                    String path, String submittedToken) {
        String accessResult = requireAdmin(session, response, path);
        if (accessResult != null) return accessResult;
        Object expectedToken = session.getAttribute(CSRF_SESSION_KEY);
        if (!(expectedToken instanceof String token) || submittedToken == null
                || !MessageDigest.isEqual(token.getBytes(StandardCharsets.UTF_8),
                submittedToken.getBytes(StandardCharsets.UTF_8))) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return "admin/access-denied";
        }
        return null;
    }

    private String csrfToken(HttpSession session) {
        Object existing = session.getAttribute(CSRF_SESSION_KEY);
        if (existing instanceof String token) return token;
        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        session.setAttribute(CSRF_SESSION_KEY, token);
        return token;
    }

    private void addChangeResult(RedirectAttributes redirectAttributes, AdminChangeResult result,
                                 String successMessage, String failureMessage) {
        if (result == AdminChangeResult.SUCCESS) {
            redirectAttributes.addFlashAttribute("adminSuccess", successMessage);
        } else if (result == AdminChangeResult.LAST_ACTIVE_ADMIN) {
            redirectAttributes.addFlashAttribute("adminError",
                    "The last active administrator cannot be deactivated or demoted.");
        } else if (result == AdminChangeResult.NOT_FOUND) {
            redirectAttributes.addFlashAttribute("adminError", "The record could not be found.");
        } else {
            redirectAttributes.addFlashAttribute("adminError", failureMessage
                    + " Its status may have changed; refresh and try again.");
        }
    }
}
