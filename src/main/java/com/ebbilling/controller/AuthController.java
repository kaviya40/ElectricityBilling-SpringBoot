package com.ebbilling.controller;

import com.ebbilling.entity.User;
import com.ebbilling.service.impl.UserServiceImpl;
import jakarta.validation.constraints.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserServiceImpl userService;

    @GetMapping("/login")
    public String loginPage(@RequestParam(required=false) String error,
                            @RequestParam(required=false) String logout,
                            @RequestParam(required=false) String expired, Model model) {
        if (error   != null) model.addAttribute("error",   "Invalid username or password.");
        if (logout  != null) model.addAttribute("message", "Logged out successfully.");
        if (expired != null) model.addAttribute("error",   "Session expired. Please login again.");
        return "auth/login";
    }

    @GetMapping("/register")
    public String registerPage() { return "auth/register"; }

    @PostMapping("/register")
    public String register(@RequestParam String username,
                           @RequestParam String fullName,
                           @RequestParam String email,
                           @RequestParam String password,
                           @RequestParam String confirmPassword,
                           @RequestParam(required=false) String phone,
                           RedirectAttributes ra) {
        try {
            if (!password.equals(confirmPassword)) throw new RuntimeException("Passwords do not match");
            if (password.length() < 6) throw new RuntimeException("Password must be at least 6 characters");
            userService.register(username, fullName, email, password, phone, User.Role.CUSTOMER);
            ra.addFlashAttribute("success", "Registration successful! Please login.");
            return "redirect:/auth/login";
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/auth/register";
        }
    }
}
