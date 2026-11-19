
package com.meetwise.model.service;

import java.util.List;

import com.meetwise.model.Data.Booking;
import com.meetwise.model.Data.MeetingRoom;
import com.meetwise.model.dao.BookingDAO;

public class AllocationService {

    private BookingDAO bookingDAO = new BookingDAO();

    // Find all suitable available rooms
    public List<MeetingRoom> findSuitableRooms(Booking booking) {

        if (booking == null
                || booking.getAttendees() <= 0
                || booking.getMeetingDate() == null
                || booking.getStartTime() == null
                || booking.getEndTime() == null
                || !booking.getEndTime().after(booking.getStartTime())) {

            return List.of();
        }

        return bookingDAO.getAvailableRooms(booking);
    }

    // Select the smallest suitable available room
    public MeetingRoom allocateBestRoom(Booking booking) {

        List<MeetingRoom> availableRooms =
                findSuitableRooms(booking);

        if (availableRooms.isEmpty()) {
            return null;
        }

        // Select the room with minimum sufficient capacity
        MeetingRoom bestRoom = availableRooms.get(0);

        for (MeetingRoom room : availableRooms) {

            if (room.getCapacity() < bestRoom.getCapacity()) {
                bestRoom = room;
            }
        }

        return bestRoom;
    }
}
