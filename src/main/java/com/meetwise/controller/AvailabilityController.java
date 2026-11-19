package com.meetwise.controller;

import java.io.IOException;
import java.sql.Date;
import java.sql.Time;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.meetwise.model.Data.Booking;
import com.meetwise.model.Data.MeetingRoom;
import com.meetwise.model.service.BookingService;

@WebServlet("/availability")
public class AvailabilityController extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private BookingService bookingService = new BookingService();

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
                          throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        // Check login session
        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("userId") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write(
                "{\"error\":\"Please login first.\"}"
            );
            return;
        }

        try {

            // Get details from frontend
            String meetingDate = request.getParameter("meetingDate");
            String startTime = request.getParameter("startTime");
            String endTime = request.getParameter("endTime");
            String attendees = request.getParameter("attendees");
            String facilities = request.getParameter("requiredFacilities");

            // Validate input
            if (meetingDate == null || startTime == null ||
                endTime == null || attendees == null ||
                facilities == null) {

                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write(
                    "{\"error\":\"Please provide all required details.\"}"
                );
                return;
            }

            // Create Booking object for availability search
            Booking booking = new Booking();

            booking.setMeetingDate(Date.valueOf(meetingDate));
            booking.setStartTime(parseTime(startTime));
            booking.setEndTime(parseTime(endTime));
            booking.setAttendees(Integer.parseInt(attendees));
            booking.setRequiredFacilities(facilities.trim());

            // Get available rooms
            List<MeetingRoom> rooms =
                bookingService.getAvailableRooms(booking);

            // Build JSON response
            StringBuilder json = new StringBuilder();

            json.append("{\"rooms\":[");

            for (int i = 0; i < rooms.size(); i++) {

                MeetingRoom room = rooms.get(i);

                if (i > 0) {
                    json.append(",");
                }

                json.append("{");
                json.append("\"roomId\":").append(room.getRoomId()).append(",");
                json.append("\"roomName\":\"")
                    .append(escapeJson(room.getRoomName())).append("\",");
                json.append("\"capacity\":").append(room.getCapacity()).append(",");
                json.append("\"location\":\"")
                    .append(escapeJson(room.getLocation())).append("\",");
                json.append("\"facilities\":\"")
                    .append(escapeJson(room.getFacilities())).append("\"");
                json.append("}");
            }

            json.append("]}");

            response.getWriter().write(json.toString());

        } catch (IllegalArgumentException e) {

            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write(
                "{\"error\":\"Invalid date, time or attendee value.\"}"
            );

        } catch (Exception e) {

            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write(
                "{\"error\":\"Unable to check room availability.\"}"
            );

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

    // Escape special characters before adding values to JSON
    private String escapeJson(String value) {

        if (value == null) {
            return "";
        }

        return value.replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\n", "\\n")
                    .replace("\r", "\\r")
                    .replace("\t", "\\t");
    }
}
