package com.meeting.store;

import com.meeting.entity.Employee;
import com.meeting.entity.Meeting;
import com.meeting.entity.MeetingRoom;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class DataStore {
    public final Map<String, MeetingRoom> meetingRooms = new ConcurrentHashMap<>();
    public final Map<String, Meeting> meetings = new ConcurrentHashMap<>();
    public final Map<String, Employee> employees = new ConcurrentHashMap<>();
    public final Map<String, List<Meeting>> recurrenceGroups = new ConcurrentHashMap<>();

    public DataStore() {
        initData();
    }

    private void initData() {
        meetingRooms.put("room1", new MeetingRoom("room1", "会议室A", 10, "1楼", "投影仪、白板", true));
        meetingRooms.put("room2", new MeetingRoom("room2", "会议室B", 20, "2楼", "投影仪、视频会议", true));
        meetingRooms.put("room3", new MeetingRoom("room3", "会议室C", 5, "1楼", "白板", true));
        meetingRooms.put("room4", new MeetingRoom("room4", "大会议室", 50, "3楼", "投影仪、音响、视频会议", true));
        meetingRooms.put("room5", new MeetingRoom("room5", "小型讨论室", 4, "2楼", "电视", true));

        employees.put("emp1", new Employee("emp1", "张三", "zhangsan@company.com", "技术部", true));
        employees.put("emp2", new Employee("emp2", "李四", "lisi@company.com", "产品部", true));
        employees.put("emp3", new Employee("emp3", "王五", "wangwu@company.com", "市场部", true));
        employees.put("emp4", new Employee("emp4", "赵六", "zhaoliu@company.com", "人事部", true));
        employees.put("emp5", new Employee("emp5", "钱七", "qianqi@company.com", "财务部", true));
    }

    public String generateId() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
