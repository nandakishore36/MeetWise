package com.meetwise.model.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import com.meetwise.model.Data.MeetingRoom;
import com.meetwise.util.DBConnection;

public class RoomDAO {

    // Add a new meeting room
    public boolean addRoom(MeetingRoom room) {

        String sql = "INSERT INTO meeting_rooms "
                   + "(room_name, capacity, location, facilities, status) "
                   + "VALUES (?, ?, ?, ?, ?)";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, room.getRoomName());
            ps.setInt(2, room.getCapacity());
            ps.setString(3, room.getLocation());
            ps.setString(4, room.getFacilities());
            ps.setString(5, room.getStatus());

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Get all meeting rooms
    public List<MeetingRoom> getAllRooms() {

        List<MeetingRoom> rooms = new ArrayList<>();

        String sql = "SELECT * FROM meeting_rooms ORDER BY capacity";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                rooms.add(mapRoom(rs));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return rooms;
    }

    // Get one room by ID
    public MeetingRoom getRoomById(int roomId) {

        String sql = "SELECT * FROM meeting_rooms WHERE room_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, roomId);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    return mapRoom(rs);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    // Update room details
    public boolean updateRoom(MeetingRoom room) {

        String sql = "UPDATE meeting_rooms SET "
                   + "room_name = ?, capacity = ?, location = ?, "
                   + "facilities = ?, status = ? "
                   + "WHERE room_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, room.getRoomName());
            ps.setInt(2, room.getCapacity());
            ps.setString(3, room.getLocation());
            ps.setString(4, room.getFacilities());
            ps.setString(5, room.getStatus());
            ps.setInt(6, room.getRoomId());

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Deactivate a room instead of deleting it
    public boolean deactivateRoom(int roomId) {

        String sql = "UPDATE meeting_rooms "
                   + "SET status = 'INACTIVE' "
                   + "WHERE room_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, roomId);

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Convert ResultSet row into MeetingRoom object
    private MeetingRoom mapRoom(ResultSet rs) throws Exception {

        MeetingRoom room = new MeetingRoom();

        room.setRoomId(rs.getInt("room_id"));
        room.setRoomName(rs.getString("room_name"));
        room.setCapacity(rs.getInt("capacity"));
        room.setLocation(rs.getString("location"));
        room.setFacilities(rs.getString("facilities"));
        room.setStatus(rs.getString("status"));

        return room;
    }
}
