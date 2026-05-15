package com.meeting.entity;

public class Employee {
    private String id;
    private String name;
    private String email;
    private String department;
    private Boolean available = true;

    public Employee() {}

    public Employee(String id, String name, String email, String department, Boolean available) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.department = department;
        this.available = available;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    public Boolean getAvailable() { return available; }
    public void setAvailable(Boolean available) { this.available = available; }
}
