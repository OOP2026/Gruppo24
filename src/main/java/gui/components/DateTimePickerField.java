package gui.components;

import javax.swing.*;
import java.awt.*;
import java.time.*;
import java.util.Date;

import static utils.DateFormats.TIME_FORMAT_PATTERN;
import static utils.TimeZones.EUROPE_ROME;

public class DateTimePickerField extends JPanel {

    private static final ZoneId ZONE = ZoneId.of(EUROPE_ROME);

    private final DatePickerField datePicker;
    private final JSpinner timeSpinner;

    public DateTimePickerField() {
        this(null);
    }

    public DateTimePickerField(LocalDateTime initial) {
        super(new FlowLayout(FlowLayout.LEFT, 4, 0));
        datePicker = new DatePickerField(initial != null ? initial.toLocalDate() : null);
        timeSpinner = buildTimeSpinner();
        if (initial != null) {
            setSpinnerTime(initial.toLocalTime());
        }
        add(datePicker);
        add(timeSpinner);
    }

    public LocalDateTime getLocalDateTime() {
        LocalDate date = datePicker.getLocalDate();
        if (date == null) {
            return null;
        }
        return LocalDateTime.of(date, spinnerToLocalTime());
    }

    public void setLocalDateTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            clear();
            return;
        }
        datePicker.setLocalDate(dateTime.toLocalDate());
        setSpinnerTime(dateTime.toLocalTime());
    }

    public boolean isEmpty() {
        return datePicker.getLocalDate() == null;
    }

    public void clear() {
        datePicker.clear();
        setSpinnerTime(LocalTime.MIDNIGHT);
    }

    public void addChangeListener(Runnable listener) {
        datePicker.addChangeListener(listener);
        timeSpinner.addChangeListener(e -> listener.run());
    }

    private JSpinner buildTimeSpinner() {
        JSpinner spinner = new JSpinner(new SpinnerDateModel());
        spinner.setEditor(new JSpinner.DateEditor(spinner, TIME_FORMAT_PATTERN));
        spinner.setPreferredSize(new Dimension(70, 26));
        return spinner;
    }

    private LocalTime spinnerToLocalTime() {
        Object value = timeSpinner.getValue();
        if (value instanceof Date d) {
            return d.toInstant().atZone(ZONE).toLocalTime();
        }
        return LocalTime.MIDNIGHT;
    }

    private void setSpinnerTime(LocalTime time) {
        LocalDateTime ldt = LocalDateTime.of(LocalDate.now(ZONE), time);
        Instant instant = ldt.atZone(ZONE).toInstant();
        timeSpinner.setValue(new Date(instant.toEpochMilli()));
    }
}
