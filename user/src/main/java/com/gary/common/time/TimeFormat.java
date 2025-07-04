package com.gary.common.time;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public  class TimeFormat {
    public static String nowTimestamp() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"));
    }
}
