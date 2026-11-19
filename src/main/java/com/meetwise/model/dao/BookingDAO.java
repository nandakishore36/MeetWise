package com.meetwise.model.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Date;
import java.sql.Time;
import java.sql.Types;

import java.util.ArrayList;
import java.util.List;

import com.meetwise.model.Data.Booking;
import com.meetwise.model.Data.MeetingRoom;
import com.meetwise.util.DBConnection;

public class BookingDAO {

    // 1. Get available rooms matching capacity and facilities
    public List<MeetingRoom> getAvailableRooms(Booking booking) {

        List<MeetingRoom> availableRooms = new ArrayList<>();

        String sql = "SELECT * FROM meeting_rooms "
                   + "WHERE status = 'ACTIVE' AND capacity >= ? "
                   + "ORDER BY capacity ASC, room_id ASC";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, booking.getAttendees());

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {

                    MeetingRoom room = mapRoom(rs);

                    if (!hasRequiredFacilities(
                            room.getFacilities(),
                            booking.getRequiredFacilities())) {
                        continue;
                    }

                    if (!hasBookingConflict(
                            con,
                            room.getRoomId(),
                            booking.getMeetingDate(),
                            booking.getStartTime(),
                            booking.getEndTime())) {

                        availableRooms.add(room);
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return availableRooms;
    }


    // 2. Allocate the smallest suitable room
    public MeetingRoom allocateRoom(Booking booking) {

        List<MeetingRoom> rooms = getAvailableRooms(booking);

        if (rooms.isEmpty()) {
            return null;
        }

        // Rooms are already sorted by capacity
        return rooms.get(0);
    }


    // 3. Create booking safely using a database transaction
    public boolean createBooking(Booking booking) {

        Connection con = null;

        try {

            con = DBConnection.getConnection();
            con.setAutoCommit(false);

            // Lock the selected room during confirmation
            String roomSql =
                "SELECT * FROM meeting_rooms "
              + "WHERE room_id = ? FOR UPDATE";

            MeetingRoom room;

            try (PreparedStatement ps =
                    con.prepareStatement(roomSql)) {

                ps.setInt(1, booking.getRoomId());

                try (ResultSet rs = ps.executeQuery()) {

                    if (!rs.next()) {
                        con.rollback();
                        return false;
                    }

                    room = mapRoom(rs);
                }
            }

            // Check room status and capacity again
            if (!"ACTIVE".equals(room.getStatus())
                    || room.getCapacity() < booking.getAttendees()) {

                con.rollback();
                return false;
            }

            // Check required facilities again
            if (!hasRequiredFacilities(
                    room.getFacilities(),
                    booking.getRequiredFacilities())) {

                con.rollback();
                return false;
            }

            // Verify that the logged-in user is the Team Lead
            String userSql =
                "SELECT role, team_id FROM users WHERE user_id = ?";

            try (PreparedStatement ps =
                    con.prepareStatement(userSql)) {

                ps.setInt(1, booking.getBookedBy());

                try (ResultSet rs = ps.executeQuery()) {

                    if (!rs.next()
                            || !"TEAM_LEAD".equals(rs.getString("role"))
                            || rs.getInt("team_id") != booking.getTeamId()) {

                        con.rollback();
                        return false;
                    }
                }
            }

            // Check overlapping bookings again
            if (hasBookingConflict(
                    con,
                    booking.getRoomId(),
                    booking.getMeetingDate(),
                    booking.getStartTime(),
                    booking.getEndTime())) {

                con.rollback();
                return false;
            }

            // Insert confirmed booking
            String insertSql =
                "INSERT INTO bookings "
              + "(team_id, room_id, booked_by, meeting_date, "
              + "start_time, end_time, attendees, status, "
              + "required_facilities) "
              + "VALUES (?, ?, ?, ?, ?, ?, ?, 'CONFIRMED', ?)";

            int bookingId;

            try (PreparedStatement ps =
                    con.prepareStatement(
                        insertSql,
                        PreparedStatement.RETURN_GENERATED_KEYS)) {

                ps.setInt(1, booking.getTeamId());
                ps.setInt(2, booking.getRoomId());
                ps.setInt(3, booking.getBookedBy());
                ps.setDate(4, booking.getMeetingDate());
                ps.setTime(5, booking.getStartTime());
                ps.setTime(6, booking.getEndTime());
                ps.setInt(7, booking.getAttendees());

                if (booking.getRequiredFacilities() == null) {
                    ps.setNull(8, Types.VARCHAR);
                } else {
                    ps.setString(8, booking.getRequiredFacilities());
                }

                if (ps.executeUpdate() == 0) {
                    con.rollback();
                    return false;
                }

                try (ResultSet keys = ps.getGeneratedKeys()) {

                    if (!keys.next()) {
                        con.rollback();
                        return false;
                    }

                    bookingId = keys.getInt(1);
                }
            }

            // Record booking history
            insertHistory(con, bookingId, "BOOKED");

            con.commit();

            booking.setBookingId(bookingId);
            booking.setStatus("CONFIRMED");

            return true;

        } catch (Exception e) {

            if (con != null) {
                try {
                    con.rollback();
                } catch (Exception rollbackError) {
                    rollbackError.printStackTrace();
                }
            }

            e.printStackTrace();
            return false;

        } finally {

            if (con != null) {
                try {
                    con.setAutoCommit(true);
                    con.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }


    // 4. Cancel booking with permission verification
    public boolean cancelBooking(int bookingId, int userId) {

        Connection con = null;

        try {

            con = DBConnection.getConnection();
            con.setAutoCommit(false);

            String bookingSql =
                "SELECT booked_by, status FROM bookings "
              + "WHERE booking_id = ? FOR UPDATE";

            int bookedBy;
            String status;

            try (PreparedStatement ps =
                    con.prepareStatement(bookingSql)) {

                ps.setInt(1, bookingId);

                try (ResultSet rs = ps.executeQuery()) {

                    if (!rs.next()) {
                        con.rollback();
                        return false;
                    }

                    bookedBy = rs.getInt("booked_by");
                    status = rs.getString("status");
                }
            }

            if (!"CONFIRMED".equals(status)) {
                con.rollback();
                return false;
            }

            // Check whether requester is Admin or booking Team Lead
            String userSql =
                "SELECT role FROM users WHERE user_id = ?";

            String role;

            try (PreparedStatement ps =
                    con.prepareStatement(userSql)) {

                ps.setInt(1, userId);

                try (ResultSet rs = ps.executeQuery()) {

                    if (!rs.next()) {
                        con.rollback();
                        return false;
                    }

                    role = rs.getString("role");
                }
            }

            boolean isAdmin = "ADMIN".equals(role);

            boolean isBookingLead =
                "TEAM_LEAD".equals(role) && bookedBy == userId;

            if (!isAdmin && !isBookingLead) {
                con.rollback();
                return false;
            }

            String updateSql =
                "UPDATE bookings SET status = 'CANCELLED' "
              + "WHERE booking_id = ?";

            try (PreparedStatement ps =
                    con.prepareStatement(updateSql)) {

                ps.setInt(1, bookingId);

                if (ps.executeUpdate() == 0) {
                    con.rollback();
                    return false;
                }
            }

            insertHistory(con, bookingId, "CANCELLED");

            con.commit();

            return true;

        } catch (Exception e) {

            if (con != null) {
                try {
                    con.rollback();
                } catch (Exception rollbackError) {
                    rollbackError.printStackTrace();
                }
            }

            e.printStackTrace();
            return false;

        } finally {

            if (con != null) {
                try {
                    con.setAutoCommit(true);
                    con.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }


    // 5. Get bookings belonging to a team
    public List<Booking> getBookingsByTeam(int teamId) {

        List<Booking> bookings = new ArrayList<>();

        String sql =
            "SELECT * FROM bookings "
          + "WHERE team_id = ? ORDER BY meeting_date, start_time";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, teamId);

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    bookings.add(mapBooking(rs));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return bookings;
    }


    // 6. Get confirmed bookings for a room
    public List<Booking> getRoomBookings(int roomId) {

        List<Booking> bookings = new ArrayList<>();

        String sql =
            "SELECT * FROM bookings "
          + "WHERE room_id = ? AND status = 'CONFIRMED' "
          + "ORDER BY meeting_date, start_time";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, roomId);

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    bookings.add(mapBooking(rs));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return bookings;
    }


    // Check overlapping bookings
    private boolean hasBookingConflict(
            Connection con,
            int roomId,
            Date meetingDate,
            Time startTime,
            Time endTime) throws Exception {

        String sql =
            "SELECT COUNT(*) FROM bookings "
          + "WHERE room_id = ? "
          + "AND meeting_date = ? "
          + "AND status = 'CONFIRMED' "
          + "AND start_time < ? "
          + "AND end_time > ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, roomId);
            ps.setDate(2, meetingDate);
            ps.setTime(3, endTime);
            ps.setTime(4, startTime);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }

        return false;
    }


    // Match every requested facility
    private boolean hasRequiredFacilities(
            String roomFacilities,
            String requiredFacilities) {

        if (requiredFacilities == null
                || requiredFacilities.trim().isEmpty()) {
            return true;
        }

        if (roomFacilities == null) {
            return false;
        }

        String[] required =
            requiredFacilities.split(",");

        List<String> available = new ArrayList<>();

        for (String facility : roomFacilities.split(",")) {
            available.add(facility.trim().toLowerCase());
        }

        for (String facility : required) {

            String requested = facility.trim().toLowerCase();

            if (requested.isEmpty()) {
                continue;
            }

            if (!available.contains(requested)) {
                return false;
            }
        }

        return true;
    }


    // Insert booking history
    private void insertHistory(
            Connection con,
            int bookingId,
            String action) throws Exception {

        String sql =
            "INSERT INTO booking_history (booking_id, action) "
          + "VALUES (?, ?)";

        try (PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, bookingId);
            ps.setString(2, action);

            ps.executeUpdate();
        }
    }


    // Convert database row into MeetingRoom object
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


    // Convert database row into Booking object
    private Booking mapBooking(ResultSet rs) throws Exception {

        Booking booking = new Booking();

        booking.setBookingId(rs.getInt("booking_id"));
        booking.setTeamId(rs.getInt("team_id"));
        booking.setRoomId(rs.getInt("room_id"));
        booking.setBookedBy(rs.getInt("booked_by"));

        booking.setMeetingDate(rs.getDate("meeting_date"));
        booking.setStartTime(rs.getTime("start_time"));
        booking.setEndTime(rs.getTime("end_time"));

        booking.setAttendees(rs.getInt("attendees"));
        booking.setStatus(rs.getString("status"));

        booking.setRequiredFacilities(
            rs.getString("required_facilities"));

        return booking;
    }
}
