package com.uniloop.controller;

import com.uniloop.repository.UserRepository;
import com.uniloop.service.LoginService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class NavbarModelAdvice {

    private final LoginService loginService = new LoginService(new UserRepository());

    @ModelAttribute
    public void addAdminNavigationFlag(HttpServletRequest request, Model model) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            model.addAttribute("activeAdmin", false);
            return;
        }

        Object authenticatedUserId = session.getAttribute("authenticatedUserId");
        boolean activeAdmin = authenticatedUserId instanceof Number userId
                && loginService.isActiveAdmin(userId.intValue());
        model.addAttribute("activeAdmin", activeAdmin);
    }
}
