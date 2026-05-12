// Legacy Java Service with Multiple Issues
// This code contains deliberate problems for testing AI refactoring capabilities

package com.example.legacy;

import java.sql.*;
import java.util.*;
import java.io.*;

public class LegacyUserService {
    
    private Connection connection;
    
    // Issue 1: No connection pooling, creates new connection each time
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:mysql://localhost:3306/userdb", "admin", "password123");
    }
    
    // Issue 2: N+1 Query Problem, Poor exception handling, SQL Injection vulnerability
    public List<User> getUsersWithOrders() {
        List<User> users = new ArrayList<>();
        Statement stmt = null;
        ResultSet rs = null;
        
        try {
            connection = getConnection();
            stmt = connection.createStatement();
            
            // Gets all users first
            rs = stmt.executeQuery("SELECT * FROM users");
            
            while (rs.next()) {
                User user = new User();
                user.setId(rs.getInt("id"));
                user.setName(rs.getString("name"));
                user.setEmail(rs.getString("email"));
                
                // N+1 Problem: Separate query for each user's orders
                Statement orderStmt = connection.createStatement();
                String orderQuery = "SELECT * FROM orders WHERE user_id = " + user.getId(); // SQL Injection vulnerability
                ResultSet orderRs = orderStmt.executeQuery(orderQuery);
                
                List<Order> orders = new ArrayList<>();
                while (orderRs.next()) {
                    Order order = new Order();
                    order.setId(orderRs.getInt("id"));
                    order.setAmount(orderRs.getDouble("amount"));
                    order.setStatus(orderRs.getString("status"));
                    orders.add(order);
                }
                user.setOrders(orders);
                users.add(user);
            }
            
        } catch (Exception e) {
            // Poor exception handling - just prints stack trace
            e.printStackTrace();
        }
        // Issue 3: Resources not properly closed, potential memory leak
        
        return users;
    }
    
    // Issue 4: Memory leak - static collection that grows indefinitely
    private static Map<String, String> sessionCache = new HashMap<>();
    
    public void addToCache(String key, String value) {
        sessionCache.put(key, value);
        // Cache never cleaned up, grows indefinitely
    }
    
    // Issue 5: Security vulnerability - password stored in plain text
    public boolean authenticateUser(String username, String password) {
        try {
            connection = getConnection();
            Statement stmt = connection.createStatement();
            
            // SQL Injection vulnerability
            String query = "SELECT password FROM users WHERE username = '" + username + "'";
            ResultSet rs = stmt.executeQuery(query);
            
            if (rs.next()) {
                String storedPassword = rs.getString("password");
                return password.equals(storedPassword); // Plain text comparison
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return false;
    }
    
    // Issue 6: No proper error handling for file operations
    public void exportUserData(String filename) {
        FileWriter writer = null;
        try {
            writer = new FileWriter(filename);
            List<User> users = getUsersWithOrders();
            
            for (User user : users) {
                writer.write(user.toString() + "\n");
                // Potential OutOfMemoryError with large datasets
            }
            
        } catch (IOException e) {
            // Swallows exception, hard to debug
            System.out.println("Error occurred");
        }
        // File writer not closed properly
    }
    
    // Issue 7: Thread safety problems
    private int requestCount = 0;
    
    public void incrementRequestCount() {
        requestCount++; // Not thread-safe, can cause race conditions
    }
    
    public int getRequestCount() {
        return requestCount;
    }
}

// Supporting classes
class User {
    private int id;
    private String name;
    private String email;
    private List<Order> orders;
    
    // Standard getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public List<Order> getOrders() { return orders; }
    public void setOrders(List<Order> orders) { this.orders = orders; }
    
    @Override
    public String toString() {
        return "User{id=" + id + ", name='" + name + "', email='" + email + "', orders=" + orders.size() + "}";
    }
}

class Order {
    private int id;
    private double amount;
    private String status;
    
    // Standard getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    @Override
    public String toString() {
        return "Order{id=" + id + ", amount=" + amount + ", status='" + status + "'}";
    }
}