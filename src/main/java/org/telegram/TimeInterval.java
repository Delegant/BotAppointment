package org.telegram;

import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.telegram.Config.*;

public class TimeInterval {

    private static final ResourceBundle MY_BUNDLE = ResourceBundle.getBundle("Labels");
    private static final Logger LOGGER = Logger.getLogger(EventMaker.class.getName());
    private final LocalTime start;
    private final LocalTime end;
    private LocalDateTime startLDT;
    private LocalDateTime endLDT;
//    private LocalDate localDate;

//    public TimeInterval(TimeInterval timeInterval, LocalDate localDate) {
//        this.start = timeInterval.start;
//        this.end = timeInterval.end;
//        this.localDate = localDate;
//        this.startLDT = start.atDate(localDate);
//        this.endLDT = end.atDate(localDate);
//    }

    public TimeInterval(LocalTime start, LocalTime end) {
        this.start = start;
        this.end = end;
    }

    public TimeInterval(LocalTime start, LocalTime end, LocalDate localDate) {
        this(start, end);
        this.startLDT = start.atDate(localDate);
        this.endLDT = end.atDate(localDate);
    }

    public TimeInterval(String start, String end) {
        this(LocalTime.parse(start), LocalTime.parse(end));
    }

    public TimeInterval(LocalDateTime startLDT, LocalDateTime endLDT) {
        this(startLDT.toLocalTime(), endLDT.toLocalTime());
        this.startLDT = startLDT;
        this.endLDT = endLDT;
    }

    public TimeInterval setLocalDate(LocalDate localDate) {
        this.startLDT = start.atDate(localDate);
        this.endLDT = end.atDate(localDate);
        return this;
    }

    public LocalDateTime getStartLDT() {
        return this.startLDT;
    }

    //поиск нескольких ближайших рабочих дней на основе рабочего графика
    public static List<TimeInterval> getNextFewWorkDay(int offsetToFindWorkDay, int workdayCount) {
        List<TimeInterval> fewNextWorkDayList = new ArrayList<>();
        ZonedDateTime dateNowPlusOffset = ZonedDateTime.from(DATE_NOW.plus(offsetToFindWorkDay, ChronoUnit.DAYS));
        int duration = (int) Duration.between(DATE_START_INTERVAL, dateNowPlusOffset).toDays();
        TimeInterval workdayTimetable = workDayArrayList.get(duration % INTERVAL);
        if (offsetToFindWorkDay < SEARCH_DEPTH && workdayCount > 0) {
            if (workdayTimetable != null) {
                fewNextWorkDayList.add(new TimeInterval(workdayTimetable.start, workdayTimetable.end, dateNowPlusOffset.toLocalDate()));
                fewNextWorkDayList.addAll(getNextFewWorkDay(offsetToFindWorkDay + 1, workdayCount - 1));
            } else {
                return getNextFewWorkDay(offsetToFindWorkDay + 1, workdayCount);
            }
        }
        return fewNextWorkDayList;
    }

    public List<TimeInterval> getSubtractInterval(TimeInterval subtrahendTimeInterval) {
        LOGGER.log(Level.INFO, "run getSubtractInterval: " + this.toString() + " % " + subtrahendTimeInterval.toString());
        if (subtrahendTimeInterval.startLDT == null || this.startLDT == null) throw new IllegalArgumentException();
        List<TimeInterval> timeIntervals = new LinkedList<>(List.of(this));
        ListIterator<TimeInterval> iterator = timeIntervals.listIterator();
        TimeInterval minuendTimeInterval = iterator.next();
        if (isIntervalLeftCrossing(subtrahendTimeInterval)) {
            TimeInterval freeTime = new TimeInterval(subtrahendTimeInterval.endLDT, minuendTimeInterval.endLDT);
            iterator.set(freeTime);
        } else if (isIntervalCentralCrossing(subtrahendTimeInterval)) {
            TimeInterval freeTime1 = new TimeInterval(minuendTimeInterval.startLDT, subtrahendTimeInterval.startLDT);
            TimeInterval freeTime2 = new TimeInterval(subtrahendTimeInterval.endLDT, minuendTimeInterval.endLDT);
            iterator.set(freeTime1);
            iterator.add(freeTime2);
        } else if (isIntervalRightCrossing(subtrahendTimeInterval)) {
            TimeInterval freeTime = new TimeInterval(minuendTimeInterval.startLDT, subtrahendTimeInterval.startLDT);
            iterator.set(freeTime);
        } else if (isIntervalNotCrossing(subtrahendTimeInterval)) {
            TimeInterval freeTime = new TimeInterval(minuendTimeInterval.startLDT, minuendTimeInterval.endLDT);
            iterator.set(freeTime);
        } else if (isIntervalFullCrossing(subtrahendTimeInterval)) {
            iterator.remove();
        }
        LOGGER.log(Level.INFO, "done getSubtractInterval: " + timeIntervals.toString());
        return timeIntervals;
    }

    public TimeInterval intervalJoin(TimeInterval timeInterval) {
        if (this.startLDT == null) throw new IllegalArgumentException();
        if (this.isIntervalLeftCrossing(timeInterval)) {
            return new TimeInterval(this.start, timeInterval.end, this.startLDT.toLocalDate());
        } else if (this.isIntervalRightCrossing(timeInterval)) {
            return new TimeInterval(timeInterval.start, this.end, this.startLDT.toLocalDate());
        } else if (this.isIntervalCentralCrossing(timeInterval)) {
            return new TimeInterval(timeInterval.start, timeInterval.end, this.startLDT.toLocalDate());
        } else if (this.isIntervalFullCrossing(timeInterval)) {
            return this;
        } else if (this.isIntervalNotCrossing(timeInterval)) {
            return null;
        }
        return null;
    }

