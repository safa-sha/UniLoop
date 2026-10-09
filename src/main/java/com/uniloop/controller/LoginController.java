package com.uniloop.controller;

import com.uniloop.model.User;
import com.uniloop.repository.UserRepository;
import com.uniloop.service.LoginService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class LoginController {

    private final LoginService loginService = new LoginService(new UserRepository());

    @GetMapping("/login")
    public String loginPage(@RequestParam(name = "returnTo", required = false) String returnTo,
                            @RequestParam(name = "required", required = false) Boolean required,
                            HttpSession session,
                            Model model,
                            HttpServletResponse response,
                            RedirectAttributes redirectAttributes) {
        if (session.getAttribute("authenticatedUserId") != null) {
            return "redirect:" + safeReturnTo(returnTo);
        }
        preventLoginCaching(response);
        if (Boolean.TRUE.equals(required)) {
            redirectAttributes.addFlashAttribute("loginNotice", "Please log in to continue.");
            return loginPageRedirect(returnTo);
        }
        model.addAttribute("returnTo", safeReturnTo(returnTo));
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam(name = "email", required = false) String email,
                        @RequestParam(name = "password", required = false) String password,
                        @RequestParam(name = "returnTo", required = false) String returnTo,
                        HttpServletRequest request,
                        HttpServletResponse response,
                        RedirectAttributes redirectAttributes) {
        HttpSession existingSession = request.getSession(false);
        if (existingSession != null
                && existingSession.getAttribute("authenticatedUserId") != null) {
            return "redirect:" + safeReturnTo(returnTo);
        }

        preventLoginCaching(response);
        User user = loginService.authenticate(email, password);
        if (user == null) {
            redirectAttributes.addFlashAttribute("email", email == null ? "" : email.trim());
            redirectAttributes.addFlashAttribute("loginError", "Email or password is incorrect.");
            return loginPageRedirect(returnTo);
        }

        HttpSession session = request.getSession(true);
        request.changeSessionId();
        session.setAttribute("authenticatedUserId", user.getUserId());
        session.setAttribute("authenticatedUserName", user.getName());
        String fullName = user.getName() == null ? "" : user.getName().trim();
        String firstName = fullName.isEmpty() ? "there" : fullName.split("\\s+", 2)[0];
        session.setAttribute("loginSuccessMessage",
                "Welcome back, " + firstName + "! You have logged in successfully.");
        return "redirect:" + safeReturnTo(returnTo);
    }

    @PostMapping("/dismiss-login-notice")
    public ResponseEntity<Void> dismissLoginNotice(HttpSession session) {
        session.removeAttribute("loginSuccessMessage");
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/logout")
    public String logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return "redirect:/";
    }

    private String safeReturnTo(String returnTo) {
        if (returnTo == null || returnTo.isBlank() || !returnTo.startsWith("/")
                || returnTo.startsWith("//") || returnTo.contains("\\")
                || returnTo.contains("\r") || returnTo.contains("\n")) {
            return "/";
        }
        try {
            URI uri = URI.create(returnTo);
            String path = uri.getPath();
            if (uri.isAbsolute() || uri.getRawAuthority() != null || path == null) return "/";
            if (path.equals("/") || path.equals("/items") || path.equals("/item")
                    || path.equals("/sell") || path.equals("/requests")
                    || path.equals("/my-requests") || path.equals("/list-item")) {
                return returnTo;
            }
        } catch (IllegalArgumentException ignored) {
            return "/";
        }
        return "/";
    }

    private String loginPageRedirect(String returnTo) {
        return "redirect:/login?returnTo="
                + URLEncoder.encode(safeReturnTo(returnTo), StandardCharsets.UTF_8);
    }

    private void preventLoginCaching(HttpServletResponse response) {
        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);
    }
}
