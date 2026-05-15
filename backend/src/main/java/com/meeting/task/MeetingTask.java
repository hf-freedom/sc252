package com.meeting.task;

import com.meeting.entity.Meeting;
import com.meeting.service.MeetingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MeetingTask {

    private static final Logger logger = LoggerFactory.getLogger(MeetingTask.class);

    @Autowired
    private MeetingService meetingService;

    @Scheduled(fixedRate = 60000)
    public void checkUpcomingMeetings() {
        List<Meeting> upcoming = meetingService.getUpcomingMeetings(30);
        if (!upcoming.isEmpty()) {
            logger.info("即将开始的会议: {} 个", upcoming.size());
            for (Meeting meeting : upcoming) {
                logger.info(" - {} 将在 {} 开始", meeting.getTitle(), meeting.getStartTime());
            }
        }
    }

    @Scheduled(fixedRate = 300000)
    public void checkAndReleaseRooms() {
        meetingService.releaseRoomIfNeeded();
        logger.info("已检查会议室释放情况");
    }
}
