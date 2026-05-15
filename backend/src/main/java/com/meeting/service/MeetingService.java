package com.meeting.service;

import com.meeting.dto.ConflictResult;
import com.meeting.entity.Employee;
import com.meeting.entity.Meeting;
import com.meeting.entity.MeetingRoom;
import com.meeting.enums.MeetingPriority;
import com.meeting.enums.MeetingStatus;
import com.meeting.enums.RecurrenceType;
import com.meeting.store.DataStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class MeetingService {

    @Autowired
    private DataStore dataStore;

    public Meeting createMeeting(Meeting meeting) {
        MeetingRoom room = dataStore.meetingRooms.get(meeting.getRoomId());
        if (room == null) {
            throw new RuntimeException("会议室不存在");
        }

        if (meeting.getExpectedAttendees() > room.getCapacity()) {
            throw new RuntimeException("参会人数超过会议室容量，当前容量: " + room.getCapacity());
        }

        ConflictResult conflictResult = checkConflict(meeting);
        if (conflictResult.getHasConflict()) {
            if (conflictResult.getCanPreempt()) {
                throw new RuntimeException("存在冲突，但可以发起抢占: " + conflictResult.getMessage());
            }
            throw new RuntimeException("存在冲突: " + conflictResult.getMessage());
        }

        meeting.setId(dataStore.generateId());
        meeting.setStatus(MeetingStatus.SCHEDULED);
        meeting.setRoomName(room.getName());
        meeting.setCreatedAt(LocalDateTime.now());
        meeting.setUpdatedAt(LocalDateTime.now());

        if (meeting.getRecurrenceType() != RecurrenceType.NONE && meeting.getRecurrenceGroupId() == null) {
            createRecurrenceMeetings(meeting);
        } else {
            dataStore.meetings.put(meeting.getId(), meeting);
        }

        return meeting;
    }

    private void createRecurrenceMeetings(Meeting firstMeeting) {
        String groupId = dataStore.generateId();
        List<Meeting> groupMeetings = new ArrayList<>();

        LocalDateTime currentStart = firstMeeting.getStartTime();
        LocalDateTime currentEnd = firstMeeting.getEndTime();
        LocalDateTime recurrenceEnd = firstMeeting.getRecurrenceEndDate();

        while (!currentStart.isAfter(recurrenceEnd)) {
            Meeting meeting = new Meeting();
            meeting.setId(dataStore.generateId());
            meeting.setTitle(firstMeeting.getTitle());
            meeting.setOrganizerId(firstMeeting.getOrganizerId());
            meeting.setOrganizerName(firstMeeting.getOrganizerName());
            meeting.setRoomId(firstMeeting.getRoomId());
            meeting.setRoomName(firstMeeting.getRoomName());
            meeting.setStartTime(currentStart);
            meeting.setEndTime(currentEnd);
            meeting.setExpectedAttendees(firstMeeting.getExpectedAttendees());
            meeting.setAttendeeIds(firstMeeting.getAttendeeIds());
            meeting.setAttendeeNames(firstMeeting.getAttendeeNames());
            meeting.setPriority(firstMeeting.getPriority());
            meeting.setStatus(MeetingStatus.SCHEDULED);
            meeting.setDescription(firstMeeting.getDescription());
            meeting.setRecurrenceType(firstMeeting.getRecurrenceType());
            meeting.setRecurrenceGroupId(groupId);
            meeting.setRecurrenceEndDate(recurrenceEnd);
            meeting.setCreatedAt(LocalDateTime.now());
            meeting.setUpdatedAt(LocalDateTime.now());

            ConflictResult conflict = checkConflict(meeting);
            if (conflict.getHasConflict()) {
                meeting.setStatus(MeetingStatus.NEEDS_REBOOKING);
            }

            dataStore.meetings.put(meeting.getId(), meeting);
            groupMeetings.add(meeting);

            switch (firstMeeting.getRecurrenceType()) {
                case DAILY:
                    currentStart = currentStart.plusDays(1);
                    currentEnd = currentEnd.plusDays(1);
                    break;
                case WEEKLY:
                    currentStart = currentStart.plusWeeks(1);
                    currentEnd = currentEnd.plusWeeks(1);
                    break;
                case MONTHLY:
                    currentStart = currentStart.plusMonths(1);
                    currentEnd = currentEnd.plusMonths(1);
                    break;
                default:
                    return;
            }
        }

        dataStore.recurrenceGroups.put(groupId, groupMeetings);
    }

    public ConflictResult checkConflict(Meeting meeting) {
        ConflictResult result = new ConflictResult();
        List<Meeting> conflictingMeetings = new ArrayList<>();
        List<String> conflictingAttendees = new ArrayList<>();

        for (Meeting existing : dataStore.meetings.values()) {
            if (existing.getId() != null && existing.getId().equals(meeting.getId())) {
                continue;
            }
            if (existing.getRecurrenceGroupId() != null && meeting.getRecurrenceGroupId() != null
                && existing.getRecurrenceGroupId().equals(meeting.getRecurrenceGroupId())) {
                continue;
            }
            if (existing.getStatus() == MeetingStatus.CANCELLED ||
                existing.getStatus() == MeetingStatus.COMPLETED ||
                existing.getStatus() == MeetingStatus.PREEMPTED) {
                continue;
            }

            if (existing.getRoomId().equals(meeting.getRoomId())) {
                if (isTimeOverlap(meeting.getStartTime(), meeting.getEndTime(),
                    existing.getStartTime(), existing.getEndTime())) {
                    conflictingMeetings.add(existing);
                }
            }
        }

        for (String attendeeId : meeting.getAttendeeIds()) {
            for (Meeting existing : dataStore.meetings.values()) {
                if (existing.getId() != null && existing.getId().equals(meeting.getId())) {
                    continue;
                }
                if (existing.getStatus() == MeetingStatus.CANCELLED ||
                    existing.getStatus() == MeetingStatus.COMPLETED) {
                    continue;
                }
                if (existing.getAttendeeIds().contains(attendeeId)) {
                    if (isTimeOverlap(meeting.getStartTime(), meeting.getEndTime(),
                        existing.getStartTime(), existing.getEndTime())) {
                        Employee emp = dataStore.employees.get(attendeeId);
                        String empName = emp != null ? emp.getName() : attendeeId;
                        if (!conflictingAttendees.contains(empName)) {
                            conflictingAttendees.add(empName);
                        }
                    }
                }
            }
        }

        if (!conflictingMeetings.isEmpty() || !conflictingAttendees.isEmpty()) {
            result.setHasConflict(true);
            result.setConflictingMeetings(conflictingMeetings);
            result.setConflictingAttendees(conflictingAttendees);

            boolean canPreempt = true;
            for (Meeting conflict : conflictingMeetings) {
                if (conflict.getPriority().getLevel() >= meeting.getPriority().getLevel()) {
                    canPreempt = false;
                    break;
                }
            }
            result.setCanPreempt(canPreempt);

            StringBuilder sb = new StringBuilder();
            if (!conflictingMeetings.isEmpty()) {
                sb.append("会议室时间冲突: ").append(conflictingMeetings.size()).append("个会议; ");
            }
            if (!conflictingAttendees.isEmpty()) {
                sb.append("参会人冲突: ").append(conflictingAttendees);
            }
            result.setMessage(sb.toString());
        }

        return result;
    }

    private boolean isTimeOverlap(LocalDateTime start1, LocalDateTime end1,
                                  LocalDateTime start2, LocalDateTime end2) {
        return start1.isBefore(end2) && end1.isAfter(start2);
    }

    public Meeting preemptMeeting(Meeting newMeetingData, String preemptedMeetingId) {
        Meeting preemptedMeeting = dataStore.meetings.get(preemptedMeetingId);

        if (preemptedMeeting == null) {
            throw new RuntimeException("被抢占的会议不存在");
        }

        if (newMeetingData.getPriority().getLevel() <= preemptedMeeting.getPriority().getLevel()) {
            throw new RuntimeException("优先级不足，无法抢占");
        }

        preemptedMeeting.setStatus(MeetingStatus.PREEMPTED);
        preemptedMeeting.setUpdatedAt(LocalDateTime.now());
        dataStore.meetings.put(preemptedMeeting.getId(), preemptedMeeting);

        List<MeetingRoom> recommended = recommendRooms(preemptedMeeting);
        if (!recommended.isEmpty()) {
            preemptedMeeting.setStatus(MeetingStatus.NEEDS_REBOOKING);
            dataStore.meetings.put(preemptedMeeting.getId(), preemptedMeeting);
        }

        MeetingRoom room = dataStore.meetingRooms.get(newMeetingData.getRoomId());
        newMeetingData.setId(dataStore.generateId());
        newMeetingData.setStatus(MeetingStatus.SCHEDULED);
        newMeetingData.setRoomName(room != null ? room.getName() : "");
        newMeetingData.setCreatedAt(LocalDateTime.now());
        newMeetingData.setUpdatedAt(LocalDateTime.now());
        dataStore.meetings.put(newMeetingData.getId(), newMeetingData);

        return newMeetingData;
    }

    public List<MeetingRoom> recommendRooms(Meeting meeting) {
        List<MeetingRoom> available = new ArrayList<>();
        for (MeetingRoom room : dataStore.meetingRooms.values()) {
            if (room.getCapacity() >= meeting.getExpectedAttendees()) {
                boolean isAvailable = true;
                for (Meeting existing : dataStore.meetings.values()) {
                    if (existing.getStatus() == MeetingStatus.CANCELLED ||
                        existing.getStatus() == MeetingStatus.COMPLETED ||
                        existing.getStatus() == MeetingStatus.PREEMPTED) {
                        continue;
                    }
                    if (existing.getRoomId().equals(room.getId()) &&
                        isTimeOverlap(meeting.getStartTime(), meeting.getEndTime(),
                            existing.getStartTime(), existing.getEndTime())) {
                        isAvailable = false;
                        break;
                    }
                }
                if (isAvailable) {
                    available.add(room);
                }
            }
        }
        return available;
    }

    public Meeting updateMeeting(String meetingId, Meeting updatedMeeting, boolean updateWholeSeries) {
        Meeting existing = dataStore.meetings.get(meetingId);
        if (existing == null) {
            throw new RuntimeException("会议不存在");
        }

        if (updateWholeSeries && existing.getRecurrenceGroupId() != null) {
            List<Meeting> groupMeetings = dataStore.recurrenceGroups.get(existing.getRecurrenceGroupId());
            if (groupMeetings != null && !groupMeetings.isEmpty()) {
                Meeting firstMeeting = groupMeetings.get(0);
                long durationMinutes = java.time.Duration.between(firstMeeting.getStartTime(), firstMeeting.getEndTime()).toMinutes();
                long offsetMinutes = java.time.Duration.between(firstMeeting.getStartTime(), existing.getStartTime()).toMinutes();
                
                for (Meeting m : groupMeetings) {
                    Meeting updated = copyMeetingFields(updatedMeeting, m);
                    updated.setUpdatedAt(LocalDateTime.now());
                    
                    LocalDateTime originalStartTime = m.getStartTime();
                    LocalDateTime newBaseStartTime = updatedMeeting.getStartTime();
                    long currentOffsetMinutes = java.time.Duration.between(firstMeeting.getStartTime(), originalStartTime).toMinutes();
                    LocalDateTime newStartTime = newBaseStartTime.plusMinutes(currentOffsetMinutes - offsetMinutes);
                    LocalDateTime newEndTime = newStartTime.plusMinutes(durationMinutes);
                    
                    updated.setStartTime(newStartTime);
                    updated.setEndTime(newEndTime);
                    
                    ConflictResult conflict = checkConflict(updated);
                    if (conflict.getHasConflict()) {
                        if (conflict.getCanPreempt()) {
                            for (Meeting conflictMeeting : conflict.getConflictingMeetings()) {
                                if (!conflictMeeting.getRecurrenceGroupId().equals(m.getRecurrenceGroupId())) {
                                    conflictMeeting.setStatus(MeetingStatus.NEEDS_REBOOKING);
                                    dataStore.meetings.put(conflictMeeting.getId(), conflictMeeting);
                                }
                            }
                        } else {
                            updated.setStatus(MeetingStatus.NEEDS_REBOOKING);
                        }
                    }
                    dataStore.meetings.put(m.getId(), updated);
                }
            }
            return existing;
        }

        Meeting updated = copyMeetingFields(updatedMeeting, existing);
        updated.setUpdatedAt(LocalDateTime.now());

        ConflictResult conflict = checkConflict(updated);
        if (conflict.getHasConflict()) {
            if (!conflict.getCanPreempt()) {
                throw new RuntimeException("更新后存在冲突: " + conflict.getMessage());
            }
        }

        dataStore.meetings.put(meetingId, updated);
        return updated;
    }

    private Meeting copyMeetingFields(Meeting source, Meeting target) {
        target.setTitle(source.getTitle());
        target.setRoomId(source.getRoomId());
        target.setStartTime(source.getStartTime());
        target.setEndTime(source.getEndTime());
        target.setExpectedAttendees(source.getExpectedAttendees());
        target.setAttendeeIds(source.getAttendeeIds());
        target.setAttendeeNames(source.getAttendeeNames());
        target.setPriority(source.getPriority());
        target.setDescription(source.getDescription());
        return target;
    }

    public void cancelMeeting(String meetingId) {
        Meeting meeting = dataStore.meetings.get(meetingId);
        if (meeting == null) {
            throw new RuntimeException("会议不存在");
        }
        meeting.setStatus(MeetingStatus.CANCELLED);
        meeting.setUpdatedAt(LocalDateTime.now());
        dataStore.meetings.put(meetingId, meeting);
    }

    public Meeting confirmAttendance(String meetingId, int confirmedCount) {
        Meeting meeting = dataStore.meetings.get(meetingId);
        if (meeting == null) {
            throw new RuntimeException("会议不存在");
        }
        meeting.setConfirmedAttendees(confirmedCount);
        meeting.setAttendanceConfirmed(true);
        meeting.setUpdatedAt(LocalDateTime.now());
        dataStore.meetings.put(meetingId, meeting);
        return meeting;
    }

    public void releaseRoomIfNeeded() {
        LocalDateTime now = LocalDateTime.now();
        for (Meeting meeting : dataStore.meetings.values()) {
            if (meeting.getStatus() != MeetingStatus.SCHEDULED) {
                continue;
            }
            if (meeting.getStartTime().isBefore(now.plusMinutes(30)) &&
                meeting.getStartTime().isAfter(now)) {
                if (!meeting.getAttendanceConfirmed() ||
                    meeting.getConfirmedAttendees() < meeting.getExpectedAttendees() / 2) {
                    meeting.setStatus(MeetingStatus.CANCELLED);
                    meeting.setUpdatedAt(LocalDateTime.now());
                    dataStore.meetings.put(meeting.getId(), meeting);
                }
            }
        }
    }

    public List<Meeting> getUpcomingMeetings(int minutes) {
        LocalDateTime now = LocalDateTime.now();
        return dataStore.meetings.values().stream()
            .filter(m -> m.getStatus() == MeetingStatus.SCHEDULED ||
                m.getStatus() == MeetingStatus.CONFIRMED)
            .filter(m -> m.getStartTime().isAfter(now) &&
                m.getStartTime().isBefore(now.plusMinutes(minutes)))
            .collect(Collectors.toList());
    }

    public List<Meeting> getAllMeetings() {
        return new ArrayList<>(dataStore.meetings.values());
    }

    public Meeting getMeeting(String id) {
        return dataStore.meetings.get(id);
    }

    public List<MeetingRoom> getAllRooms() {
        return new ArrayList<>(dataStore.meetingRooms.values());
    }

    public List<Employee> getAllEmployees() {
        return new ArrayList<>(dataStore.employees.values());
    }
}
