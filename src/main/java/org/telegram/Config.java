package org.telegram;

import java.io.*;
import java.time.Clock;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Config {
    private static final Logger LOGGER = Logger.getLogger(EventMaker.class.getName());
    public static final Properties CURRENT_PROPERTIES = Config.getProperties();

    public static synchronized Properties getProperties() {
        Properties currentProperties = null;
        try (InputStream isDefault = new FileInputStream("default.properties");
             InputStream isCurrent = new FileInputStream("current.properties")) {
            Properties defaultProperties = new Properties();
            defaultProperties.load(isDefault);
            Properties properties = new Properties(defaultProperties);
            properties.load(isCurrent);
            currentProperties = properties;
        }
        catch (IOException e) {
            LOGGER.log(Level.WARNING, "Could not find the file with properties", e);
            System.exit(2);
        }
        return currentProperties;
    }

    public static void saveProperties(String property) {
        try {
            try (OutputStream os = new FileOutputStream("current.properties")) {
                String[] propertyCouple = property.split("=");
                CURRENT_PROPERTIES.setProperty(propertyCouple[0], propertyCouple[1]);
                CURRENT_PROPERTIES.store(os, "change from Config.saveProperties");
            }
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Could not find the file for save properties", e);
            e.printStackTrace();
        }
    }

    public void loadProperties() throws IOException {
        InputStream is = getClass().getResourceAsStream("current.properties");
        CURRENT_PROPERTIES.load(is);
    }
    
    public static String sendTextProperties() {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<Object, Object> coupleKeyValue : CURRENT_PROPERTIES.entrySet()) {
            sb.append(coupleKeyValue.getKey());
            sb.append("\\=");
            sb.append(coupleKeyValue.getValue());
            sb.append("\n");
        }
        return sb.toString();
    }

    public static String parseToEscape(Object o) {
       String string = o.toString();
       return string.replace("_", "\\_")
                .replace("*", "\\*")
                .replace("[", "\\[")
                .replace("`", "\\`")
                .replace(".", "\\.")
                .replace("-", "\\-");
    }

    public static int getBaseUnitOfWorkDurationMinutes(){
      return  Integer.parseInt(CURRENT_PROPERTIES.getProperty("baseUnitOfWorkDurationMinutes"));
    }

    public static int getBaseUnitOfRelaxDurationMinutes(){
        return  Integer.parseInt(CURRENT_PROPERTIES.getProperty("unitOfRelaxDurationMinutes"));
    }

    public static int getOffsetToFindWorkDay(){
        return  Integer.parseInt(CURRENT_PROPERTIES.getProperty("offsetToFindWorkDay"));
    }

    public static int getWorkdayCount(){
        return  Integer.parseInt(CURRENT_PROPERTIES.getProperty("workdayCount"));
    }

    public static TimeInterval getMorningLocalTime(){
        return new TimeInterval(
                LocalTime.parse(CURRENT_PROPERTIES.getProperty("morningStart")),
                LocalTime.parse(CURRENT_PROPERTIES.getProperty("morningEnd")));
    }

    public static TimeInterval getDayLocalTime(){
        return new TimeInterval(
                LocalTime.parse(CURRENT_PROPERTIES.getProperty("dayStart")),
                LocalTime.parse(CURRENT_PROPERTIES.getProperty("dayEnd")));
    }

    public static TimeInterval getEveningLocalTime(){
        return new TimeInterval(
                LocalTime.parse(CURRENT_PROPERTIES.getProperty("eveningStart")),
                LocalTime.parse(CURRENT_PROPERTIES.getProperty("eveningEnd")));
    }

    public static ZoneId zone = ZoneId.of(CURRENT_PROPERTIES.getProperty("zoneId"));
    public static final ZonedDateTime DATE_START_INTERVAL = ZonedDateTime.of(2020, 6, 1, 0, 0, 0, 0, zone);
    public static final ZonedDateTime DATE_NOW = ZonedDateTime.now(Clock.system(zone));
    public static final int INTERVAL = 7;
    public static final int SEARCH_DEPTH = 14;
    public static final int NUMBER_OF_EVENTS = 7;
    public static final List<TimeInterval> workDayArrayList = new ArrayList<>(INTERVAL);

    static{
        workDayArrayList.add(0,null);
        workDayArrayList.add(1,new TimeInterval(LocalTime.of(7,0),LocalTime.of(17,0)));
        workDayArrayList.add(2,null);
        workDayArrayList.add(3,new TimeInterval(LocalTime.of(7,0),LocalTime.of(17,0)));
        workDayArrayList.add(4,null);
        workDayArrayList.add(5,null);
        workDayArrayList.add(6,null);
    }
}
