package com.meeting.enums;

public enum MeetingPriority {
    LOW(1),
    NORMAL(2),
    HIGH(3),
    URGENT(4);

    private int level;

    MeetingPriority(int level) {
        this.level = level;
    }

    public int getLevel() {
        return level;
    }
}
