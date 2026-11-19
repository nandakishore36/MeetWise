package com.meetwise.controller;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.meetwise.model.Data.MeetingRoom;
import com.meetwise.model.service.RoomService;

@WebServlet("/rooms")
public class RoomController extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private RoomService roomService = new RoomService();

    // View all rooms
    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
                         throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        if (!isAdmin(request, response)) {
            return;
        }

        try {
            List<MeetingRoom> rooms = roomService.getAllRooms();

            StringBuilder json = new StringBuilder();
            json.append("{\"rooms\":[");

            for (int i = 0; i < rooms.size(); i++) {

                MeetingRoom room = rooms.get(i);

                if (i > 0) {
                    json.append(",");
                }

                json.append("{");
                json.append("\"roomId\":").append(room.getRoomId()).append(",");
                json.append("\"roomName\":\"").append(escapeJson(room.getRoomName())).append("\",");
                json.append("\"capacity\":").append(room.getCapacity()).append(",");
                json.append("\"location\":\"").append(escapeJson(room.getLocation())).append("\",");
                json.append("\"facilities\":\"").append(escapeJson(room.getFacilities())).append("\",");
                json.append("\"status\":\"").append(escapeJson(room.getStatus())).append("\"");
                json.append("}");
            }

            json.append("]}");

            response.getWriter().write(json.toString());

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\":\"Unable to retrieve rooms.\"}");
            e.printStackTrace();
        }
    }

    // Add, update or deactivate rooms
    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
                          throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        if (!isAdmin(request, response)) {
            return;
        }

        try {

            int adminId = (Integer) request.getSession(false)
                                           .getAttribute("userId");

            String action = request.getParameter("action");

            if (action == null || action.trim().isEmpty()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("{\"error\":\"Action is required.\"}");
                return;
            }

            boolean result = false;

            switch (action.toLowerCase()) {

                case "add":

                    MeetingRoom newRoom = readRoomDetails(request);

                    result = roomService.addRoom(newRoom, adminId);
                    break;

                case "update":

                    String updateId = request.getParameter("roomId");

                    if (updateId == null || updateId.trim().isEmpty()) {
                        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        response.getWriter().write("{\"error\":\"Room ID is required.\"}");
                        return;
                    }

                    MeetingRoom updatedRoom = readRoomDetails(request);
                    updatedRoom.setRoomId(Integer.parseInt(updateId));

                    result = roomService.updateRoom(updatedRoom, adminId);
                    break;

                case "deactivate":

                    String deactivateId = request.getParameter("roomId");

                    if (deactivateId == null || deactivateId.trim().isEmpty()) {
                        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        response.getWriter().write("{\"error\":\"Room ID is required.\"}");
                        return;
                    }

                    result = roomService.deactivateRoom(
                        Integer.parseInt(deactivateId), adminId
                    );
                    break;

                default:

                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    response.getWriter().write("{\"error\":\"Invalid action.\"}");
                    return;
            }

            if (result) {
                response.getWriter().write("{\"message\":\"Room operation successful.\"}");
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("{\"error\":\"Room operation failed.\"}");
            }

        } catch (NumberFormatException e) {

            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"error\":\"Invalid numeric value.\"}");

        } catch (Exception e) {

            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\":\"Unable to process room operation.\"}");
            e.printStackTrace();
        }
    }

    // Read room details from request
    private MeetingRoom readRoomDetails(HttpServletRequest request) {

        String name = request.getParameter("roomName");
        String capacity = request.getParameter("capacity");
        String location = request.getParameter("location");
        String facilities = request.getParameter("facilities");

        if (name == null || capacity == null ||
            location == null || facilities == null) {
            throw new IllegalArgumentException("Missing room details.");
        }

        MeetingRoom room = new MeetingRoom();

        room.setRoomName(name.trim());
        room.setCapacity(Integer.parseInt(capacity));
        room.setLocation(location.trim());
        room.setFacilities(facilities.trim());

        return room;
    }

    // Check whether the logged-in user is an Admin
    private boolean isAdmin(HttpServletRequest request,
                            HttpServletResponse response) throws IOException {

        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("userId") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"error\":\"Please login first.\"}");
            return false;
        }

        if (!"ADMIN".equals(session.getAttribute("userRole"))) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().write("{\"error\":\"Admin access required.\"}");
            return false;
        }

        return true;
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
