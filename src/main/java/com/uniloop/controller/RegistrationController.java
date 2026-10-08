package com.uniloop.controller;
import com.uniloop.repository.UserRepository;
import com.uniloop.service.UserService;
import com.uniloop.service.UserService.RegistrationResult;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
@Controller
public class RegistrationController {
    private final UserService userService = new UserService(new UserRepository());
    @GetMapping("/register")
    public String registrationPage() {
        return "register";
    }
    @PostMapping("/register")
    public String register(@RequestParam(name = "fullName", required = false) String fullName,
                           @RequestParam(name = "email", required = false) String email,
                           @RequestParam(name = "password", required = false) String password,
                           RedirectAttributes redirectAttributes) {
        RegistrationResult result = userService.register(fullName, email, password);
        if (result == RegistrationResult.SUCCESS) {
            redirectAttributes.addFlashAttribute(
                    "registrationSuccessMessage",
                    "Account created. You can sign in when login is available.");
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
}
