package com.example.ecommerce.controller;

import jakarta.servlet.http.HttpSession;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    @Autowired
    private JdbcTemplate jdbcTemplate; // For executing SQL queries

    @GetMapping("/home")
    public String homePage(HttpSession session, Model model) {
        return checkSessionAndReturn("home", session, model);
    }

    @GetMapping("/products")
    public String productsPage(HttpSession session, Model model) {
        return checkSessionAndReturn("products", session, model);
    }

    @GetMapping("/cart")
    public String cartPage(HttpSession session, Model model) {
        return checkSessionAndReturn("cart", session, model);
    }

    @GetMapping("/orders")
    public String ordersPage(HttpSession session, Model model) {
        return checkSessionAndReturn("orders", session, model);
    }

    @GetMapping("/profile")
    public String profilePage(HttpSession session, Model model) {
        return checkSessionAndReturn("profile", session, model);
    }

    @GetMapping("/admindashboard")
    public String adminDashboard(HttpSession session, Model model) {
        if (!isAdmin(session)) {
            return "redirect:/"; // Redirect non-admin users to login
        }

        // ✅ Get Product Count from Database
        int productCount = Optional.ofNullable(
    jdbcTemplate.queryForObject("SELECT COUNT(*) FROM products", Integer.class)
).orElse(0);

        model.addAttribute("productCount", productCount);

        return "admindashboard"; // Load Admin Dashboard Page
    }

    // 🔹 Check if user is logged in and pass role for navbar visibility
    private String checkSessionAndReturn(String page, HttpSession session, Model model) {
        String username = (String) session.getAttribute("username");
        String role = (String) session.getAttribute("role");

        if (username == null) {
            return "redirect:/"; // Redirect to login page if not logged in
        }

        model.addAttribute("username", username);
        model.addAttribute("role", role); // ✅ Pass role for navbar button visibility

        return page; // Load requested page
    }

    // 🔹 Helper Method: Check if user is Admin
    private boolean isAdmin(HttpSession session) {
        String role = (String) session.getAttribute("role");
        return role != null && "ADMIN".equals(role);
    }
}
