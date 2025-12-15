package org.example.demo_ssr_v1_1._core.utils;


import org.springframework.format.annotation.DateTimeFormat;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class MyDateUtil {

    // 정적 메서드 (기능) 시간 포맷터 기능
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static String timestamp() {

        return null;
    }
}
