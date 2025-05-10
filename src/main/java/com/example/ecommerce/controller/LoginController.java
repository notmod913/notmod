package com.example.ecommerce.controller;

import com.example.ecommerce.service.IpWhoisService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

@Controller
public class LoginController {

    private final String LOG_FILE_PATH = "src/main/resources/static/logins.json";

    @Autowired
    private IpWhoisService ipWhoisService;

    @GetMapping("/")
    public String showLoginPage() {
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String username,
                        @RequestParam String password,
                        HttpSession session,
                        HttpServletRequest request,
                        Model model) {

        boolean validUser = false;
        String role = "";

        if ("not".equals(username) && "mod".equals(password)) {
            role = "USER";
            validUser = true;
        } else if ("admin".equals(username) && "mod".equals(password)) {
            role = "ADMIN";
            validUser = true;
        } else if ("notmod".equals(username) && "loggers".equals(password)) {
            validUser = true;
        }

        if (validUser) {
            session.setAttribute("username", username);
            session.setAttribute("role", role);

            // Detect real IP address (supports proxies like Render's)
            String ipAddress = extractClientIp(request);
            String location = ipWhoisService.getLocationByIp(ipAddress);

            // Log the login event
            logLogin(username, ipAddress, role, location);

            if ("notmod".equals(username) && "loggers".equals(password)) {
                return "redirect:/iplogs";
            }

            return "redirect:/Dashboard";
        } else {
            model.addAttribute("error", "Invalid username or password");
            return "login";
        }
    }

    private String extractClientIp(HttpServletRequest request) {
        String header = request.getHeader("X-Forwarded-For");
        if (header != null && !header.isEmpty()) {
            return header.split(",")[0].trim(); // first IP in list
        }
        return request.getRemoteAddr();
    }

    private void logLogin(String username, String ipAddress, String role, String location) {
        ObjectMapper mapper = new ObjectMapper();
        List<Map<String, Object>> logs = new ArrayList<>();
        File logFile = new File(LOG_FILE_PATH);

        try {
            if (logFile.exists()) {
                logs = mapper.readValue(logFile, new TypeReference<>() {});
            }

            Map<String, Object> entry = new HashMap<>();
            entry.put("username", username);  // Add username to the log entry
            entry.put("ip", ipAddress);       // Add IP to the log entry
            entry.put("role", role);          // Add role (USER/ADMIN) to the log entry
            entry.put("location", location);  // Add location (city, region, country)
            entry.put("timestamp", LocalDateTime.now().toString());  // Timestamp of login event

            logs.add(entry);
            mapper.writerWithDefaultPrettyPrinter().writeValue(logFile, logs);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @GetMapping("/Dashboard")
    public String showDashboard(HttpSession session, Model model) {
        String username = (String) session.getAttribute("username");
        if (username == null) {
            return "redirect:/";
        }
        model.addAttribute("username", username);
        model.addAttribute("role", session.getAttribute("role"));
        return "Dashboard";
    }

    @GetMapping("/adminDashboard")
    public String showAdminDashboard(HttpSession session, Model model) {
        String role = (String) session.getAttribute("role");
        if (role == null || !"ADMIN".equals(role)) {
            return "redirect:/Dashboard";
        }
        model.addAttribute("username", session.getAttribute("username"));
        return "adminDashboard";
    }

    @GetMapping("/iplogs")
    public String showIpLogs(HttpSession session, Model model) {
        String username = (String) session.getAttribute("username");
        if (username == null) {
            return "redirect:/";
        }

        ObjectMapper mapper = new ObjectMapper();
        List<Map<String, Object>> logs = new ArrayList<>();
        File logFile = new File(LOG_FILE_PATH);

        if (logFile.exists()) {
            try {
                logs = mapper.readValue(logFile, new TypeReference<>() {});
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        model.addAttribute("logs", logs);
        return "iplogs";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }
}
