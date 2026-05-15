package com.meeting.dto;

import com.meeting.entity.Meeting;

import java.util.List;

public class ConflictResult {
    private Boolean hasConflict = false;
    private List<Meeting> conflictingMeetings;
    private List<String> conflictingAttendees;
    private String message;
    private Boolean canPreempt = false;

    public ConflictResult() {}

    public ConflictResult(Boolean hasConflict, List<Meeting> conflictingMeetings, List<String> conflictingAttendees, String message, Boolean canPreempt) {
        this.hasConflict = hasConflict;
        this.conflictingMeetings = conflictingMeetings;
        this.conflictingAttendees = conflictingAttendees;
        this.message = message;
        this.canPreempt = canPreempt;
    }

    public Boolean getHasConflict() { return hasConflict; }
    public void setHasConflict(Boolean hasConflict) { this.hasConflict = hasConflict; }
    public List<Meeting> getConflictingMeetings() { return conflictingMeetings; }
    public void setConflictingMeetings(List<Meeting> conflictingMeetings) { this.conflictingMeetings = conflictingMeetings; }
    public List<String> getConflictingAttendees() { return conflictingAttendees; }
    public void setConflictingAttendees(List<String> conflictingAttendees) { this.conflictingAttendees = conflictingAttendees; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public Boolean getCanPreempt() { return canPreempt; }
    public void setCanPreempt(Boolean canPreempt) { this.canPreempt = canPreempt; }
}
