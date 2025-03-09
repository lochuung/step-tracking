package hcmute.edu.vn.huuloc.steptracking.util;

import androidx.room.TypeConverter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class DateConverter {
    private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;
    
    @TypeConverter
    public static LocalDate fromString(String value) {
        return value == null ? null : LocalDate.parse(value, formatter);
    }

    @TypeConverter
    public static String localDateToString(LocalDate date) {
        return date == null ? null : date.format(formatter);
    }
}
