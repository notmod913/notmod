package com.example.ecommerce.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // Display Admin Dashboard
    @GetMapping
    public String showAdminDashboard(HttpSession session, Model model) {
        String username = (String) session.getAttribute("username");

        // Check if user is admin
        if (!"admin".equals(username)) {
            return "redirect:/"; // Redirect non-admins
        }

        // Retrieve total product count
        String countQuery = "SELECT COUNT(*) FROM products";
        Integer productCount = jdbcTemplate.queryForObject(countQuery, Integer.class);

        // Retrieve all products
        String selectQuery = "SELECT * FROM products";
        List<Map<String, Object>> products = jdbcTemplate.queryForList(selectQuery);

        model.addAttribute("username", username);
        model.addAttribute("productCount", productCount);
        model.addAttribute("products", products); // Pass product list to frontend
        return "adminDashboard";
    }

    // Handle Add, Update, and Delete in a Single Endpoint
    @PostMapping("/manageProduct")
    public String manageProduct(@RequestParam String actionType,
                                @RequestParam(required = false) Long id,
                                @RequestParam(required = false) String name,
                                @RequestParam(required = false) Double price,
                                @RequestParam(required = false) String pictureUrl,
                                Model model) {
        try {
            switch (actionType) {
                case "add":
                    if (name != null && price != null && pictureUrl != null) {
                        String insertQuery = "INSERT INTO products (name, price, picture_url) VALUES (?, ?, ?)";
                        jdbcTemplate.update(insertQuery, name, price, pictureUrl);
                        model.addAttribute("message", "Product added successfully.");
                    } else {
                        model.addAttribute("error", "All fields are required to add a product.");
                    }
                    break;

                case "update":
                    if (id == null) {
                        model.addAttribute("error", "Product ID is required for update.");
                        break;
                    }

                    StringBuilder updateQuery = new StringBuilder("UPDATE products SET ");
                    List<Object> updateParams = new ArrayList<>();

                    if (name != null) {
                        updateQuery.append("name = ?, ");
                        updateParams.add(name);
                    }
                    if (price != null) {
                        updateQuery.append("price = ?, ");
                        updateParams.add(price);
                    }
                    if (pictureUrl != null) {
                        updateQuery.append("picture_url = ?, ");
                        updateParams.add(pictureUrl);
                    }

                    if (updateParams.isEmpty()) {
                        model.addAttribute("error", "No fields provided for update.");
                        break;
                    }

                    // Remove trailing comma and space
                    updateQuery.setLength(updateQuery.length() - 2);
                    updateQuery.append(" WHERE id = ?");
                    updateParams.add(id);

                    int rowsUpdated = jdbcTemplate.update(updateQuery.toString(), updateParams.toArray());
                    model.addAttribute("message", rowsUpdated > 0 ? "Product updated successfully." : "Product not found.");
                    break;

                case "delete":
                    if (id != null) {
                        String deleteQuery = "DELETE FROM products WHERE id = ?";
                        int rowsDeleted = jdbcTemplate.update(deleteQuery, id);
                        model.addAttribute("message", rowsDeleted > 0 ? "Product deleted successfully." : "Product not found.");
                    } else {
                        model.addAttribute("error", "Product ID is required for deletion.");
                    }
                    break;

                default:
                    model.addAttribute("error", "Invalid action type.");
            }
        } catch (Exception e) {
            model.addAttribute("error", "Error processing request: " + e.getMessage());
        }

        return "redirect:/admin";
    }
}


