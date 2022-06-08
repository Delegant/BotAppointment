package org.telegram;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.telegram.Config.CURRENT_PROPERTIES;

public class CustomCalendar {
    private static final JsonFactory GSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final List<String> SCOPES = Collections.singletonList(CalendarScopes.CALENDAR);
    private static Calendar calendar;
    private static HttpRequestInitializer service;
    private static final Logger LOGGER = Logger.getLogger(CustomCalendar.class.getName());


    private static HttpRequestInitializer getCredentials(final NetHttpTransport HTTP_TRANSPORT) {
        try(InputStream in = EventMaker.class.getResourceAsStream(CURRENT_PROPERTIES.getProperty("credentialsServiceFilePath"))) {
            service = new HttpCredentialsAdapter(GoogleCredentials.fromStream(in).createScoped(SCOPES)); //HTTP_TRANSPORT,JSON_FACTORY).createScoped(SCOPES);
        }
        catch (IOException e){
            LOGGER.log(Level.WARNING, "Could not open the file with resources", e);
            System.exit(2);
        }
        return service;
    }
    public static synchronized Calendar instanceCalendar () {
        final Calendar currentCalendar;
        try {
            final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
                if (calendar == null) {
                    LOGGER.log(Level.INFO, "Field CALENDAR equal null, crate new calendar");
                    calendar = new Calendar.Builder(HTTP_TRANSPORT, GSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                            .setApplicationName(CURRENT_PROPERTIES.getProperty("filesUser"))
                            .build();
                }
            } catch (IOException | GeneralSecurityException e) {
            LOGGER.log(Level.WARNING, "IOException | GeneralSecurityException", e);
            System.exit(2);
            }
        currentCalendar = calendar;
        return currentCalendar;
    }
}
