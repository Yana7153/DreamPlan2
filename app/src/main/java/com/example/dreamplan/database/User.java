package com.example.dreamplan.database;

public class User {
    private int id;
    private String email;
    private String password;
    private String name;

    // Constructors
    public User() {}

    public User(String email, String password, String name) {
        this.email = email;
        this.password = password;
        this.name = name;
    }

    public User(int id, String email, String password, String name) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.name = name;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    // Utility methods
    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", password='[PROTECTED]'" +
                ", name='" + name + '\'' +
                '}';
    }

    // You might want to add validation methods
    public boolean isValidEmail() {
        return email != null && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public boolean isValidPassword() {
        return password != null && password.length() >= 6;
    }

    public boolean isValidName() {
        return name != null && !name.trim().isEmpty();
    }

    // Method to check if all required fields are valid
    public boolean isValidUser() {
        return isValidEmail() && isValidPassword() && isValidName();
    }
}