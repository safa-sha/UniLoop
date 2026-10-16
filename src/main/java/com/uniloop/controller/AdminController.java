package com.uniloop.controller;

import com.uniloop.model.AdminListing;
import com.uniloop.model.AdminUser;
import com.uniloop.repository.UserRepository;
import com.uniloop.repository.ItemRepository;
import com.uniloop.service.LoginService;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Controller
public class AdminController {

    private static final Logger LOGGER = Logger.getLogger(AdminController.class.getName());
    private final LoginService loginService = new LoginService(new UserRepository());
    private final UserRepository userRepository = new UserRepository();
    private final ItemRepository itemRepository = new ItemRepository();

    @GetMapping("/admin")
    public String dashboard(HttpSession session, HttpServletResponse response, Model model) {
        String accessResult = requireAdmin(session, response, "/admin");
        if (accessResult != null) return accessResult;

        try {
            int totalUsers = userRepository.countUsers();
            int activeUsers = userRepository.countActiveUsers();
            int totalListings = itemRepository.countListings();
            int availableListings = itemRepository.countAvailableListings();
            model.addAttribute("totalUsers", totalUsers);
            model.addAttribute("activeUsers", activeUsers);
            model.addAttribute("totalListings", totalListings);
            model.addAttribute("availableListings", availableListings);
        } catch (SQLException exception) {
            LOGGER.log(Level.SEVERE, "Could not load admin dashboard statistics", exception);
            response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            model.addAttribute("dashboardError",
                    "Dashboard statistics are temporarily unavailable. Please try again later.");
        }

        return "admin/dashboard";
    }

    @GetMapping("/admin/users")
    public String users(HttpSession session, HttpServletResponse response, Model model) {
        String accessResult = requireAdmin(session, response, "/admin/users");
        if (accessResult != null) return accessResult;

        try {
            List<AdminUser> users = userRepository.findAllForAdmin();
            model.addAttribute("users", users);
        } catch (SQLException exception) {
            LOGGER.log(Level.SEVERE, "Could not load admin user list", exception);
            response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            model.addAttribute("managementError",
                    "User information is temporarily unavailable. Please try again later.");
        }
        return "admin/users";
    }

    @GetMapping("/admin/listings")
    public String listings(HttpSession session, HttpServletResponse response, Model model) {
        String accessResult = requireAdmin(session, response, "/admin/listings");
        if (accessResult != null) return accessResult;

        try {
            List<AdminListing> listings = itemRepository.findAllForAdmin();
            model.addAttribute("listings", listings);
        } catch (SQLException exception) {
            LOGGER.log(Level.SEVERE, "Could not load admin listing list", exception);
            response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            model.addAttribute("managementError",
                    "Listing information is temporarily unavailable. Please try again later.");
        }
        return "admin/listings";
    }

    private String requireAdmin(HttpSession session, HttpServletResponse response, String path) {
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
}
