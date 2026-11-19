package com.meetwise.model.Data;

public class MeetingRoom {

    private int roomId;
    private String roomName;
    private int capacity;
    private String location;
    private String facilities;
    private String status;

    public MeetingRoom() {

    }

    public MeetingRoom(int roomId, String roomName, int capacity,
                       String location, String facilities, String status) {

        this.roomId = roomId;
        this.roomName = roomName;
        this.capacity = capacity;
        this.location = location;
        this.facilities = facilities;
        this.status = status;
    }

    public int getRoomId() {
        return roomId;
    }

    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getFacilities() {
        return facilities;
    }

    public void setFacilities(String facilities) {
        this.facilities = facilities;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}