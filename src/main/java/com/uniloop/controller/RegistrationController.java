package com.uniloop.controller;
import com.uniloop.model.User;
import com.uniloop.repository.UserRepository;
import com.uniloop.service.LoginService;
import com.uniloop.service.UserService;
import com.uniloop.service.UserService.RegistrationResult;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
@Controller
public class RegistrationController {
    private final UserRepository userRepository = new UserRepository();
    private final UserService userService = new UserService(userRepository);
    private final LoginService loginService = new LoginService(userRepository);
    @GetMapping("/register")
    public String registrationPage() {
        return "register";
    }
    @PostMapping("/register")
    public String register(@RequestParam(name = "fullName", required = false) String fullName,
                           @RequestParam(name = "email", required = false) String email,
                           @RequestParam(name = "password", required = false) String password,
                           HttpServletRequest request,
                           RedirectAttributes redirectAttributes) {
        RegistrationResult result = userService.register(fullName, email, password);
        if (result == RegistrationResult.SUCCESS) {
            User user = loginService.authenticate(email, password);
            if (user != null) {
                HttpSession session = request.getSession(true);
                request.changeSessionId();
                session.setAttribute("authenticatedUserId", user.getUserId());
                String name = usableDisplayName(user.getName());
                if (name == null) {
                    session.removeAttribute("authenticatedUserName");
                    session.setAttribute("loginSuccessMessage",
                            "Welcome to UniLoop! Your account has been created successfully.");
                } else {
                    session.setAttribute("authenticatedUserName", name);
                    String firstName = name.split("\\s+", 2)[0];
                    session.setAttribute("loginSuccessMessage", "Welcome to UniLoop, "
                            + firstName + "! Your account has been created successfully.");
                }
                return "redirect:/";
            }

            redirectAttributes.addFlashAttribute("loginNotice",
                    "Your account was created, but we couldn't sign you in. Please log in.");
            return "redirect:/login";
        } else {
            redirectAttributes.addFlashAttribute("fullName", fullName == null ? "" : fullName.trim());
            redirectAttributes.addFlashAttribute("email", email == null ? "" : email.trim());
            redirectAttributes.addFlashAttribute("registrationError", errorMessage(result));
        }
        return "redirect:/register";
    }
    private String errorMessage(RegistrationResult result) {
        return switch (result) {
            case INVALID_NAME -> "Please enter your name (up to 100 characters).";
            case INVALID_EMAIL_SYNTAX -> "Please enter a valid college email address.";
            case INVALID_EMAIL_DOMAIN -> "Please use your @saintgits.org college email address.";
            case INVALID_PASSWORD -> "Use a password with at least 8 characters and no more than 72 UTF-8 bytes.";
            case DUPLICATE_EMAIL -> "An account with this email already exists.";
            default -> "We could not create your account right now. Please try again.";
        };
    }

    private String usableDisplayName(String name) {
        if (name == null) return null;
        String trimmed = name.trim();
        return trimmed.isEmpty() || "null".equalsIgnoreCase(trimmed) ? null : trimmed;
    }
}
