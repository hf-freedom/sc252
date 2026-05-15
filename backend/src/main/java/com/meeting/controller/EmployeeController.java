package com.meeting.controller;

import com.meeting.dto.ApiResponse;
import com.meeting.entity.Employee;
import com.meeting.service.MeetingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/employees")
@CrossOrigin(origins = "*")
public class EmployeeController {

    @Autowired
    private MeetingService meetingService;

    @GetMapping
    public ApiResponse<List<Employee>> getAllEmployees() {
        return ApiResponse.success(meetingService.getAllEmployees());
    }
}
