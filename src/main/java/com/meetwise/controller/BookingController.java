package com.meetwise.controller;

import java.io.IOException;
import java.sql.Date;
import java.sql.Time;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.meetwise.model.Data.Booking;
import com.meetwise.model.service.BookingService;

@WebServlet("/book")
public class BookingController extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private BookingService bookingService = new BookingService();

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
                          throws ServletException, IOException {

        response.setContentType("text/plain");
        response.setCharacterEncoding("UTF-8");

        // Check user session
        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("userId") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().println("Please login first.");
            return;
        }

        // Only Team Leads can create bookings
        String role = (String) session.getAttribute("userRole");

        if (!"TEAM_LEAD".equals(role)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().println("Only Team Leads can book rooms.");
            return;
        }

        try {

            // Get logged-in user details from session
            int userId = (Integer) session.getAttribute("userId");
            int teamId = (Integer) session.getAttribute("teamId");

            // Receive booking details from frontend
            String meetingDate = request.getParameter("meetingDate");
            String startTime = request.getParameter("startTime");
            String endTime = request.getParameter("endTime");
            String attendees = request.getParameter("attendees");
            String facilities = request.getParameter("requiredFacilities");

            // Validate empty fields
            if (meetingDate == null || startTime == null ||
                endTime == null || attendees == null ||
                facilities == null) {

                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().println("Please provide all booking details.");
                return;
            }

            // Create Booking object
            Booking booking = new Booking();

            booking.setBookedBy(userId);
            booking.setTeamId(teamId);
            booking.setMeetingDate(Date.valueOf(meetingDate));
            booking.setStartTime(parseTime(startTime));
            booking.setEndTime(parseTime(endTime));
            booking.setAttendees(Integer.parseInt(attendees));
            booking.setRequiredFacilities(facilities.trim());

            // Create booking through service layer
            boolean result = bookingService.createBooking(booking);

            if (result) {
                response.getWriter().println("Booking created successfully!");
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().println(
                    "Booking failed. No suitable room may be available or the details may be invalid."
                );
            }

        } catch (IllegalArgumentException e) {

            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("Invalid date, time or attendee value.");

        } catch (Exception e) {

            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().println("Something went wrong while creating the booking.");
            e.printStackTrace();
        }
    }

    // Convert HTML time input into SQL Time
    private Time parseTime(String value) {

        if (value.length() == 5) {
            value = value + ":00";
        }

        return Time.valueOf(value);
    }
}
