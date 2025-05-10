package com.example.ecommerce.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

@Service
public class IpWhoisService {

    private static final String ACCESS_TOKEN = "cf90fe8ed34ff1"; // Replace with your ipinfo token

    public String getLocationByIp(String ipAddress) {
        String location = "Unknown";
        try {
            // If running locally and IP is localhost
            if ("127.0.0.1".equals(ipAddress) || "0:0:0:0:0:0:0:1".equals(ipAddress)) {
                ipAddress = ""; // Leave empty to let ipinfo detect public IP
            }

            String apiUrl = "https://ipinfo.io/" + ipAddress + "?token=" + ACCESS_TOKEN;
            HttpURLConnection conn = (HttpURLConnection) new URL(apiUrl).openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(3000);
            conn.setReadTimeout(3000);

            if (conn.getResponseCode() == 200) {
                Scanner scanner = new Scanner(conn.getInputStream());
                String response = scanner.useDelimiter("\\A").next();
                scanner.close();

                ObjectMapper mapper = new ObjectMapper();
                JsonNode node = mapper.readTree(response);

                String city = node.path("city").asText("");
                String region = node.path("region").asText("");
                String country = node.path("country").asText("");

                if (!city.isEmpty() || !region.isEmpty() || !country.isEmpty()) {
                    location = String.join(", ", city, region, country).replaceAll(",\\s*,", ",").replaceAll(",$", "");
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return location;
    }
}

