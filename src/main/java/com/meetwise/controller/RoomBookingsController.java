package com.meetwise.controller;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.meetwise.model.Data.Booking;
import com.meetwise.model.service.BookingService;

@WebServlet("/roomBookings")
public class RoomBookingsController extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private BookingService bookingService = new BookingService();

    @Override
    protected void doGet(HttpServletRequest request,
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

            // Get room ID from request
            String roomIdValue = request.getParameter("roomId");

            if (roomIdValue == null || roomIdValue.trim().isEmpty()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write(
                    "{\"error\":\"Room ID is required.\"}"
                );
                return;
            }

            int roomId = Integer.parseInt(roomIdValue);

            if (roomId <= 0) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write(
                    "{\"error\":\"Invalid room ID.\"}"
                );
                return;
            }

            // Retrieve room bookings
            List<Booking> bookings =
                bookingService.getRoomBookings(roomId);

            // Build JSON response
            StringBuilder json = new StringBuilder();

            json.append("{\"bookings\":[");

            for (int i = 0; i < bookings.size(); i++) {

                Booking booking = bookings.get(i);

                if (i > 0) {
                    json.append(",");
                }

                json.append("{");

                json.append("\"bookingId\":")
                    .append(booking.getBookingId()).append(",");

                json.append("\"teamId\":")
                    .append(booking.getTeamId()).append(",");

                json.append("\"meetingDate\":\"")
                    .append(booking.getMeetingDate()).append("\",");

                json.append("\"startTime\":\"")
                    .append(booking.getStartTime()).append("\",");

                json.append("\"endTime\":\"")
                    .append(booking.getEndTime()).append("\",");

                json.append("\"attendees\":")
                    .append(booking.getAttendees()).append(",");

                json.append("\"status\":\"")
                    .append(escapeJson(booking.getStatus())).append("\"");

                json.append("}");
            }

            json.append("]}");

            response.getWriter().write(json.toString());

        } catch (NumberFormatException e) {

            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write(
                "{\"error\":\"Room ID must be a valid number.\"}"
            );

        } catch (Exception e) {

            response.setStatus(
                HttpServletResponse.SC_INTERNAL_SERVER_ERROR
            );

            response.getWriter().write(
                "{\"error\":\"Unable to retrieve room bookings.\"}"
            );

            e.printStackTrace();
        }
    }

    // Escape special characters for JSON
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
