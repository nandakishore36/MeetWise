package com.meetwise.model.service;

import java.util.List;

import com.meetwise.model.Data.MeetingRoom;
import com.meetwise.model.Data.User;
import com.meetwise.model.dao.RoomDAO;
import com.meetwise.model.dao.UserDAO;
import com.meetwise.model.design.RoomDesign;

public class RoomService implements RoomDesign {

    private RoomDAO roomDAO = new RoomDAO();
    private UserDAO userDAO = new UserDAO();

    // Verify Admin
    private boolean isAdmin(int adminId) {

        User admin = userDAO.getUserById(adminId);

        return admin != null && "ADMIN".equals(admin.getRole());
    }

    // Add room
    @Override
    public boolean addRoom(MeetingRoom room, int adminId) {

        if (!isAdmin(adminId) || room == null) {
            return false;
        }

        if (room.getRoomName() == null
                || room.getRoomName().trim().isEmpty()
                || room.getCapacity() <= 0
                || room.getFacilities() == null
                || room.getFacilities().trim().isEmpty()) {

            return false;
        }

        room.setStatus("ACTIVE");

        return roomDAO.addRoom(room);
    }

    // Get all rooms
    @Override
    public List<MeetingRoom> getAllRooms() {
        return roomDAO.getAllRooms();
    }

    // Get room by ID
    @Override
    public MeetingRoom getRoomById(int roomId) {

        if (roomId <= 0) {
            return null;
        }

        return roomDAO.getRoomById(roomId);
    }

    // Update room
    @Override
    public boolean updateRoom(MeetingRoom room, int adminId) {

        if (!isAdmin(adminId) || room == null
                || room.getRoomId() <= 0
                || room.getRoomName() == null
                || room.getRoomName().trim().isEmpty()
                || room.getCapacity() <= 0) {

            return false;
        }

        return roomDAO.updateRoom(room);
    }

    // Deactivate room
    @Override
    public boolean deactivateRoom(int roomId, int adminId) {

        if (!isAdmin(adminId) || roomId <= 0) {
            return false;
        }

        return roomDAO.deactivateRoom(roomId);
    }
}
