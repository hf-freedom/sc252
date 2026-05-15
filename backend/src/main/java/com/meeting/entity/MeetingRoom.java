package com.meeting.entity;

public class MeetingRoom {
    private String id;
    private String name;
    private Integer capacity;
    private String location;
    private String equipment;
    private Boolean available = true;

    public MeetingRoom() {}

    public MeetingRoom(String id, String name, Integer capacity, String location, String equipment, Boolean available) {
        this.id = id;
        this.name = name;
        this.capacity = capacity;
        this.location = location;
        this.equipment = equipment;
        this.available = available;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Integer getCapacity() { return capacity; }
    public void setCapacity(Integer capacity) { this.capacity = capacity; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getEquipment() { return equipment; }
    public void setEquipment(String equipment) { this.equipment = equipment; }
    public Boolean getAvailable() { return available; }
    public void setAvailable(Boolean available) { this.available = available; }
}
