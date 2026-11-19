package com.meetwise.model.design;

import java.util.List;

import com.meetwise.model.Data.MeetingRoom;

public interface RoomDesign {

    boolean addRoom(MeetingRoom room, int adminId);

    List<MeetingRoom> getAllRooms();

    MeetingRoom getRoomById(int roomId);

    boolean updateRoom(MeetingRoom room, int adminId);

    boolean deactivateRoom(int roomId, int adminId);

}
