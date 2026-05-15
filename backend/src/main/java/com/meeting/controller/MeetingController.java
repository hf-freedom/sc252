package com.meeting.controller;

import com.meeting.dto.ApiResponse;
import com.meeting.dto.ConflictResult;
import com.meeting.entity.Employee;
import com.meeting.entity.Meeting;
import com.meeting.entity.MeetingRoom;
import com.meeting.service.MeetingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/meetings")
@CrossOrigin(origins = "*")
public class MeetingController {

    @Autowired
    private MeetingService meetingService;

    @PostMapping
    public ApiResponse<Meeting> createMeeting(@RequestBody Meeting meeting) {
        try {
            Meeting created = meetingService.createMeeting(meeting);
            return ApiResponse.success("创建成功", created);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @PostMapping("/check-conflict")
    public ApiResponse<ConflictResult> checkConflict(@RequestBody Meeting meeting) {
        try {
            ConflictResult result = meetingService.checkConflict(meeting);
            return ApiResponse.success(result);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @PostMapping("/preempt")
    public ApiResponse<Meeting> preemptMeeting(@RequestBody Meeting newMeeting,
                                               @RequestParam String preemptedMeetingId) {
        try {
            Meeting result = meetingService.preemptMeeting(newMeeting, preemptedMeetingId);
            return ApiResponse.success("抢占成功", result);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @PostMapping("/{id}/recommend-rooms")
    public ApiResponse<List<MeetingRoom>> recommendRooms(@PathVariable String id) {
        try {
            Meeting meeting = meetingService.getMeeting(id);
            if (meeting == null) {
                return ApiResponse.error("会议不存在");
            }
            List<MeetingRoom> rooms = meetingService.recommendRooms(meeting);
            return ApiResponse.success(rooms);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ApiResponse<Meeting> updateMeeting(@PathVariable String id,
                                              @RequestBody Meeting meeting,
                                              @RequestParam(defaultValue = "false") boolean updateWholeSeries) {
        try {
            Meeting updated = meetingService.updateMeeting(id, meeting, updateWholeSeries);
            return ApiResponse.success("更新成功", updated);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> cancelMeeting(@PathVariable String id) {
        try {
            meetingService.cancelMeeting(id);
            return ApiResponse.success("取消成功", null);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @PostMapping("/{id}/confirm-attendance")
    public ApiResponse<Meeting> confirmAttendance(@PathVariable String id,
                                                  @RequestParam int confirmedCount) {
        try {
            Meeting meeting = meetingService.confirmAttendance(id, confirmedCount);
            return ApiResponse.success("确认成功", meeting);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @GetMapping
    public ApiResponse<List<Meeting>> getAllMeetings() {
        return ApiResponse.success(meetingService.getAllMeetings());
    }

    @GetMapping("/{id}")
    public ApiResponse<Meeting> getMeeting(@PathVariable String id) {
        Meeting meeting = meetingService.getMeeting(id);
        if (meeting == null) {
            return ApiResponse.error("会议不存在");
        }
        return ApiResponse.success(meeting);
    }

    @GetMapping("/upcoming")
    public ApiResponse<List<Meeting>> getUpcomingMeetings(@RequestParam(defaultValue = "60") int minutes) {
        return ApiResponse.success(meetingService.getUpcomingMeetings(minutes));
    }

    @PostMapping("/check-release")
    public ApiResponse<String> checkAndReleaseRooms() {
        meetingService.releaseRoomIfNeeded();
        return ApiResponse.success("已检查并释放人数不足的会议室");
    }
}