    public List<TimeInterval> skipFreeTimeToUnit() {
        if (this.startLDT == null) throw new IllegalArgumentException();
        List<TimeInterval> unitWorkTimeIntervalList = new ArrayList<>();
        int baseUnitOfWorkDurationMinutes = Config.getBaseUnitOfWorkDurationMinutes();
        int baseUnitOfRelaxDurationMinutes = Config.getBaseUnitOfRelaxDurationMinutes();
        LocalDateTime unitWorkTimeStart = this.startLDT;
        LocalDateTime unitWorkTimeEnd = unitWorkTimeStart.plus(baseUnitOfWorkDurationMinutes, ChronoUnit.MINUTES);
        while (this.endLDT.isAfter(unitWorkTimeEnd.plus(baseUnitOfRelaxDurationMinutes, ChronoUnit.MINUTES))) {
            TimeInterval unitWorkTimeInterval = new TimeInterval(unitWorkTimeStart, unitWorkTimeEnd);
            unitWorkTimeIntervalList.add(unitWorkTimeInterval);
            unitWorkTimeStart = unitWorkTimeEnd;
            unitWorkTimeEnd = unitWorkTimeEnd.plus(baseUnitOfWorkDurationMinutes, ChronoUnit.MINUTES);
        }
        return unitWorkTimeIntervalList;
    }

    public long getStartWorkAtEpochMilli() {
        LocalDateTime startWorkDataTime = LocalDateTime.of(startLDT.toLocalDate(), start);
        return startWorkDataTime.toInstant(ZoneOffset.ofHours(3)).toEpochMilli();
    }

    public long getEndWorkAtEpochMilli() {
        LocalDateTime endWorkDataTime = LocalDateTime.of(endLDT.toLocalDate(), end);
        return endWorkDataTime.toInstant(ZoneOffset.ofHours(3)).toEpochMilli();
    }

    // [   ]
    //   [              ]
    public boolean isIntervalLeftCrossing(TimeInterval timeInterval) {
        if (timeInterval.isLocalTime() || this.isLocalTime()) {
            return (timeInterval.start.equals(this.start)
                    || timeInterval.start.isBefore(this.start))
                    && timeInterval.end.isAfter(this.start);
        }
        return (timeInterval.startLDT.equals(this.startLDT)
                || timeInterval.startLDT.isBefore(this.startLDT))
                && timeInterval.endLDT.isAfter(this.startLDT);

    }

    //                 [  ]
    //   [              ]
    public boolean isIntervalRightCrossing(TimeInterval timeInterval) {
        if (timeInterval.isLocalTime() || this.isLocalTime()) {
            return (timeInterval.start.isBefore(this.end)
                    && (timeInterval.end.isAfter(this.end)
                    || timeInterval.end.equals(this.end)));
        }
        return (timeInterval.startLDT.isBefore(this.endLDT)
                && (timeInterval.endLDT.isAfter(this.endLDT)
                || timeInterval.endLDT.equals(this.endLDT)));
    }

    //         [  ]
    //   [              ]
    public boolean isIntervalCentralCrossing(TimeInterval timeInterval) {
        if (timeInterval.isLocalTime() || this.isLocalTime()) {
            return timeInterval.start.isAfter(this.start)
                    && timeInterval.end.isBefore(this.end);
        }
        return timeInterval.startLDT.isAfter(this.startLDT)
                && timeInterval.endLDT.isBefore(this.endLDT);
    }

    //  [                ]
    //   [              ]
    public boolean isIntervalFullCrossing(TimeInterval timeInterval) {
        if (timeInterval.isLocalTime() || this.isLocalTime()) {
            return (timeInterval.start.isBefore(this.start)
                    || timeInterval.start.equals(this.start))
                    && (timeInterval.end.isAfter(this.end)
                    || timeInterval.end.equals(this.end));
        }
        return (timeInterval.startLDT.isBefore(this.startLDT)
                || timeInterval.startLDT.equals(this.startLDT))
                && (timeInterval.endLDT.isAfter(this.endLDT)
                || timeInterval.endLDT.equals(this.endLDT));
    }

    // [  ]                     [  ]
    //       [              ]
    public boolean isIntervalNotCrossing(TimeInterval timeInterval) {
        if (timeInterval.isLocalTime() || this.isLocalTime()) {
            return timeInterval.end.isBefore(this.start)
                    || timeInterval.end.equals(this.start)
                    || timeInterval.start.isAfter(this.end)
                    || timeInterval.start.equals(this.end);
        }
        return timeInterval.endLDT.isBefore(this.startLDT)
                || timeInterval.endLDT.equals(this.startLDT)
                || timeInterval.startLDT.isAfter(this.endLDT)
                || timeInterval.startLDT.equals(this.endLDT);
    }

    public boolean isLocalTime() {
        return this.startLDT == null ||
                this.endLDT == null;
    }

    @Override
    public String toString() {
        if (startLDT != null && startLDT.toLocalDate() == endLDT.toLocalDate()) {
            return startLDT.toLocalDate().toString() + ": " + start.toString() + "-" + end.toString() + "\n";
        } else if (startLDT != null && startLDT.toLocalDate() != endLDT.toLocalDate()) {
            return startLDT.toString() + "-" + endLDT.toString() + "\n";
        }
        return start.toString() + "-" + end.toString();
    }

}
