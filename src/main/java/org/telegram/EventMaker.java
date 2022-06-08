package org.telegram;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.*;

import java.io.*;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static org.telegram.Config.*;

public class EventMaker {
    private static final Logger LOGGER = Logger.getLogger(EventMaker.class.getName());

    public static List<TimeInterval> getFreeUnitsTimeAtWorkday(TimeInterval timeIntervalBound) {
        List<TimeInterval> nextFewWorkDayList = TimeInterval.getNextFewWorkDay(Config.getOffsetToFindWorkDay(), Config.getWorkdayCount());
        if (nextFewWorkDayList.isEmpty()) return new ArrayList<>();
        return nextFewWorkDayList
                .stream()
                .map(t->t.intervalJoin(timeIntervalBound))
                .map(x -> EventMaker.eventsToList(EventMaker.getEventsAtTheDay(x))
                        .stream()
                        .map(x::getSubtractInterval))
                .flatMap(x->x.flatMap(Collection::stream))
                .map(TimeInterval::skipFreeTimeToUnit)
                .flatMap(x->x.stream().limit(1))
                .collect(Collectors.toList());
    }

    private static Events getEventsAtTheDay(TimeInterval nextWorkday) {
        Events events = new Events();
        LOGGER.log(Level.INFO, "nextWorkday - " + new DateTime(nextWorkday.getStartWorkAtEpochMilli()) + new DateTime(nextWorkday.getEndWorkAtEpochMilli()));
        try {
            events = CustomCalendar.instanceCalendar()
                    .events()
                    .list(CURRENT_PROPERTIES.getProperty("userId"))
                    .setMaxResults(NUMBER_OF_EVENTS)
                    .setTimeMin(new DateTime(nextWorkday.getStartWorkAtEpochMilli(), 0))
                    .setTimeMax(new DateTime(nextWorkday.getEndWorkAtEpochMilli(), 0))
                    .setOrderBy("startTime")
                    .setSingleEvents(true)
                    .execute();
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "List param {0}", CURRENT_PROPERTIES.getProperty("userId"));
            LOGGER.log(Level.WARNING, "IOException", e);
        }
        LOGGER.log(Level.INFO, "count events - " + events.getItems().size());
        return events;
    }

    private static List<TimeInterval> eventsToList(Events events) {
        List<TimeInterval> intervalArrayList = new ArrayList<>();
        for (Event eventItem : events.getItems()) {
            long starEventTimeUNIX = eventItem.getStart().getDateTime().getValue() / 1000;//get unix time start and end event from google.api.client.util.DateTime
            long endEventTimeUNIX = eventItem.getEnd().getDateTime().getValue() / 1000;
            LocalDateTime starEventTimeLDT = LocalDateTime.ofEpochSecond(starEventTimeUNIX, 0, ZoneOffset.ofHours(3));//get LocalDateTime from unix time
            LocalDateTime endEventTimeLDT = LocalDateTime.ofEpochSecond(endEventTimeUNIX, 0, ZoneOffset.ofHours(3));
            TimeInterval busyTime = new TimeInterval(starEventTimeLDT, endEventTimeLDT);
            intervalArrayList.add(busyTime);
        }
        intervalArrayList.add(new TimeInterval(DATE_NOW.toLocalDateTime(), DATE_NOW.toLocalDateTime())); //PRESENT!!!
        return intervalArrayList;
    }

    public static void createEvent() {
        Event event = new Event()
                .setSummary("Google I/O 2015")
                .setLocation("800 Howard St., San Francisco, CA 94103")
                .setDescription("A chance to hear more about Google's developer products.");

        DateTime startDateTime = new DateTime("2021-06-28T09:00:00-07:00");
        EventDateTime start = new EventDateTime()
                .setDateTime(startDateTime)
                .setTimeZone("America/Los_Angeles");
        event.setStart(start);

        DateTime endDateTime = new DateTime("2021-06-28T17:00:00-07:00");
        EventDateTime end = new EventDateTime()
                .setDateTime(endDateTime)
                .setTimeZone("America/Los_Angeles");
        event.setEnd(end);

        try {
            CustomCalendar.instanceCalendar().events().insert(CURRENT_PROPERTIES.getProperty("userId"), event).execute();
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "List param {0}", CURRENT_PROPERTIES.getProperty("userId"));
            LOGGER.log(Level.WARNING, "IOException", e);
        }

    }
}
//    public static List<TimeInterval> createEvent(int index) {
//        TimeInterval workDay = TimeInterval.getWorkDay(index);
//        TimeInterval event = new TimeInterval(workDay.getStartWork());
//        List<TimeInterval> freeTimeList = new ArrayList<>();
//        //создаем массив который нужно наполнить информацией свободными интервалами времени
//        LocalTime startWorkInterval = workDay.getStartWork();
//        //создаем интервал времени на основе начала дня + базовое время(30 минут)
//        for (Event eventItems : getFreeTime(1)) {
//            workDay.getStartWork();
//            long startWorkInterval = workDay.getStartWorkAtEpochMilli();
//            long dateTime = eventItems.getStart().getDateTime().getValue();
//        }
//        //создаем интервал времени на основе начала дня + базовое время(30 минут)
//        //стартКонсультации = время начала рабочего дня
//        // если стартКонсультации больше времени конца n события
//        //      то если стартКонсультации+времяКонсультации меньше вреени начала n события
//        //          то стартКонсультации записываем в массив
//        //      то если стартКонсультации+времяКонсультации больше времени начала n события
//        //          то стартКонсультации записываем в массив
//        // стартКонсультации больше
//    }


