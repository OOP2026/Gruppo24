package gui.components;

import com.toedter.calendar.JDateChooser;

import javax.swing.*;
import java.awt.*;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

import static utils.DateFormats.DATE_FORMAT_PATTERN;
import static utils.TimeZones.EUROPE_ROME;

public class DatePickerField extends JPanel {

    private static final ZoneId ZONE = ZoneId.of(EUROPE_ROME);

    private final JDateChooser dateChooser;

    public DatePickerField() {
        this(null);
    }

    public DatePickerField(LocalDate initial) {
        super(new FlowLayout(FlowLayout.LEFT, 0, 0));
        dateChooser = new JDateChooser();
        dateChooser.setDateFormatString(DATE_FORMAT_PATTERN);
        dateChooser.setPreferredSize(new Dimension(120, 26));
        if (initial != null) {
            setLocalDate(initial);
        }
        add(dateChooser);
    }

    public LocalDate getLocalDate() {
        Date date = dateChooser.getDate();
        if (date == null) {
            return null;
        }
        return Instant.ofEpochMilli(date.getTime()).atZone(ZONE).toLocalDate();
    }

    public void setLocalDate(LocalDate date) {
        if (date == null) {
            dateChooser.setDate(null);
            return;
        }
        dateChooser.setDate(Date.from(date.atStartOfDay(ZONE).toInstant()));
    }

    public void clear() {
        dateChooser.setDate(null);
    }

    public void addChangeListener(Runnable listener) {
        dateChooser.addPropertyChangeListener("date", e -> listener.run());
    }
}
