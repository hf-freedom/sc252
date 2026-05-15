package com.meeting.entity;

import com.meeting.enums.MeetingPriority;
import com.meeting.enums.MeetingStatus;
import com.meeting.enums.RecurrenceType;

import java.time.LocalDateTime;
import java.util.List;

public class Meeting {
    private String id;
    private String title;
    private String organizerId;
    private String organizerName;
    private String roomId;
    private String roomName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer expectedAttendees;
    private Integer confirmedAttendees = 0;
    private List<String> attendeeIds;
    private List<String> attendeeNames;
    private MeetingPriority priority;
    private MeetingStatus status;
    private String description;
    private RecurrenceType recurrenceType = RecurrenceType.NONE;
    private String recurrenceGroupId;
    private LocalDateTime recurrenceEndDate;
    private Boolean attendanceConfirmed = false;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Meeting() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getOrganizerId() { return organizerId; }
    public void setOrganizerId(String organizerId) { this.organizerId = organizerId; }
    public String getOrganizerName() { return organizerName; }
    public void setOrganizerName(String organizerName) { this.organizerName = organizerName; }
    public String getRoomId() { return roomId; }
    public void setRoomId(String roomId) { this.roomId = roomId; }
    public String getRoomName() { return roomName; }
    public void setRoomName(String roomName) { this.roomName = roomName; }
    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
    public Integer getExpectedAttendees() { return expectedAttendees; }
    public void setExpectedAttendees(Integer expectedAttendees) { this.expectedAttendees = expectedAttendees; }
    public Integer getConfirmedAttendees() { return confirmedAttendees; }
    public void setConfirmedAttendees(Integer confirmedAttendees) { this.confirmedAttendees = confirmedAttendees; }
    public List<String> getAttendeeIds() { return attendeeIds; }
    public void setAttendeeIds(List<String> attendeeIds) { this.attendeeIds = attendeeIds; }
    public List<String> getAttendeeNames() { return attendeeNames; }
    public void setAttendeeNames(List<String> attendeeNames) { this.attendeeNames = attendeeNames; }
    public MeetingPriority getPriority() { return priority; }
    public void setPriority(MeetingPriority priority) { this.priority = priority; }
    public MeetingStatus getStatus() { return status; }
    public void setStatus(MeetingStatus status) { this.status = status; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public RecurrenceType getRecurrenceType() { return recurrenceType; }
    public void setRecurrenceType(RecurrenceType recurrenceType) { this.recurrenceType = recurrenceType; }
    public String getRecurrenceGroupId() { return recurrenceGroupId; }
    public void setRecurrenceGroupId(String recurrenceGroupId) { this.recurrenceGroupId = recurrenceGroupId; }
    public LocalDateTime getRecurrenceEndDate() { return recurrenceEndDate; }
    public void setRecurrenceEndDate(LocalDateTime recurrenceEndDate) { this.recurrenceEndDate = recurrenceEndDate; }
    public Boolean getAttendanceConfirmed() { return attendanceConfirmed; }
    public void setAttendanceConfirmed(Boolean attendanceConfirmed) { this.attendanceConfirmed = attendanceConfirmed; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
