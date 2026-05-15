package com.meeting.controller;

import com.meeting.dto.ApiResponse;
import com.meeting.entity.MeetingRoom;
import com.meeting.service.MeetingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rooms")
@CrossOrigin(origins = "*")
public class RoomController {

    @Autowired
    private MeetingService meetingService;

    @GetMapping
    public ApiResponse<List<MeetingRoom>> getAllRooms() {
        return ApiResponse.success(meetingService.getAllRooms());
    }
}
