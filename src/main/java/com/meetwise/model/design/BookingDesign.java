package com.meetwise.model.design;

import java.util.List;

import com.meetwise.model.Data.Booking;
import com.meetwise.model.Data.MeetingRoom;

public interface BookingDesign {

    List<MeetingRoom> getAvailableRooms(Booking booking);

    MeetingRoom allocateRoom(Booking booking);

    boolean createBooking(Booking booking);

    boolean cancelBooking(int bookingId, int userId);

    List<Booking> getBookingsByTeam(int teamId);

    List<Booking> getRoomBookings(int roomId);

}
