package utils;

import java.time.format.DateTimeFormatter;

public final class DateFormats {

    public static final String DATE_FORMAT_PATTERN = "dd/MM/yyyy";
    public static final String DATE_TIME_FORMAT_PATTERN = "dd/MM/yyyy HH:mm";
    public static final String TIME_FORMAT_PATTERN = "dd/MM/yyyy HH:mm";

    public static final DateTimeFormatter DATE_FMT  = DateTimeFormatter.ofPattern(DATE_FORMAT_PATTERN);
    public static final DateTimeFormatter DATETIME_FMT = DateTimeFormatter.ofPattern(DATE_TIME_FORMAT_PATTERN);
    public static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern(TIME_FORMAT_PATTERN);

    private DateFormats() {
    }
}