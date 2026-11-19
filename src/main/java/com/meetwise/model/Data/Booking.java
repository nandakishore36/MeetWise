
package com.meetwise.model.Data;

import java.sql.Date;
import java.sql.Time;

public class Booking {

    private int bookingId;
    private int teamId;
    private int roomId;
    private int bookedBy;

    private Date meetingDate;
    private Time startTime;
    private Time endTime;

    private int attendees;
    private String status;
    private String requiredFacilities;

    public Booking() {

    }

    public Booking(int bookingId, int teamId, int roomId,
                   int bookedBy, Date meetingDate,
                   Time startTime, Time endTime,
                   int attendees, String status,
                   String requiredFacilities) {

        this.bookingId = bookingId;
        this.teamId = teamId;
        this.roomId = roomId;
        this.bookedBy = bookedBy;
        this.meetingDate = meetingDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.attendees = attendees;
        this.status = status;
        this.requiredFacilities = requiredFacilities;
    }

    public int getBookingId() {
        return bookingId;
    }

    public void setBookingId(int bookingId) {
        this.bookingId = bookingId;
    }

    public int getTeamId() {
        return teamId;
    }

    public void setTeamId(int teamId) {
        this.teamId = teamId;
    }

    public int getRoomId() {
        return roomId;
    }

    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }

    public int getBookedBy() {
        return bookedBy;
    }

    public void setBookedBy(int bookedBy) {
        this.bookedBy = bookedBy;
    }

    public Date getMeetingDate() {
        return meetingDate;
    }

    public void setMeetingDate(Date meetingDate) {
        this.meetingDate = meetingDate;
    }

    public Time getStartTime() {
        return startTime;
    }

    public void setStartTime(Time startTime) {
        this.startTime = startTime;
    }

    public Time getEndTime() {
        return endTime;
    }

    public void setEndTime(Time endTime) {
        this.endTime = endTime;
    }

    public int getAttendees() {
        return attendees;
    }

    public void setAttendees(int attendees) {
        this.attendees = attendees;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRequiredFacilities() {
        return requiredFacilities;
    }

    public void setRequiredFacilities(String requiredFacilities) {
        this.requiredFacilities = requiredFacilities;
    }
}
