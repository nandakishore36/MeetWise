package com.meetwise.controller;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.meetwise.model.service.BookingService;

@WebServlet("/cancelBooking")
public class CancelBookingController extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private BookingService bookingService = new BookingService();

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
                          throws ServletException, IOException {

        response.setContentType("text/plain");
        response.setCharacterEncoding("UTF-8");

        // Check login session
        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("userId") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().println("Please login first.");
            return;
        }

        // Only Admin and Team Lead can cancel bookings
        String role = (String) session.getAttribute("userRole");

        if (!"ADMIN".equals(role) && !"TEAM_LEAD".equals(role)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().println(
                "Employees are not allowed to cancel bookings."
            );
            return;
        }

        try {

            int userId = (Integer) session.getAttribute("userId");

            String bookingIdValue = request.getParameter("bookingId");

            if (bookingIdValue == null ||
                bookingIdValue.trim().isEmpty()) {

                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().println("Booking ID is required.");
                return;
            }

            int bookingId = Integer.parseInt(bookingIdValue);

            if (bookingId <= 0) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().println("Invalid booking ID.");
                return;
            }

            boolean result =
                bookingService.cancelBooking(bookingId, userId);

            if (result) {

                response.getWriter().println(
                    "Booking cancelled successfully."
                );

            } else {

                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.getWriter().println(
                    "Cancellation failed. You may not have permission, or the booking may already be cancelled."
                );
            }

        } catch (NumberFormatException e) {

            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("Invalid booking ID.");

        } catch (Exception e) {

            response.setStatus(
                HttpServletResponse.SC_INTERNAL_SERVER_ERROR
            );

            response.getWriter().println(
                "Something went wrong while cancelling the booking."
            );

            e.printStackTrace();
        }
    }
}
