package com.parking.vault_service.utils;

import com.parking.vault_service.exception.AppException;
import com.parking.vault_service.exception.ErrorCode;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;

public class TimeUtils {

    public static String convertTimestampToString(long timestamp, String format) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
        LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault());
        return dateTime.format(formatter);
    }

    public static String convertTime(long timeInMillis, String format) {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        Date date = new Date(timeInMillis);
        return sdf.format(date);
    }

    public static long timeToLong(String time, String format) {
        String[] split = time.split("/");
        if (split[0].length() == 1) {
            split[0] = "0" + split[0];
        }

        if (split[1].length() == 1) {
            split[1] = "0" + split[1];
        }

        time = split[0] + "/" + split[1] + "/" + split[2];
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern(format);
        try {
            LocalDate date = LocalDate.parse(time, dtf);
            return date.atStartOfDay(ZoneId.of("Asia/Ho_Chi_Minh")).toInstant().toEpochMilli();
        } catch (DateTimeParseException e) {
            e.printStackTrace();
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public static long getStartOfMonth(String dateString) {
        String[] split = dateString.split("/");
        if (split[0].length() == 1) {
            split[0] = "0" + split[0];
        }

        if (split[1].length() == 1) {
            split[1] = "0" + split[1];
        }

        dateString = split[0] + "/" + split[1] + "/" + split[2];
        LocalDate date = LocalDate.parse(dateString, DATE_FORMATTER);
        LocalDateTime startOfDay = date.withDayOfMonth(1).atStartOfDay();
        return startOfDay.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    public static long getStartOfDay(String dateString) {
        String[] split = dateString.split("/");
        if (split[0].length() == 1) {
            split[0] = "0" + split[0];
        }

        if (split[1].length() == 1) {
            split[1] = "0" + split[1];
        }

        dateString = split[0] + "/" + split[1] + "/" + split[2];
        LocalDate date = LocalDate.parse(dateString, DATE_FORMATTER);
        LocalDateTime startOfDay = date.atStartOfDay();
        return startOfDay.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }


    public static long getEndOfDay(String dateString) {
        String[] split = dateString.split("/");
        if (split[0].length() == 1) {
            split[0] = "0" + split[0];
        }

        if (split[1].length() == 1) {
            split[1] = "0" + split[1];
        }

        dateString = split[0] + "/" + split[1] + "/" + split[2];

        LocalDate date = LocalDate.parse(dateString, DATE_FORMATTER);
        LocalDateTime endOfDay = date.atTime(23, 59, 59, 999000000);
        return endOfDay.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();

    }
}
