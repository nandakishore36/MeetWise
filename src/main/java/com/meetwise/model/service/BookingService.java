package com.meetwise.model.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.Duration;
import java.util.List;

import com.meetwise.model.Data.Booking;
import com.meetwise.model.Data.MeetingRoom;
import com.meetwise.model.Data.User;
import com.meetwise.model.dao.BookingDAO;
import com.meetwise.model.dao.UserDAO;
import com.meetwise.model.design.BookingDesign;

public class BookingService implements BookingDesign {

    private BookingDAO bookingDAO = new BookingDAO();
    private UserDAO userDAO = new UserDAO();
    private AllocationService allocationService = new AllocationService();

    // Validate booking details
    private boolean isValidBooking(Booking booking) {

        if (booking == null) {
            return false;
        }

        if (booking.getAttendees() <= 0) {
            return false;
        }

        if (booking.getMeetingDate() == null ||
            booking.getStartTime() == null ||
            booking.getEndTime() == null) {
            return false;
        }

        if (booking.getRequiredFacilities() == null ||
            booking.getRequiredFacilities().trim().isEmpty()) {
            return false;
        }

        LocalDate meetingDate = booking.getMeetingDate().toLocalDate();

        LocalDate today = LocalDate.now(ZoneId.of("Asia/Kolkata"));
        LocalDate lastBookingDate = today.plusDays(7);

        if (meetingDate.isBefore(today) ||
            meetingDate.isAfter(lastBookingDate)) {
            return false;
        }

        LocalTime start = booking.getStartTime().toLocalTime();
        LocalTime end = booking.getEndTime().toLocalTime();

        if (!end.isAfter(start)) {
            return false;
        }

        // Working hours: 10 AM to 4 PM
        if (start.isBefore(LocalTime.of(10, 0)) ||
            end.isAfter(LocalTime.of(16, 0))) {
            return false;
        }

        // Lunch break: 1 PM to 2 PM
        if (start.isBefore(LocalTime.of(13, 0)) &&
            end.isAfter(LocalTime.of(13, 0))) {
            return false;
        }

        if (start.isBefore(LocalTime.of(14, 0)) &&
            end.isAfter(LocalTime.of(13, 0))) {
            return false;
        }

        // Duration must be at least 30 minutes
        long duration = Duration.between(start, end).toMinutes();

        if (duration < 30 || duration % 30 != 0) {
            return false;
        }

        // Start and end must be on 30-minute boundaries
        if (start.getMinute() % 30 != 0 ||
            end.getMinute() % 30 != 0 ||
            start.getSecond() != 0 ||
            end.getSecond() != 0) {
            return false;
        }

        return true;
    }

    // Display available rooms
    @Override
    public List<MeetingRoom> getAvailableRooms(Booking booking) {

        if (!isValidBooking(booking)) {
            return List.of();
        }

        return bookingDAO.getAvailableRooms(booking);
    }

    // Allocate the smallest suitable room
    @Override
    public MeetingRoom allocateRoom(Booking booking) {

        if (!isValidBooking(booking)) {
            return null;
        }

        return allocationService.allocateBestRoom(booking);
    }

    // Create a booking
    @Override
    public boolean createBooking(Booking booking) {

        if (!isValidBooking(booking)) {
            return false;
        }

        if (booking.getBookedBy() <= 0 ||
            booking.getTeamId() <= 0) {
            return false;
        }

        // Check the booking user
        User user = userDAO.getUserById(booking.getBookedBy());

        if (user == null) {
            return false;
        }

        // Only Team Leads can book
        if (!"TEAM_LEAD".equals(user.getRole())) {
            return false;
        }

        // Team Lead can book only for their own team
        if (user.getTeamId() != booking.getTeamId()) {
            return false;
        }

        // Automatically select the smallest suitable room
        MeetingRoom bestRoom = allocationService.allocateBestRoom(booking);

        if (bestRoom == null) {
            return false;
        }

        booking.setRoomId(bestRoom.getRoomId());

        // DAO rechecks availability and permissions inside a transaction
        return bookingDAO.createBooking(booking);
    }

    // Cancel a booking
    @Override
    public boolean cancelBooking(int bookingId, int userId) {

        if (bookingId <= 0 || userId <= 0) {
            return false;
        }

        return bookingDAO.cancelBooking(bookingId, userId);
    }

    // Get bookings belonging to a team
    @Override
    public List<Booking> getBookingsByTeam(int teamId) {

        if (teamId <= 0) {
            return List.of();
        }

        return bookingDAO.getBookingsByTeam(teamId);
    }

    // Get confirmed bookings for a room
    @Override
    public List<Booking> getRoomBookings(int roomId) {

        if (roomId <= 0) {
            return List.of();
        }

        return bookingDAO.getRoomBookings(roomId);
    }
}
